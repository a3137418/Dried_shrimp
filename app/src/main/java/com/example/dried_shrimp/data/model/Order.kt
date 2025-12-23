package com.example.dried_shrimp.data.model

import java.io.Serializable

data class Order(
    val orderId: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Int = 0,
    val receiverName: String = "",
    val receiverPhone: String = "",
    val receiverAddress: String = "",
    val status: String = "",
    val timestamp: Long = 0,
    // 🔥 請新增這行！這是判斷按鈕顯示「去評價」還是「查看評價」的關鍵
    val hasReviewed: Boolean = false
) : Serializable