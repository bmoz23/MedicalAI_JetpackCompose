package com.teduniversity.medicalai.data


import com.google.gson.annotations.SerializedName

/**
 * API'ye gönderilecek istek gövdesi:
 * { "message": "User's message" }
 */
data class ChatRequest(
    @SerializedName("message") val message: String
)

/**
 * API'den dönen cevap:
 * { "question": "Bot's question", "report_created": false }
 */
data class ChatResponse(
    @SerializedName("question") val question: String,
    @SerializedName("report_created") val reportCreated: Boolean = false
)
