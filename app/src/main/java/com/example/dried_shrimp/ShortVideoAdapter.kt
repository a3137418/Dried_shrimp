package com.example.dried_shrimp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ShortVideoAdapter(
    private val videoList: List<String>, // 本地或網路影片路徑
    private val onVideoClick: (String) -> Unit
) : RecyclerView.Adapter<ShortVideoAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_short_video, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = videoList[position]

        // 這裡你可以用 Glide 去產生縮圖
        // Glide.with(holder.itemView).load(path).into(holder.imgThumb)

        holder.itemView.setOnClickListener {
            onVideoClick(path)
        }
    }

    override fun getItemCount() = videoList.size
}

