package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.ChatMessage

class ChatAdapter(private val messageList: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    // ★ 新增：用來存對方頭像網址的變數
    private var opponentAvatarUrl: String = ""

    // ★ 新增：提供一個方法讓外部設定頭像
    fun setOpponentAvatar(url: String) {
        this.opponentAvatarUrl = url
        notifyDataSetChanged() // 重新整理畫面
    }
    // 定義兩種 ViewType：0 代表 AI (左邊)，1 代表 User (右邊)
    companion object {
        const val VIEW_TYPE_RECEIVE = 0
        const val VIEW_TYPE_SENT = 1
    }

    // 根據訊息是誰發的，回傳對應的 ViewType
    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]

        // 如果 isFromUser 是 true，就要回傳 SENT (代表右邊)
        return if (message.isFromUser) {
            VIEW_TYPE_SENT // 確保這裡對應的是載入 item_chat_sent.xml
        } else {
            VIEW_TYPE_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)



        val view = if (viewType == VIEW_TYPE_SENT) {
            // VIEW_TYPE_SENT (1) 應該載入 item_chat_sent (右邊)
            inflater.inflate(R.layout.item_chat_sent, parent, false)
        } else {
            // VIEW_TYPE_RECEIVE (0) 應該載入 item_chat_receive (左邊)
            inflater.inflate(R.layout.item_chat_receive, parent, false)
        }
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messageList[position]
        // ★ 傳入 opponentAvatarUrl 給 ViewHolder
        holder.bind(message, opponentAvatarUrl)
    }

    override fun getItemCount(): Int = messageList.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        // 嘗試找頭像 (只有 item_chat_receive 會有這個 ID，item_chat_sent 會是 null)
        private val ivAvatar: ImageView? = itemView.findViewById(R.id.ivAvatar)

        fun bind(message: ChatMessage, avatarUrl: String) {
            tvMessage.text = message.message

            // ★★★ 關鍵：如果有找到 ivAvatar (代表是對方發的訊息)，就載入圖片
            if (ivAvatar != null) {
                if (avatarUrl.isNotEmpty()) {
                    try {
                        Glide.with(itemView.context)
                            .load(avatarUrl)
                            .placeholder(R.drawable.user) // 載入中顯示預設圖
                            .error(R.drawable.user)       // 錯誤時顯示預設圖
                            .circleCrop()                 // 圓形裁切
                            .into(ivAvatar)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    // 如果對方沒網址，顯示預設圖
                    ivAvatar.setImageResource(R.drawable.user)
                }
            }
        }
    }
}