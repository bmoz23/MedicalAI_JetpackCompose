package com.teduniversity.medicalai.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teduniversity.medicalai.model.Notification
import com.teduniversity.medicalai.repository.NotificationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class NotificationViewModel : ViewModel() {
    private val repository = NotificationRepository.getInstance()
    private val auth = Firebase.auth
    private var currentUserId: String? = null
    
    companion object {
        private const val TAG = "NotificationViewModel"
    }
    
    // UI State
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadNotifications()
    }
    
    private fun loadNotifications() {
        // User değişikliğini kontrol et
        val userId = auth.currentUser?.uid
        if (currentUserId != userId) {
            Log.d(TAG, "User changed in ViewModel from $currentUserId to $userId")
            currentUserId = userId
            // Repository'yi yeniden initialize et
            Log.d(TAG, "Initializing NotificationRepository for user: $userId")
            repository.initialize()
        } else {
            Log.d(TAG, "User unchanged, ensuring repository is initialized")
            repository.initialize()
        }
        
        // Start loading
        _isLoading.value = true
        Log.d(TAG, "Loading started - isLoading set to true")
        
        // Load notifications
        viewModelScope.launch {
            try {
                repository.getUserNotifications().collect { notificationList ->
                    _notifications.value = notificationList
                    _isLoading.value = false // Set loading to false after first emission
                    Log.d(TAG, "Loading finished - isLoading set to false")
                    Log.d(TAG, "Updated notifications list with ${notificationList.size} items")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notifications: ${e.message}")
                _error.value = e.message
                _isLoading.value = false
                Log.d(TAG, "Loading finished due to error - isLoading set to false")
            }
        }
        
        // Load unread count
        viewModelScope.launch {
            try {
                repository.getUnreadCount().collect { count ->
                    _unreadCount.value = count
                    Log.d(TAG, "Updated unread count: $count")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading unread count: ${e.message}")
            }
        }
    }
    
    fun refreshNotifications() {
        viewModelScope.launch {
            Log.d(TAG, "Manual refresh triggered")
            try {
                // Refresh notifications
                repository.getUserNotifications().collect { notificationList ->
                    _notifications.value = notificationList
                    Log.d(TAG, "Refreshed notifications list with ${notificationList.size} items")
                }
                
                // Refresh unread count
                repository.getUnreadCount().collect { count ->
                    _unreadCount.value = count
                    Log.d(TAG, "Refreshed unread count: $count")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in refresh: ${e.message}")
                _error.value = e.message
            }
        }
    }
    
    fun markAsRead(notificationId: String) {
        repository.markAsRead(notificationId)
        Log.d(TAG, "Marked notification as read: $notificationId")
    }
    
    fun markAllAsRead() {
        repository.markAllAsRead()
        Log.d(TAG, "Marked all notifications as read")
    }
    
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "NotificationViewModel cleared")
    }
} 