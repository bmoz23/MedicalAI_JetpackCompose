package com.teduniversity.medicalai.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.teduniversity.medicalai.model.Report
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ReportRepository {
    private val database = FirebaseDatabase.getInstance()
    private val reportsRef = database.getReference("reports")
    private val auth = FirebaseAuth.getInstance()

    suspend fun getUserReports(): Flow<List<Report>> = flow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val snapshot = reportsRef.orderByChild("userId").equalTo(userId).get().await()
            val reports = snapshot.children.mapNotNull { it.getValue<Report>() }
            emit(reports)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun addReport(report: Report) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val reportWithId = report.copy(
                id = reportsRef.push().key ?: throw Exception("Failed to generate report ID"),
                userId = userId
            )
            reportsRef.child(reportWithId.id).setValue(reportWithId).await()
        } catch (e: Exception) {
            throw e
        }
    }
} 