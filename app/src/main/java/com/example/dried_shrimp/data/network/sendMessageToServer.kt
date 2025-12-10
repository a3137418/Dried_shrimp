package com.example.dried_shrimp.data.network

import android.content.Context
import android.widget.Toast
import com.example.dried_shrimp.data.model.ChatRequest
import com.example.dried_shrimp.data.model.ChatResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun sendMessageToServer(context: Context, message: String) {

    val request = ChatRequest(message)

    RetrofitInstance.api.sendMessage(request)
        .enqueue(object : Callback<ChatResponse> {

            override fun onResponse(
                call: Call<ChatResponse>,
                response: Response<ChatResponse>
            ) {
                if (response.isSuccessful) {
                    val reply = response.body()?.reply ?: ""
                    Toast.makeText(context, "AI：" + reply, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                Toast.makeText(context, "連線失敗：" + t.message, Toast.LENGTH_SHORT).show()
            }
        })
}
