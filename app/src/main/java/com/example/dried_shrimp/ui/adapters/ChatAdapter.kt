package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.ChatMessage

class ChatAdapter(private val messageList: List<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

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
        holder.bind(message)
    }

    override fun getItemCount(): Int = messageList.size

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 假設你的 layout 裡面的 TextView ID 叫做 tvMessage
        // 這裡先用 findViewById，也可以改用 ViewBinding
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)

        fun bind(message: ChatMessage) {
            tvMessage.text = message.message
        }
    }
}