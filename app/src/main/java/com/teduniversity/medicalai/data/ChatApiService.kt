package com.teduniversity.medicalai.data

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Retrofit üzerinden FastAPI vb. chat endpoint'ini tanımlayan interface.
 */
interface ChatApiService {

    @GET("crew/kickoff")
    suspend fun initiateChat(@Query("session_uuid") sessionUuid: String): ChatResponse

    @Headers("Content-Type: application/json")
    @POST("chat")
    suspend fun sendMessage(
        @Query("session_uuid") sessionUuid: String,
        @Body request: ChatRequest
    ): ChatResponse
}


