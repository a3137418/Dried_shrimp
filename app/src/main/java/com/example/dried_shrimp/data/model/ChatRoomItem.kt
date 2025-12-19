package com.example.dried_shrimp.data.model

data class ChatRoomItem(
    val id: String,        // 這是 Room ID (例如 chat_userA_userB)
    val name: String,      // 對方名稱
    val targetId: String,  // ★ 新增：對方的 UID
    val lastMessage: String,
    val time: String,
    val icon: Int,
    var avatarUrl: String = ""
)