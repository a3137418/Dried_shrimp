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
        if (currentUser == null) {
            Log.e("ChatList", "User not logged in")
            return
        }

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

                if (snapshot != null && !snapshot.isEmpty) {
                    chatRooms.clear()

                    for (doc in snapshot.documents) {
                        try {
                            val chatName = doc.getString("chatName") ?: "未知"
                            val lastMessage = doc.getString("lastMessage") ?: ""
                            val participantId = doc.getString("participantId") ?: ""
                            val participantType = doc.getString("participantType") ?: "ai"
                            val timestamp = doc.getTimestamp("lastMessageTime")

                            // 格式化時間
                            val timeStr = formatTimestamp(timestamp)

                            val chatRoomItem = ChatRoomItem(
                                id = doc.id,
                                name = chatName,
                                lastMessage = lastMessage,
                                time = timeStr,
                                icon = if (participantType == "ai") R.drawable.chat else R.drawable.user
                            )

                            chatRooms.add(chatRoomItem)

                        } catch (ex: Exception) {
                            Log.e("ChatList", "Failed to parse chat room", ex)
                        }
                    }

                    adapter.notifyDataSetChanged()
                    Log.d("ChatList", "Loaded ${chatRooms.size} chat rooms")
                }
            }
    }

    /**
     * 導航到聊天室
     */
    private fun navigateToChatRoom(item: ChatRoomItem) {
        val intent = Intent(requireContext(), ChatMainActivity::class.java)
        intent.putExtra("chat_target_name", item.name)
        intent.putExtra("chat_target_id", item.id.substringAfterLast("_"))
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