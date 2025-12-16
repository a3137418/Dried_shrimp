package com.example.dried_shrimp.data.model

enum class OrderStatus(val description: String) {
    PENDING_PAYMENT("待付款"),   // 買家剛下單
    PENDING_SHIPMENT("待出貨"),  // 買家已付款，賣家準備出貨
    SHIPPED("待收貨"),           // 賣家已出貨
    COMPLETED("已完成"),         // 買家確認收貨
    CANCELED("已取消")           // 交易取消
}