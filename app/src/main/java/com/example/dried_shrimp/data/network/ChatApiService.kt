package com.example.dried_shrimp.data.network

import com.example.dried_shrimp.data.model.ChatRequest
import com.example.dried_shrimp.data.model.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ChatApiService {

    @Headers("Content-Type: application/json")
    @POST("/chat")
    fun sendMessage(@Body req: ChatRequest): Call<ChatResponse>

}