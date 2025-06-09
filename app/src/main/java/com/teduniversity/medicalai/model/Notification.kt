package com.teduniversity.medicalai.model

data class Notification(
    val id: String = "",
    val reportId: String = "",
    val reportFileName: String = "",
    val title: String = "Doctor Reviewed Your Report",
    val message: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false,
    val doctorDiagnosis: String = ""
) 