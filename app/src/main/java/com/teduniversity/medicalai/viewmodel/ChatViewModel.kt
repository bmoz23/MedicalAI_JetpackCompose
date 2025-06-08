package com.teduniversity.medicalai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teduniversity.medicalai.data.ChatApiService
import com.teduniversity.medicalai.data.ChatRequest
import com.teduniversity.medicalai.data.ChatResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * ChatViewModel:
 * - messages: UI tarafından gözlemlenir; listeye yeni kullanıcı ve bot mesajları eklenir.
 * - sendChatMessage(): önce listeye kullanıcı mesajını ekler, sonra API çağrısı yapar, gelen cevabı yine listeye ekler.
 */
class ChatViewModel(
    private val apiService: ChatApiService
) : ViewModel() {

    // Chat ekranında gösterilecek mesaj listesi
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Send initial kickoff request when ViewModel is created
        initiateChat()
    }

    private fun initiateChat() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Get user UID from Firebase Auth
                val currentUser = Firebase.auth.currentUser
                if (currentUser == null) {
                    throw Exception("User not authenticated")
                }
                
                // Send kickoff request with session_uuid
                val response = apiService.initiateChat(sessionUuid = currentUser.uid)

                // Add bot's initial message to the messages
                val botMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = response.question,
                    isMine = false,
                    timestamp = Date()
                )
                _messages.value = listOf(botMessage)
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Failed to initialize chat: ${e.message}",
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
                // Get user UID from Firebase Auth
                val currentUser = Firebase.auth.currentUser
                if (currentUser == null) {
                    throw Exception("User not authenticated")
                }
                
                // Send message to chat endpoint
                val request = ChatRequest(message = userText)
                val response = apiService.sendMessage(
                    sessionUuid = currentUser.uid,
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

            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Failed to send message: ${e.message}",
                    isMine = false,
                    timestamp = Date()
                )
                _messages.value = _messages.value + errorMsg
            } finally {
                _isLoading.value = false
            }
        }
    }
}
