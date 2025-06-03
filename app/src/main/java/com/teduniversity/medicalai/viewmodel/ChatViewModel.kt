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
        // 1) Kullanıcı mesajını _messages’a ekle
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = userText,
            isMine = true,
            timestamp = Date()
        )
        _messages.value = _messages.value + userMessage

        viewModelScope.launch {
            try {
                // 2) API’ye çağrı yap
                val request = ChatRequest(message = userText)
                val response: ChatResponse = apiService.sendMessage(request)

                // 3) API’den gelen cevabı listeye ekle
                val botMessage = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = response.reply,
                    isMine = false,
                    timestamp = Date()
                )
                _messages.value = _messages.value + botMessage

            } catch (e: Exception) {
                // Hata durumunda gözlem için bir hata mesajı ekleyebilirsiniz
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "Sunucuya bağlanırken hata oluştu: ${e.message}",
                    isMine = false,
                    timestamp = Date()
                )
                _messages.value = _messages.value + errorMsg
            }
        }
    }
}
