package com.example.dried_shrimp.data.model

import com.google.firebase.Timestamp

data class ChatMessage(
    val message: String = "",
    val isFromUser: Boolean = false,
    val timestamp: Timestamp? = null
) {
    // Firestore 需要無參數建構子
    constructor() : this("", false, null)
}