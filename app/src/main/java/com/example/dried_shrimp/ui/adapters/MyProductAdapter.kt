package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dried_shrimp.data.model.Product
// 引用剛剛建立的 XML Binding
import com.example.dried_shrimp.databinding.ItemMyProductBinding

class MyProductAdapter(
    private var productList: List<Product>,
    // 這裡定義了兩個參數，分別是點擊「編輯」和「變更狀態」時要執行的程式碼
    private val onEditClick: (Product) -> Unit,
    private val onStatusChangeClick: (Product) -> Unit
) : RecyclerView.Adapter<MyProductAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMyProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMyProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.binding.apply {
            tvName.text = product.name
            tvPrice.text = "$${product.price}"
            tvStock.text = "庫存: ${product.stock}"

            if (product.status == "OFF_SHELF") {
                // 在未上架列表，這個按鈕顯示為「刪除」
                btnChangeStatus.text = "刪除"
                // 建議設個紅色文字或背景示警
                // btnChangeStatus.setTextColor(Color.RED)
            } else {
                btnChangeStatus.text = "下架"
            }

            if (product.imageUrl.isNotEmpty()) {
                Glide.with(root.context).load(product.imageUrl).into(imgProduct)
            }

            // 設定點擊事件
            btnEdit.setOnClickListener { onEditClick(product) }
            btnChangeStatus.setOnClickListener { onStatusChangeClick(product) }
        }
    }

    override fun getItemCount() = productList.size

    fun updateData(newList: List<Product>) {
        productList = newList
        notifyDataSetChanged()
    }
}