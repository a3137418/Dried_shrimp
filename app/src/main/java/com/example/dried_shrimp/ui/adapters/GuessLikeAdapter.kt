package com.example.dried_shrimp.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.ItemGuesslikeBinding
import com.example.dried_shrimp.ui.activities.ProductDetailActivity

class GuessLikeAdapter(
    private var productList: List<Product>
) : RecyclerView.Adapter<GuessLikeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemGuesslikeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGuesslikeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        android.util.Log.e("DebugAdapter", "商品: ${product.name}, 分數: ${product.rating}, 評價數: ${product.reviewCount}")
        holder.binding.apply {
            textView30.text = product.name           // 商品名稱
            textView33.text = "$${product.price}"    // 價格

            // ==========================================
            // 🔥 新增：綁定星星分數 (textView31)
            // ==========================================
            if (product.rating > 0) {
                // 有分數：保留一位小數，例如 "4.5"
                textView31.text = String.format("%.1f", product.rating)
            } else {
                // 沒分數：顯示 "New" 或 "0.0"
                textView31.text = "New"
            }

            // ==========================================
            // 🔥 修改：右下角顯示資訊 (textView34)
            // ==========================================
            // 您原本是顯示庫存，建議配合星星改成顯示「評價數量」
            // 如果想改回庫存，請取消註解下面那行

            textView34.text = "評價(${product.reviewCount})"
            // textView34.text = "庫存 ${product.stock}"

            // 處理圖片
            if (product.imageUrl.isNotEmpty()) {
                Glide.with(root.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.shrimp_icon) // 建議加個預設圖防止閃爍
                    .into(itemImage)
            } else {
                itemImage.setImageResource(R.drawable.shrimp_icon)
            }

            // 點擊跳轉
            root.setOnClickListener {
                val context = root.context
                val intent = Intent(context, ProductDetailActivity::class.java)
                intent.putExtra("PRODUCT_DATA", product)
                // 確保這裡也有傳 PRODUCT_ID (雖然傳了 PRODUCT_DATA 物件通常就有 id)
                intent.putExtra("PRODUCT_ID", product.id)
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