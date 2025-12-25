package com.example.dried_shrimp.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.R

class CategoryMenuAdapter(
    private val categoryList: List<String>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryMenuAdapter.ViewHolder>() {

    // 記錄目前選中的位置，預設第 0 個
    private var selectedPosition = 0

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        holder.tvName.text = category

        // 🔥 變色邏輯：如果是選中項，背景變橘色/文字變白；否則背景白色/文字黑色
        if (selectedPosition == position) {
            holder.tvName.setBackgroundColor(Color.parseColor("#FF5722")) // 橘色背景
            holder.tvName.textColor = Color.WHITE
        } else {
            holder.tvName.setBackgroundColor(Color.parseColor("#F5F5F5")) // 淺灰背景
            holder.tvName.textColor = Color.BLACK
        }

        // 點擊事件
        holder.itemView.setOnClickListener {
            // 更新選中位置
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            // 通知 Adapter 刷新這兩個項目 (舊的變回來，新的變色)
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            // 觸發回調，告訴 Fragment 要換商品了
            onCategoryClick(category)
        }
    }

    // 把 helper property 轉成 setTextColor 方法
    private var TextView.textColor: Int
        get() = currentTextColor
        set(value) = setTextColor(value)

    override fun getItemCount() = categoryList.size
}