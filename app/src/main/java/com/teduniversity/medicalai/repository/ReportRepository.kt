package com.teduniversity.medicalai.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import com.teduniversity.medicalai.model.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ReportRepository {
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val auth = FirebaseAuth.getInstance()
    
    suspend fun getUserReports(): Flow<List<Report>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            Log.d("ReportRepository", "Loading reports for user: $userId")
            
            // Storage'dan dosya listesini al
            val storageRef = storage.reference
                .child("patients")
                .child(userId)
                .child("reports")
            
            Log.d("ReportRepository", "Checking Firebase Storage path: patients/$userId/reports")
            val listResult = storageRef.listAll().await()
            Log.d("ReportRepository", "Found ${listResult.items.size} files in Firebase Storage")
            
            val reports = mutableListOf<Report>()
            
            // Her dosya için metadata'yı al ve Report objesi oluştur
            for (item in listResult.items) {
                try {
                    Log.d("ReportRepository", "Processing file: ${item.name}")
                    val metadata = item.metadata.await()
                    val downloadUrl = item.downloadUrl.await().toString()
                    val fileName = item.name
                    
                    // Dosya isminden title çıkar
                    val title = extractTitleFromFileName(fileName)
                    
                    // Firestore'dan bu dosya için ek bilgi var mı kontrol et
                    val firestoreData = getReportFromFirestore(userId, fileName)
                    
                    val report = Report(
                        id = fileName, // dosya ismi unique ID olarak kullan
                        userId = userId,
                        title = firestoreData?.title?.ifEmpty { title } ?: title,
                        description = firestoreData?.description ?: "",
                        date = metadata.creationTimeMillis ?: System.currentTimeMillis(),
                        imageUrl = downloadUrl,
                        fileName = fileName,
                        fileSize = metadata.sizeBytes,
                        lastModified = metadata.updatedTimeMillis ?: metadata.creationTimeMillis ?: System.currentTimeMillis()
                    )
                    
                    reports.add(report)
                    Log.d("ReportRepository", "Added report: ${report.title}")
                } catch (e: Exception) {
                    Log.e("ReportRepository", "Error processing file ${item.name}: ${e.message}")
                    // Tek dosya hatası tüm listeyi bozmasın
                    continue
                }
            }
            
            Log.d("ReportRepository", "Successfully loaded ${reports.size} reports")
            emit(reports)
        } catch (e: Exception) {
            Log.e("ReportRepository", "Error getting user reports: ${e.message}", e)
            throw e
        }
    }
    
    private suspend fun getReportFromFirestore(userId: String, fileName: String): Report? {
        return try {
            val snapshot = firestore
                .collection("patients")
                .document(userId)
                .collection("reports")
                .whereEqualTo("fileName", fileName)
                .get()
                .await()
            
            snapshot.documents.firstOrNull()?.toObject(Report::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractTitleFromFileName(fileName: String): String {
        return try {
            // medical_report_20250605_012702_Persistent_headache_and_fatigu.pdf
            // formatından başlığı çıkar
            val withoutExtension = fileName.substringBeforeLast(".")
            val parts = withoutExtension.split("_")
            
            if (parts.size > 4) {
                // İlk 4 kısmı atla (medical_report_date_time) ve geri kalanı birleştir
                val titleParts = parts.drop(4)
                titleParts.joinToString(" ") { part ->
                    part.replace("_", " ").replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase() else it.toString() 
                    }
                }
            } else {
                // Eğer format beklendiği gibi değilse, dosya isminin kendisini kullan
                withoutExtension.replace("_", " ").replaceFirstChar { 
                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                }
            }
        } catch (e: Exception) {
            // Herhangi bir hata durumunda dosya ismini kullan
            fileName.substringBeforeLast(".").replace("_", " ")
        }
    }
    
    suspend fun addReport(report: Report) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            
            // Firestore path: patients/{userId}/reports
            val reportsCollection = firestore
                .collection("patients")
                .document(userId)
                .collection("reports")
                
            val documentRef = reportsCollection.document()
            val reportWithId = report.copy(
                id = documentRef.id,
                userId = userId
            )
            
            documentRef.set(reportWithId).await()
        } catch (e: Exception) {
            throw e
        }
    }
    
    suspend fun getReportById(reportId: String): Report? {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            
            val document = firestore
                .collection("patients")
                .document(userId)
                .collection("reports")
                .document(reportId)
                .get()
                .await()
                
            document.toObject(Report::class.java)?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun deleteReport(reportId: String) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            
            firestore
                .collection("patients")
                .document(userId)
                .collection("reports")
                .document(reportId)
                .delete()
                .await()
        } catch (e: Exception) {
            throw e
        }
    }
} 