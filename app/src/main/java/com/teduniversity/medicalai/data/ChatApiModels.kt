package com.teduniversity.medicalai.data


import com.google.gson.annotations.SerializedName

/**
 * API’ye gönderilecek istek gövdesi:
 * { "message": "Kullanıcının yazdığı mesaj" }
 */
data class ChatRequest(
    @SerializedName("message") val message: String
)

/**
 * API’den dönen cevap:
 * { "reply": "Botun cevabı" }
 */
data class ChatResponse(
    @SerializedName("reply") val reply: String
)
