package com.teduniversity.medicalai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teduniversity.medicalai.data.ChatApiService
import com.teduniversity.medicalai.data.ChatRequest
import com.teduniversity.medicalai.data.ChatResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.util.Log

/**
 * ChatViewModel:
 * - messages: UI tarafından gözlemlenir; listeye yeni kullanıcı ve bot mesajları eklenir.
 * - sendChatMessage(): önce listeye kullanıcı mesajını ekler, sonra API çağrısı yapar, gelen cevabı yine listeye ekler.
 */
class ChatViewModel(
    private val apiService: ChatApiService,
    private val onReportCreated: (() -> Unit)? = null
) : ViewModel() {

    // Chat ekranında gösterilecek mesaj listesi
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    // Session management
    private var sessionInitialized = false
    private var currentSessionUuid: String? = null

    init {
        // Initialize session only once
        if (!sessionInitialized) {
            initiateChat()
        }
    }

    private fun initiateChat() {
        if (sessionInitialized) return // Prevent multiple initializations
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Get user UID from Firebase Auth
                val currentUser = Firebase.auth.currentUser
                if (currentUser == null) {
                    throw Exception("User not authenticated")
                }
                
                currentSessionUuid = currentUser.uid
                sessionInitialized = true
                
                // Send kickoff request with session_uuid
                val response = apiService.initiateChat(sessionUuid = currentSessionUuid!!)

                // Add bot's initial message to the messages
                val botMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = response.question,
                    isMine = false,
                    timestamp = Date()
                )
                _messages.value = listOf(botMessage)
                
            } catch (e: Exception) {
                sessionInitialized = false // Reset on error to allow retry
                val errorText = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Initializing agents... This might take a moment. Please wait."
                    e.message?.contains("failed to connect", ignoreCase = true) == true -> 
                        "Connection failed. Please check your network and try again."
                    else -> "Failed to initialize chat: ${e.message}"
                }
                
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = errorText,
                    isMine = false,
                    timestamp = Date()
                )
                _messages.value = listOf(errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Basit bir modeller; ChatMessage Compose ekranından daha önceki dosyalarda tanımlanmıştı.
    data class ChatMessage(
        val id: String,
        val text: String,
        val isMine: Boolean,
        val timestamp: Date = Date()
    )

    /**
     * Kullanıcının yazdığı mesajı al ve önce ekrana yansıtalım,
     * sonra API çağrısı yapıp gelen cevabı yine ekrana yansıt.
     */
    fun sendChatMessage(userText: String) {
        // Prevent sending if already loading or session not initialized
        if (_isLoading.value || !sessionInitialized || currentSessionUuid == null) {
            return
        }
        
        // Add user message to the list
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = userText,
            isMine = true,
            timestamp = Date()
        )
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Send message to chat endpoint using current session
                val request = ChatRequest(message = userText)
                val response = apiService.sendMessage(
                    sessionUuid = currentSessionUuid!!,
                    request = request
                )

                // Add bot's response to the messages
                val botMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = response.question,
                    isMine = false,
                    timestamp = Date()
                )
                _messages.value = _messages.value + botMessage

                // Check if report was created and notify callback
                // Check both explicit flag and message content for report creation
                // BUT exclude cases where report generation failed
                val reportCreationFailed = response.question.contains("unable to generate", ignoreCase = true) ||
                    response.question.contains("issues with the available tools", ignoreCase = true) ||
                    response.question.contains("error generating", ignoreCase = true) ||
                    response.question.contains("tool error", ignoreCase = true) ||
                    response.question.contains("cannot access", ignoreCase = true)
                
                val reportWasCreated = !reportCreationFailed && (
                    response.reportCreated || 
                    response.question.contains("rapor oluşturuldu", ignoreCase = true) ||
                    response.question.contains("report created", ignoreCase = true) ||
                    response.question.contains("rapor hazırlandı", ignoreCase = true) ||
                    response.question.contains("report generated", ignoreCase = true) ||
                    response.question.contains("medical report saved successfully", ignoreCase = true) ||
                    response.question.contains("uploaded to firebase storage", ignoreCase = true)
                )
                
                Log.d("ChatViewModel", "Response: ${response.question}")
                Log.d("ChatViewModel", "Report created flag: ${response.reportCreated}")
                Log.d("ChatViewModel", "Report creation failed: $reportCreationFailed")
                Log.d("ChatViewModel", "Report detected from message: $reportWasCreated")
                
                if (reportWasCreated) {
                    Log.d("ChatViewModel", "Report creation detected! Refreshing reports in 2 seconds...")
                    // Wait a bit for Firebase to sync then refresh reports
                    viewModelScope.launch {
                        delay(2000) // Wait 2 seconds for backend to upload to Firebase
                        onReportCreated?.invoke()
                        Log.d("ChatViewModel", "Report refresh callback invoked")
                    }
                }

            } catch (e: Exception) {
                val errorText = when {
                    e.message?.contains("timeout", ignoreCase = true) == true -> 
                        "Agents are thinking... This might take a moment. Please try again."
                    e.message?.contains("failed to connect", ignoreCase = true) == true -> 
                        "Connection failed. Please check your network and try again."
                    e.message?.contains("500", ignoreCase = true) == true -> 
                        "Server error. Please try again in a moment."
                    else -> "Failed to send message: ${e.message}"
                }
                
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = errorText,
                    isMine = false,
                    timestamp = Date()
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Function to reset session if needed
    fun resetSession() {
        sessionInitialized = false
        currentSessionUuid = null
        _messages.value = emptyList()
        initiateChat()
    }
}
