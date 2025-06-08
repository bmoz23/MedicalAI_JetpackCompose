package com.teduniversity.medicalai.model

data class Report(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = 0,
    val imageUrl: String = "",
    val fileName: String = "",
    val fileSize: Long = 0,
    val lastModified: Long = 0
) 