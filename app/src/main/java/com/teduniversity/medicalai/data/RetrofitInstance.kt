package com.teduniversity.medicalai.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    // For emulator use 10.0.2.2 instead of localhost
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // Configure custom OkHttpClient with extended timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)  // Time to establish connection
        .readTimeout(60, TimeUnit.SECONDS)     // Time to read response
        .writeTimeout(30, TimeUnit.SECONDS)    // Time to write request
        .build()

    val api: ChatApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)  // Use custom client with extended timeouts
            .addConverterFactory(GsonConverterFactory.create())
            // Coroutine desteği için adapter eklemek isterseniz:
            // .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(ChatApiService::class.java)
    }
}