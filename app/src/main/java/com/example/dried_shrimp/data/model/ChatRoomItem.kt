package com.example.dried_shrimp.data.model

// 這裡必須是 data class，而且變數名稱要跟 Adapter 呼叫的一模一樣
data class ChatRoomItem(
    val id: String,        // 聊天室 ID
    val name: String,      // 對方名稱
    val lastMessage: String,
    val time: String,
    val icon: Int          // 圖示資源 ID
)