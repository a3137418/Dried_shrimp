package com.example.dried_shrimp.ui.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.ui.activities.ProductDetailActivity

class HomeProductAdapter(
    private var productList: List<Product>
) : RecyclerView.Adapter<HomeProductAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgProduct: ImageView = view.findViewById(R.id.item_image)   // 圖片
        val tvName: TextView = view.findViewById(R.id.textView30)        // 名稱
        val tvPrice: TextView = view.findViewById(R.id.textView33)       // 價格

        // 🔥 修改 1: 這是顯示 "評價數量" 或 "庫存" 的欄位
        val tvInfo: TextView = view.findViewById(R.id.textView34)

        // 🔥 修改 2: 這是顯示 "星星分數" 的欄位 (原本漏掉這個！)
        val tvRating: TextView = view.findViewById(R.id.textView31)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guesslike, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        // 1. 設定文字
        holder.tvName.text = product.name
        holder.tvPrice.text = "$${product.price}"

        // 🔥 修改 3: 設定星星分數邏輯
        if (product.rating > 0) {
            // 有分數：保留一位小數，例如 "4.5"
            holder.tvRating.text = String.format("%.1f", product.rating)
        } else {
            // 沒分數：顯示 New
            holder.tvRating.text = "New"
        }

        // 🔥 修改 4: 設定右下角資訊 (改成顯示評價數，跟星星比較搭)
        // 如果您比較想顯示庫存，可以把下面這行改成: "庫存 ${product.stock}"
        holder.tvInfo.text = "評價(${product.reviewCount})"

        // 2. 設定圖片
        if (product.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                // 建議加個 placeholder 避免載入時閃爍
                .placeholder(R.drawable.shrimp_icon)
                .into(holder.imgProduct)
        } else {
            holder.imgProduct.setImageResource(R.drawable.shrimp_icon)
        }

        // 3. 點擊事件
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailActivity::class.java)

            // 傳遞整包資料
            intent.putExtra("PRODUCT_DATA", product)
            // 🔥 補上這行：明確傳遞 ID，確保詳情頁能順利抓到評價
            intent.putExtra("PRODUCT_ID", product.id)

            context.startActivity(intent)
        }
    }

    override fun getItemCount() = productList.size

    fun updateData(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}