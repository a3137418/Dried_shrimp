package com.example.dried_shrimp.data.network

import com.example.dried_shrimp.data.model.ChatRequest
import com.example.dried_shrimp.data.model.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApiService {
    // 這裡的 "chat" 對應 Python 裡的 @app.route('/chat')
    @POST("chat")
    fun sendMessage(@Body request: ChatRequest): Call<ChatResponse>
}