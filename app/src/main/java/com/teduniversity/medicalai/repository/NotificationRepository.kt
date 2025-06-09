package com.teduniversity.medicalai.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.teduniversity.medicalai.model.Notification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private var isMonitoring = false
    private var currentUserId: String? = null
    
    // In-memory notifications
    private val _notifications = mutableListOf<Notification>()
    private var reviewedReports = mutableSetOf<String>() // Track already reviewed reports
    private var readNotifications = mutableSetOf<String>() // Track read notifications
    
    // Reactive streams
    private val _notificationsFlow = MutableStateFlow<List<Notification>>(emptyList())
    private val _unreadCountFlow = MutableStateFlow(0)
    
    companion object {
        private const val TAG = "NotificationRepository"
        
        @Volatile
        private var INSTANCE: NotificationRepository? = null
        
        fun getInstance(): NotificationRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationRepository().also { INSTANCE = it }
            }
        }
    }
    
    // Reports monitoring'i başlat
    fun initialize() {
        val userId = auth.currentUser?.uid
        Log.d(TAG, "Initialize called for user: $userId")
        
        // Eğer farklı bir user ile giriş yapıldıysa, önceki verileri temizle
        if (currentUserId != userId) {
            Log.d(TAG, "User changed from $currentUserId to $userId, resetting data")
            resetUserData()
            currentUserId = userId
            
            // Yeni user için read state'leri Firebase'den yükle
            Log.d(TAG, "Loading read notifications for new user")
            loadReadNotifications()
        }
        
        if (userId != null) {
            if (!isMonitoring) {
                Log.d(TAG, "Starting monitoring reports for user: $userId")
                startMonitoringReports()
                isMonitoring = true
            } else {
                Log.d(TAG, "Monitoring already active, restarting for reliability")
                isMonitoring = false
                startMonitoringReports()
                isMonitoring = true
            }
        } else {
            Log.w(TAG, "Cannot start monitoring - user is null")
        }
        
        // Initialize flows
        updateFlows()
        Log.d(TAG, "Initialize completed. Notifications: ${_notifications.size}, Unread: ${_notifications.count { !it.isRead }}")
    }
    
    // User değiştiğinde verileri sıfırla
    private fun resetUserData() {
        _notifications.clear()
        reviewedReports.clear()
        readNotifications.clear()
        isMonitoring = false // Bu önemli - monitoring'i reset ediyoruz
        updateFlows()
        Log.d(TAG, "User data reset completed - monitoring reset to false")
    }
    
    // Hasta raporlarını dinle ve reviewed değişikliklerini yakala
    fun startMonitoringReports() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "User not authenticated, cannot start monitoring reports")
            return
        }
        
        Log.d(TAG, "Setting up Firestore listener for reports collection: patients/$userId/reports")
        
        try {
            firestore
                .collection("patients")
                .document(userId)
                .collection("reports")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error monitoring reports: ${error.message}")
                        Log.e(TAG, "Error details: ${error.localizedMessage}")
                        // Reset monitoring flag to allow retry
                        isMonitoring = false
                        return@addSnapshotListener
                    }
                    
                    if (snapshot == null) {
                        Log.d(TAG, "Reports snapshot is null")
                        return@addSnapshotListener
                    }
                    
                    Log.d(TAG, "Reports snapshot received: ${snapshot.size()} documents, checking for reviewed changes...")
                    
                    snapshot.documents.forEach { document ->
                        val reportId = document.id
                        val reviewed = document.getBoolean("reviewed") ?: false
                        val fileName = document.getString("filename") ?: document.getString("fileName") ?: "Report"
                        val doctorDiagnosis = document.getString("doctor_diagnosis") ?: ""
                        
                        Log.d(TAG, "Report $reportId: reviewed = $reviewed, fileName = $fileName, diagnosis = $doctorDiagnosis")
                        
                        // Eğer reviewed true ve daha önce bu report için notification oluşturmadıysak
                        if (reviewed && !reviewedReports.contains(reportId)) {
                            Log.d(TAG, "Creating notification for newly reviewed report: $reportId")
                            createInMemoryNotification(reportId, fileName, doctorDiagnosis)
                            reviewedReports.add(reportId)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup Firestore listener: ${e.message}")
            isMonitoring = false
        }
    }
    
    // In-memory notification oluştur
    private fun createInMemoryNotification(reportId: String, fileName: String, doctorDiagnosis: String) {
        val notificationId = "notif_$reportId"
        val isRead = readNotifications.contains(notificationId) // Firebase'den gelen read state'i kontrol et
        
        val notification = Notification(
            id = notificationId,
            reportId = reportId,
            reportFileName = fileName,
            title = "Doctor Reviewed Your Report",
            message = "Doctor reviewed your report: $fileName",
            timestamp = System.currentTimeMillis(),
            isRead = isRead, // Firebase'den gelen state'i kullan
            doctorDiagnosis = doctorDiagnosis
        )
        
        _notifications.add(0, notification) // En başa ekle (en yeni)
        updateFlows()
        Log.d(TAG, "Added in-memory notification for report: $reportId with diagnosis: $doctorDiagnosis, isRead: $isRead")
    }
    
    // Notification'ları al
    fun getUserNotifications(): Flow<List<Notification>> = _notificationsFlow.asStateFlow()
    
    // Okunmamış sayıyı al
    fun getUnreadCount(): Flow<Int> = _unreadCountFlow.asStateFlow()
    
    // Notification'ı okundu olarak işaretle
    fun markAsRead(notificationId: String) {
        val index = _notifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            _notifications[index] = _notifications[index].copy(isRead = true)
            readNotifications.add(notificationId)
            saveReadNotificationToFirebase(notificationId)
            updateFlows()
            Log.d(TAG, "Marked notification as read: $notificationId")
        }
    }
    
    // Tümünü okundu olarak işaretle
    fun markAllAsRead() {
        _notifications.forEachIndexed { index, notification ->
            _notifications[index] = notification.copy(isRead = true)
            readNotifications.add(notification.id)
        }
        saveAllReadNotificationsToFirebase()
        updateFlows()
        Log.d(TAG, "Marked all notifications as read")
    }
    
    // Firebase'den read notifications'ları yükle
    private fun loadReadNotifications() {
        val userId = auth.currentUser?.uid ?: run {
            Log.w(TAG, "Cannot load read notifications - user not authenticated")
            return
        }
        
        Log.d(TAG, "Loading read notifications from Firebase for user: $userId")
        
        firestore
            .collection("patients")
            .document(userId)
            .collection("notification_settings")
            .document("read_notifications")
            .get()
            .addOnSuccessListener { document ->
                Log.d(TAG, "Firebase read notifications query successful. Document exists: ${document.exists()}")
                if (document.exists()) {
                    val readList = document.get("read_notification_ids") as? List<String> ?: emptyList()
                    readNotifications.clear()
                    readNotifications.addAll(readList)
                    Log.d(TAG, "Loaded ${readList.size} read notifications from Firebase: $readList")
                    
                    // Mevcut notification'ların read state'lerini güncelle
                    updateExistingNotificationsReadState()
                } else {
                    Log.d(TAG, "No read notifications document found in Firebase")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error loading read notifications: ${exception.message}")
            }
    }
    
    // Tek bir read notification'ı Firebase'e kaydet
    private fun saveReadNotificationToFirebase(notificationId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        firestore
            .collection("patients")
            .document(userId)
            .collection("notification_settings")
            .document("read_notifications")
            .set(mapOf("read_notification_ids" to readNotifications.toList()))
            .addOnSuccessListener {
                Log.d(TAG, "Successfully saved read notification: $notificationId")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error saving read notification: ${exception.message}")
            }
    }
    
    // Tüm read notifications'ları Firebase'e kaydet
    private fun saveAllReadNotificationsToFirebase() {
        val userId = auth.currentUser?.uid ?: return
        
        firestore
            .collection("patients")
            .document(userId)
            .collection("notification_settings")
            .document("read_notifications")
            .set(mapOf("read_notification_ids" to readNotifications.toList()))
            .addOnSuccessListener {
                Log.d(TAG, "Successfully saved all read notifications")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error saving all read notifications: ${exception.message}")
            }
    }
    
    // Mevcut notification'ların read state'lerini güncelle
    private fun updateExistingNotificationsReadState() {
        _notifications.forEachIndexed { index, notification ->
            if (readNotifications.contains(notification.id)) {
                _notifications[index] = notification.copy(isRead = true)
            }
        }
        updateFlows()
        Log.d(TAG, "Updated existing notifications read state")
    }
    
    // Flow'ları güncelle
    private fun updateFlows() {
        _notificationsFlow.value = _notifications.toList()
        _unreadCountFlow.value = _notifications.count { !it.isRead }
        Log.d(TAG, "Updated flows - Notifications: ${_notifications.size}, Unread: ${_notifications.count { !it.isRead }}")
    }
    
    // Test amaçlı notification oluştur
    fun createTestNotification() {
        val timestamp = System.currentTimeMillis()
        val testNotification = Notification(
            id = "test_$timestamp",
            reportId = "test_report_$timestamp",
            reportFileName = "Test Report.pdf",
            title = "Doctor Reviewed Your Report",
            message = "Doctor reviewed your report: Test Report.pdf",
            timestamp = timestamp,
            isRead = false
        )
        
        _notifications.add(0, testNotification)
        updateFlows()
        Log.d(TAG, "Created test notification with ID: test_$timestamp")
    }
} 