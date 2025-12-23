package com.example.dried_shrimp.data.model

data class Review(
    val reviewId: String = "",
    val orderId: String = "",
    val productId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)