package com.example.dried_shrimp.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Product
// 1. 引用自動生成的 Binding 類別 (根據您的 layout 檔名 item_guesslike.xml 生成)
import com.example.dried_shrimp.databinding.ItemGuesslikeBinding
import com.example.dried_shrimp.ui.activities.ProductDetailActivity

class GuessLikeAdapter(
    private var productList: List<Product>
) : RecyclerView.Adapter<GuessLikeAdapter.ViewHolder>() {

    // 2. ViewHolder 改為接收 Binding 物件
    // 繼承時傳入 binding.root 給父類別
    inner class ViewHolder(val binding: ItemGuesslikeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 3. 使用 Binding 的 inflate 方法載入佈局
        val binding = ItemGuesslikeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        // 4. 使用 binding 存取元件
        // 注意：這裡的變數名稱會直接對應您 XML 裡的 ID (自動轉為駝峰式命名)
        // 例如 R.id.textView30 -> binding.textView30

        holder.binding.apply {
            textView30.text = product.name           // 原本的 tvName
            textView33.text = "$${product.price}"    // 原本的 tvPrice
            textView34.text = "庫存 ${product.stock}" // 原本的 tvStock

            // 處理圖片
            if (product.imageUrl.isNotEmpty()) {
                Glide.with(root.context) // 可以用 binding.root.context 取得 Context
                    .load(product.imageUrl)
                    .into(itemImage)     // 原本的 R.id.item_image -> itemImage
            } else {
                itemImage.setImageResource(R.drawable.shrimp_icon)
            }

            // 5. 點擊跳轉
            root.setOnClickListener {
                val context = root.context
                val intent = Intent(context, ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_DATA", product)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = productList.size

    fun updateData(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}