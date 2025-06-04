package com.teduniversity.medicalai.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Headers

/**
 * Retrofit üzerinden FastAPI vb. chat endpoint'ini tanımlayan interface.
 */
interface ChatApiService {

    @GET("crew/kickoff")
    suspend fun initiateChat(): ChatResponse

    @Headers("Content-Type: application/json")
    @POST("chat")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): ChatResponse
}


