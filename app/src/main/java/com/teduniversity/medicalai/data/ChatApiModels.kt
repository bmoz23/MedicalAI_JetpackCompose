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
 * { "question": "Bot's question" }
 */
data class ChatResponse(
    @SerializedName("question") val question: String
)
