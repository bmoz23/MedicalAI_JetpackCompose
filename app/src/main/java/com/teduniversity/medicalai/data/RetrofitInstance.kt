package com.teduniversity.medicalai.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // EMÜLATÖR İÇİN baseUrl = "http://10.0.2.2:8000/"
    // Fiziksel cihaz test ediyorsanız, ev ağınızdaki IP'yi yazın (örneğin: "http://192.168.1.7:8000/")
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val api: ChatApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            // Coroutine desteği için adapter eklemek isterseniz:
            // .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
            .create(ChatApiService::class.java)
    }
}