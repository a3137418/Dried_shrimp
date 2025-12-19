package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.ChatMessage
import com.example.dried_shrimp.data.model.ChatRequest
import com.example.dried_shrimp.data.model.ChatResponse
import com.example.dried_shrimp.data.network.RetrofitInstance
import com.example.dried_shrimp.databinding.ActivityChatMainBinding
import com.example.dried_shrimp.ui.adapters.ChatAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatMainBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var currentUserId: String = ""
    private var chatRoomId: String = ""
    private var chatTargetId: String = ""  // AI 或賣家 ID
    private var chatTargetType: String = "" // "ai" 或 "seller"

    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, statusBarHeight, view.paddingRight, view.paddingBottom)
            insets
        }

        // 從 Intent 取得資料
        val chatTargetName = intent.getStringExtra("chat_target_name") ?: "聊天室"
        val productName = intent.getStringExtra("product_name")
        chatTargetId = intent.getStringExtra("chat_target_id") ?: "ai_service"
        chatTargetType = intent.getStringExtra("chat_target_type") ?: "ai"
        val passedRoomId = intent.getStringExtra("chat_room_id")
        binding.tvChatTitle.text = chatTargetName

        // 2. 取得當前使用者 ID
        val currentUser = auth.currentUser
        if (currentUser != null) {
            currentUserId = currentUser.uid

            // 3. 決定 Room ID
            if (passedRoomId != null) {
                // 如果有傳 ID 過來，就直接用 (解決買賣家 ID 不一致問題)
                chatRoomId = passedRoomId
            } else {
                // 如果沒傳 (例如從商品頁新發起的)，才用算的
                chatRoomId = "chat_${currentUserId}_${chatTargetId}"
            }
        } else {
            Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("ChatActivity", "Current User ID: $currentUserId")
        Log.d("ChatActivity", "Chat Room ID: $chatRoomId")
        Log.d("ChatActivity", "Target ID: $chatTargetId, Type: $chatTargetType")

        setupRecyclerView()
        setupListeners()
        // 初始化聊天室(如果不存在)
        initializeChatRoom(chatTargetName)
        // 開始監聽訊息
        listenToMessages()
        loadOpponentProfile()


    }
    // 修改 ChatMainActivity.kt 的這個函式
    private fun loadOpponentProfile() {
        // Log 1: 確認有開始執行
        Log.d("ChatDebug", "準備載入對方頭像，對方 ID: $chatTargetId, 類型: $chatTargetType")

        if (chatTargetType == "ai") return

        db.collection("users").document(chatTargetId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 嘗試抓取 photoUrl 或 imageUrl
                    val url = document.getString("photoUrl") ?: document.getString("imageUrl") ?: ""

                    // Log 2: 確認資料庫裡存的網址是什麼
                    Log.d("ChatDebug", "從 Firestore 抓到的圖片網址: $url")

                    if (url.isNotEmpty()) {
                        chatAdapter.setOpponentAvatar(url)
                    } else {
                        Log.d("ChatDebug", "網址是空的，顯示預設圖")
                    }
                } else {
                    Log.d("ChatDebug", "找不到該使用者的文件 (User Document 不存在)")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ChatDebug", "讀取資料庫失敗", e)
            }
    }
    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSend.setOnClickListener {
            val userInput = binding.etMessage.text.toString().trim()
            if (userInput.isNotEmpty()) {
                sendMessage(userInput)
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)
        binding.rvChatHistory.apply {
            layoutManager = LinearLayoutManager(this@ChatMainActivity)
            adapter = chatAdapter
        }
    }

    /**
     * 初始化聊天室(在使用者的 chatRooms 下建立聊天室資訊)
     */
    private fun initializeChatRoom(chatTargetName: String) {
        val chatRoomData = hashMapOf(
            "chatName" to chatTargetName,
            "participantId" to chatTargetId,
            "participantType" to chatTargetType,
            "lastMessage" to "",
            "lastMessageTime" to com.google.firebase.Timestamp.now(),
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        // 在當前使用者下建立聊天室
        db.collection("users")
            .document(currentUserId)
            .collection("chatRooms")
            .document(chatRoomId)
            .set(chatRoomData, com.google.firebase.firestore.SetOptions.merge())

        // 如果是與賣家聊天,也在賣家那邊建立聊天室
        if (chatTargetType == "seller") {
            val sellerChatRoomData = hashMapOf(
                "chatName" to getUserName(), // 在賣家那邊，顯示買家的名字
                "participantId" to currentUserId, // 對話對象是買家
                "participantType" to "buyer",
                "lastMessage" to "",
                "lastMessageTime" to com.google.firebase.Timestamp.now(),
                "createdAt" to com.google.firebase.Timestamp.now()
            )

            // 修正：db.collection("sellers") -> db.collection("users")
            db.collection("users")
                .document(chatTargetId) // 賣家的 UID
                .collection("chatRooms")
                .document(chatRoomId)
                .set(sellerChatRoomData, com.google.firebase.firestore.SetOptions.merge())
        }
    }

    /**
     * 監聽訊息
     */
    private fun listenToMessages() {
        if (currentUserId.isEmpty() || chatRoomId.isEmpty()) {
            Log.e("ChatActivity", "User ID or Room ID is empty!")
            return
        }

        // 從使用者的聊天室下讀取訊息
        db.collection("users")
            .document(currentUserId)
            .collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("ChatActivity", "監聽失敗", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    messageList.clear()

                    for (doc in snapshot.documents) {
                        try {
                            val message = doc.getString("message") ?: ""
                            val senderId = doc.getString("senderId") ?: ""
                            val timestamp = doc.getTimestamp("timestamp")

                            // 判斷是否為當前使用者發送
                            val isFromUser = (senderId == currentUserId)

                            Log.d("ChatActivity", "Message: $message, senderId: $senderId, isFromUser: $isFromUser")

                            val chatMsg = ChatMessage(message, isFromUser, timestamp)
                            messageList.add(chatMsg)

                        } catch (ex: Exception) {
                            Log.e("ChatActivity", "Failed to parse message", ex)
                        }
                    }

                    chatAdapter.notifyDataSetChanged()

                    if (messageList.isNotEmpty()) {
                        binding.rvChatHistory.scrollToPosition(messageList.size - 1)
                    }

                    Log.d("ChatActivity", "Total messages: ${messageList.size}")
                }
            }
    }

    /**
     * 發送訊息
     */
    private fun sendMessage(message: String) {
        binding.etMessage.text.clear()

        Log.d("ChatActivity", "Sending message: $message")

        // 儲存訊息到 Firestore
        saveMessageToFirestore(message, currentUserId)

        // 如果是 AI 聊天,呼叫 API
        if (chatTargetType == "ai") {
            val request = ChatRequest(message)
            RetrofitInstance.api.sendMessage(request).enqueue(object : Callback<ChatResponse> {
                override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                    if (response.isSuccessful) {
                        val reply = response.body()?.reply ?: "無法取得回應"
                        Log.d("ChatActivity", "AI reply: $reply")
                        saveMessageToFirestore(reply, chatTargetId)
                    } else {
                        Toast.makeText(this@ChatMainActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                    Log.e("ChatActivity", "API call failed", t)
                    Toast.makeText(this@ChatMainActivity, "連線失敗: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    /**
     * 儲存訊息到 Firestore
     */
    private fun saveMessageToFirestore(text: String, senderId: String) {
        val timestamp = com.google.firebase.Timestamp.now()

        val newMessage = hashMapOf(
            "message" to text,
            "senderId" to senderId,
            "timestamp" to timestamp
        )

        // 1. 儲存到當前使用者的聊天室 (保持不變)
        db.collection("users")
            .document(currentUserId)
            .collection("chatRooms")
            .document(chatRoomId)
            .collection("messages")
            .add(newMessage)
            .addOnSuccessListener {
                updateChatRoomLastMessage(text, timestamp)
            }

        // ★★★ 修正這裡：如果是與賣家聊天，也儲存到賣家的 "users" 集合 ★★★
        if (chatTargetType == "seller") {
            // 修正：db.collection("sellers") -> db.collection("users")
            db.collection("users")
                .document(chatTargetId) // 賣家的 UID
                .collection("chatRooms")
                .document(chatRoomId) // 使用相同的 Room ID
                .collection("messages")
                .add(newMessage)
                .addOnSuccessListener {
                    Log.d("ChatActivity", "Message saved to seller's chat room")
                    updateSellerChatRoomLastMessage(text, timestamp)
                }
        }
    }

    /**
     * 更新當前使用者聊天室的最後訊息
     */
    private fun updateChatRoomLastMessage(text: String, timestamp: com.google.firebase.Timestamp) {
        val updateData = hashMapOf(
            "lastMessage" to text,
            "lastMessageTime" to timestamp
        )

        db.collection("users")
            .document(currentUserId)
            .collection("chatRooms")
            .document(chatRoomId)
            .update(updateData as Map<String, Any>)
    }

    /**
     * 更新賣家聊天室的最後訊息
     */
    private fun updateSellerChatRoomLastMessage(text: String, timestamp: com.google.firebase.Timestamp) {
        val updateData = hashMapOf(
            "lastMessage" to text,
            "lastMessageTime" to timestamp
        )

        // ★★★ 修正：db.collection("sellers") -> db.collection("users") ★★★
        db.collection("users")
            .document(chatTargetId)
            .collection("chatRooms")
            .document(chatRoomId)
            .update(updateData as Map<String, Any>)
    }

    /**
     * 取得當前使用者名稱(從 Firestore 或 Auth)
     */
    private fun getUserName(): String {
        return auth.currentUser?.displayName ?: "買家"
    }
}