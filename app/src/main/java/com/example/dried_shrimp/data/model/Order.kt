package com.example.dried_shrimp.data.model

import java.io.Serializable

data class Order(
    val orderId: String = "",
    val buyerId: String = "",
    val sellerId: String = "", // ★ 新增：這張訂單屬於哪個賣家
    val items: List<CartItem> = emptyList(),
    val totalPrice: Int = 0,
    val receiverName: String = "",
    val receiverPhone: String = "",
    val receiverAddress: String = "",
    val status: String = "",
    val timestamp: Long = 0
) : Serializable