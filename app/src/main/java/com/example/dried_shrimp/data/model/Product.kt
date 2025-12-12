package com.example.dried_shrimp.data.model

data class Product(
    var id: String = "",              // Firebase 文件 ID
    val sellerId: String = "",        // 賣家 ID
    val name: String = "",            // 商品名稱
    val description: String = "",     // 商品描述
    val category: String = "",        // 分類
    val price: Int = 0,               // 價格
    val stock: Int = 0,               // 商品數量 (庫存)
    val minQuantity: Int = 1,         // 最低購買數量 (預設 1)
    val imageUrl: String = "",        // 商品圖片網址
    val gtin: String = "",            // GTIN (選填)
    val shippingFee: Int = 60,        // 運費 (範例預設值)
    val timestamp: Long = System.currentTimeMillis() // 上架時間
)