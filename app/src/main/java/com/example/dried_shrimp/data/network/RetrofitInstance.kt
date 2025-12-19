package com.example.dried_shrimp.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // 修改這裡！確保是 http:// 開頭，並以 / 結尾
    // 10.0.2.2 是模擬器連線電腦本機的專用 IP
    private const val BASE_URL = "http://10.0.2.2:5000/"

    val api: ChatApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApiService::class.java)
    }
}