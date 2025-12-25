package com.example.dried_shrimp.data.model

data class Category(
    val id: String = "",        // 分類 ID (可以用名稱當 ID，也可以用數字)
    val name: String = "",      // 分類名稱 (顯示在畫面上)
    val iconResId: Int = 0      // (選用) 對應 drawable 的圖示 ID
)