package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.data.model.ChatRoomItem
import com.example.dried_shrimp.databinding.ItemChatRoomBinding

// 這裡我們傳入一個 Lambda (onItemClick) 來處理點擊事件
class ChatListAdapter(
    private val chatList: List<ChatRoomItem>,
    private val onItemClick: (ChatRoomItem) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        // 使用 ViewBinding 來綁定 layout (假設你的 item layout 檔名是 item_chat_room.xml)
        val binding = ItemChatRoomBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatRoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(chatList[position])
    }

    override fun getItemCount(): Int = chatList.size

    inner class ChatRoomViewHolder(private val binding: ItemChatRoomBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatRoomItem) {
            binding.tvUserName.text = item.name
            binding.tvLastMessage.text = item.lastMessage
            binding.tvTime.text = item.time
            binding.ivAvatar.setImageResource(item.icon)

            // 設定點擊事件
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}