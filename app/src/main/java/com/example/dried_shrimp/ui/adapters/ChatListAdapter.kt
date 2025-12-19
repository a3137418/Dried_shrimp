package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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

            // ★★★ 修改圖片載入邏輯 ★★★
            if (item.avatarUrl.isNotEmpty()) {
                // 1. 如果有網址，用 Glide 載入
                Glide.with(itemView.context)
                    .load(item.avatarUrl)
                    .placeholder(item.icon) // 載入中顯示預設圖
                    .error(item.icon)       // 失敗顯示預設圖
                    .circleCrop()
                    .into(binding.ivAvatar)
            } else {
                // 2. 沒網址，顯示預設圖 (R.drawable.user 或 R.drawable.chat)
                binding.ivAvatar.setImageResource(item.icon)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}