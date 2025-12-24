package com.example.dried_shrimp.data.model

data class Review(
    val reviewId: String = "",
    val orderId: String = "",
    val productId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val content: String = "",
    val rating: Float = 0f,
    val timestamp: Long = 0,
    val buyerName: String = "匿名買家", // 對應寫入時的 key
    val buyerAvatar: String = ""
)