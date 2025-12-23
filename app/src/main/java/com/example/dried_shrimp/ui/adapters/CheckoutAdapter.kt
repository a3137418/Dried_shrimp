package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dried_shrimp.data.model.CartItem
import com.example.dried_shrimp.databinding.ItemCheckoutBinding // 確保這裡對應您的 XML 檔名

class CheckoutAdapter(
    private val items: List<CartItem>
) : RecyclerView.Adapter<CheckoutAdapter.ViewHolder>() {

    // ViewBinding 的名稱會根據 XML 檔名自動產生
    // 如果您的 XML 檔名是 item_checkout.xml，這裡就是 ItemCheckoutBinding
    inner class ViewHolder(val binding: ItemCheckoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCheckoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            tvProductName.text = item.name
            tvProductPrice.text = "$${item.price}"

            // 綁定數量 (tvItemCount)
            tvItemCount.text = "x ${item.quantity}"

            // 綁定小計 (tvFinalPrice)
            val subtotal = item.price * item.quantity
            tvFinalPrice.text = "$$subtotal"

            // 載入圖片
            if (item.imageUrl.isNotEmpty()) {
                Glide.with(root.context)
                    .load(item.imageUrl)
                    .into(imgProduct)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}