package com.teduniversity.medicalai.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository {
    private val storage = Firebase.storage
    private val auth = FirebaseAuth.getInstance()
    
    suspend fun uploadReport(fileUri: Uri, fileName: String? = null): String {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        
        // Dosya adını oluştur
        val finalFileName = fileName ?: "${UUID.randomUUID()}_${System.currentTimeMillis()}"
        
        // Storage path: patients/{userId}/reports/{fileName}
        val storageRef = storage.reference
            .child("patients")
            .child(userId)
            .child("reports")
            .child(finalFileName)
        
        // Dosyayı yükle
        val uploadTask = storageRef.putFile(fileUri)
        uploadTask.await()
        
        // Download URL'ini al
        return storageRef.downloadUrl.await().toString()
    }
    
    suspend fun deleteReport(downloadUrl: String) {
        try {
            val storageRef = storage.getReferenceFromUrl(downloadUrl)
            storageRef.delete().await()
        } catch (e: Exception) {
            throw Exception("Failed to delete file: ${e.message}")
        }
    }
    
    suspend fun getAllReportFiles(): List<String> {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        
        val storageRef = storage.reference
            .child("patients")
            .child(userId)
            .child("reports")
        
        val listResult = storageRef.listAll().await()
        return listResult.items.map { it.downloadUrl.await().toString() }
    }
} 