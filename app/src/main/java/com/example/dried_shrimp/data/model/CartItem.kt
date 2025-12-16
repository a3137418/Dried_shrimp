package com.example.dried_shrimp.data.model

import java.io.Serializable

data class CartItem(
    val productId: String = "",
    val name: String = "",
    val price: Int = 0,
    val imageUrl: String = "",
    var quantity: Int = 1,
    var isChecked: Boolean = false, // 是否勾選

    // ★★★ 務必補上這個欄位，否則結帳會失敗 ★★★
    val sellerId: String = ""
) : Serializable