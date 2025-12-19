package com.example.dried_shrimp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.ChatRoomItem
import com.example.dried_shrimp.databinding.FragmentChatListBinding
import com.example.dried_shrimp.ui.activities.ChatMainActivity
import com.example.dried_shrimp.ui.adapters.ChatListAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class Fragment_ChatList : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatRooms = mutableListOf<ChatRoomItem>()
    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadChatRooms()
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter(chatRooms) { item ->
            // 點擊聊天室,開啟對話
            navigateToChatRoom(item)
        }

        binding.rvChatList.layoutManager = LinearLayoutManager(context)
        binding.rvChatList.adapter = adapter
    }

    /**
     * 從 Firestore 載入聊天室清單
     */
    private fun loadChatRooms() {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        val userId = currentUser.uid

        db.collection("users")
            .document(userId)
            .collection("chatRooms")
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("ChatList", "監聽失敗", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    chatRooms.clear()

                    for (doc in snapshot.documents) {
                        try {
                            val chatName = doc.getString("chatName") ?: "未知"
                            val lastMessage = doc.getString("lastMessage") ?: ""
                            val participantId = doc.getString("participantId") ?: ""
                            val participantType = doc.getString("participantType") ?: "ai"
                            val timestamp = doc.getTimestamp("lastMessageTime")
                            val timeStr = formatTimestamp(timestamp)

                            // 1. 先建立基本的 Item (圖片先空著)
                            val chatRoomItem = ChatRoomItem(
                                id = doc.id,
                                name = chatName,
                                targetId = participantId,
                                lastMessage = lastMessage,
                                time = timeStr,
                                icon = if (participantType == "ai") R.drawable.chat else R.drawable.user,
                                avatarUrl = "" // 先設為空
                            )

                            chatRooms.add(chatRoomItem)

                            // 2. 如果不是 AI，額外去抓取頭像
                            if (participantType != "ai" && participantId.isNotEmpty()) {
                                fetchUserAvatar(participantId, chatRooms.size - 1)
                            }

                        } catch (ex: Exception) {
                            Log.e("ChatList", "Error", ex)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
    // ★★★ 新增：抓取使用者頭像的函式 ★★★
    private fun fetchUserAvatar(targetUserId: String, position: Int) {
        db.collection("users").document(targetUserId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 抓取 photoUrl 或 imageUrl
                    val url = document.getString("photoUrl") ?: document.getString("imageUrl")

                    if (!url.isNullOrEmpty()) {
                        // 1. 確保列表沒有被清空或 index 跑掉
                        if (position < chatRooms.size) {
                            val item = chatRooms[position]
                            // 2. 雙重確認 ID 是否匹配 (避免網路延遲導致更新錯人)
                            if (item.targetId == targetUserId) {
                                item.avatarUrl = url
                                // 3. 只更新這一行，避免整個畫面閃爍
                                adapter.notifyItemChanged(position)
                            }
                        }
                    }
                }
            }
    }

    /**
     * 導航到聊天室
     */
    private fun navigateToChatRoom(item: ChatRoomItem) {
        val intent = Intent(requireContext(), ChatMainActivity::class.java)
        intent.putExtra("chat_target_name", item.name)

        // ★ 關鍵 1：直接使用我們存好的 targetId，不要用 substring 去猜
        intent.putExtra("chat_target_id", item.targetId)

        // ★ 關鍵 2：直接傳送 Room ID，確保雙方進入同一個房間
        intent.putExtra("chat_room_id", item.id)

        intent.putExtra("chat_target_type", if (item.id.contains("ai_service")) "ai" else "seller")
        startActivity(intent)
    }

    /**
     * 格式化時間戳
     */
    private fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""

        val now = System.currentTimeMillis()
        val messageTime = timestamp.toDate().time
        val diff = now - messageTime

        return when {
            diff < 60000 -> "剛剛"
            diff < 3600000 -> "${diff / 60000} 分鐘前"
            diff < 86400000 -> "${diff / 3600000} 小時前"
            else -> {
                val dateFormat = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
                dateFormat.format(timestamp.toDate())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}