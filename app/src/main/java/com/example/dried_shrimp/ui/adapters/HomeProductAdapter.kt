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
        // ⭐ 這裡必須跟 item_guesslike.xml 的 ID 一模一樣
        val imgProduct: ImageView = view.findViewById(R.id.item_image)   // 圖片
        val tvName: TextView = view.findViewById(R.id.textView30)        // 名稱
        val tvPrice: TextView = view.findViewById(R.id.textView33)       // 價格
        val tvStock: TextView = view.findViewById(R.id.textView34)       // 庫存/已售

        // ❌ 刪除 btn_edit 和 btn_delete，因為 item_guesslike.xml 裡面沒有這些按鈕
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // ⭐ 指定載入 item_guesslike
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_guesslike, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        // 1. 設定文字
        holder.tvName.text = product.name
        holder.tvPrice.text = "$${product.price}"
        holder.tvStock.text = "庫存 ${product.stock}" // 顯示在庫存那一格

        // 2. 設定圖片
        if (product.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(product.imageUrl)
                .into(holder.imgProduct)
        } else {
            holder.imgProduct.setImageResource(R.drawable.shrimp_icon)
        }

        // 3. 點擊事件 (不用隱藏按鈕了，因為根本沒有按鈕)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ProductDetailActivity::class.java)
            intent.putExtra("PRODUCT_DATA", product)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = productList.size

    fun updateData(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}