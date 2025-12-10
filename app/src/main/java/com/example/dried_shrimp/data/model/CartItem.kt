package com.example.dried_shrimp.data.model

// 定義商品資料結構
data class CartItem(
    val name: String = "",       // 商品名稱 (一定要有預設值)
    val price: Int = 0,          // 價格
    val imageResId: Int = 0,     // 圖片 ID (若是網路圖片則存 URL 字串)
    val quantity: Int = 1        // 數量
)