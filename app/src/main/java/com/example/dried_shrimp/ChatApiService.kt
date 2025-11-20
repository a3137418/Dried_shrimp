package com.example.dried_shrimp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class ChatRequest(val message: String)
data class ChatResponse(val reply: String)

interface ChatApiService {

    @Headers("Content-Type: application/json")
    @POST("/chat")
    fun sendMessage(@Body req: ChatRequest): Call<ChatResponse>

}
