package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dried_shrimp.data.model.CartItem
import com.example.dried_shrimp.databinding.ItemCartBinding

class CartAdapter(
    private var cartList: List<CartItem>,
    // 定義三個動作：減少、增加、刪除
    private val onMinusClick: (CartItem) -> Unit,
    private val onPlusClick: (CartItem) -> Unit,
    private val onDeleteClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = cartList[position]

        holder.binding.apply {
            tvName.text = item.name
            tvPrice.text = "$${item.price}"
            tvQuantity.text = item.quantity.toString()

            if (item.imageUrl.isNotEmpty()) {
                Glide.with(root.context).load(item.imageUrl).into(imgProduct)
            }

            // 按鈕事件綁定
            imgMinus.setOnClickListener { onMinusClick(item) }
            imgAdd.setOnClickListener { onPlusClick(item) }
            btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    override fun getItemCount() = cartList.size

    fun updateData(newList: List<CartItem>) {
        cartList = newList
        notifyDataSetChanged()
    }
}