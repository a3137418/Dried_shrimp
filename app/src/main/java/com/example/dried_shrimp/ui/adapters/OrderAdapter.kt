package com.example.dried_shrimp.ui.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.databinding.ItemOrderBinding
import com.example.dried_shrimp.ui.activities.PaymentActivity
import com.example.dried_shrimp.ui.activities.ReviewActivity
import java.text.SimpleDateFormat
import java.util.Locale

class OrderAdapter(
    private var orders: List<Order>,
    private val currentUserId: String, // 傳入當前使用者 ID
    private val onActionClick: (Order) -> Unit,
    private val onCancelClick: ((Order) -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("OrderAdapter", "onCreateViewHolder 被調用")
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        Log.d("OrderAdapter", "onBindViewHolder 位置: $position")
        holder.binding.apply {
            // 判斷是否為賣家 (檢查商品列表中的 sellerId)
            val isSeller = order.items.any { it.sellerId == currentUserId }

            // 1. 設定文字資訊 (對應 XML 中的 ID)
            tvOrderId.text = "訂單編號: ${order.orderId}"
            tvOrderTotal.text = "$${order.totalPrice}"

            if (order.items.isNotEmpty()) {
                tvOrderItemsSummary.text = "${order.items[0].name} 等 ${order.items.size} 件商品"
            } else {
                tvOrderItemsSummary.text = "無商品資訊"
            }

            // 2. 設定狀態與按鈕邏輯
            when (order.status) {
                "PENDING" -> {
                    tvOrderStatus.text = "狀態: 待付款"
                    btnAction.visibility = if (isSeller) View.GONE else View.VISIBLE
                    btnAction.text = "去付款"
                    btnAction.isEnabled = true
                    btnAction.setOnClickListener {
                        val context = holder.itemView.context
                        val intent = Intent(context, PaymentActivity::class.java)
                        intent.putExtra("ORDER_DATA", order)
                        context.startActivity(intent)
                    }
                    // 🔥 新增：如果是買家且在待付款狀態，顯示取消按鈕
                    if (!isSeller) {
                        btnCancel.visibility = View.VISIBLE
                        btnCancel.setOnClickListener {
                            onCancelClick?.invoke(order)
                        }
                    }
                }
                "TO_SHIP" -> {
                    tvOrderStatus.text = "狀態: 待出貨"
                    if (isSeller) {
                        btnAction.visibility = View.VISIBLE
                        btnAction.text = "安排出貨"
                        btnAction.isEnabled = true
                    } else {
                        btnAction.visibility = View.VISIBLE
                        btnAction.text = "等待賣家出貨"
                        btnAction.isEnabled = false
                    }
                }
                "SHIPPED" -> {
                    tvOrderStatus.text = "狀態: 待收貨"
                    if (isSeller) {
                        btnAction.visibility = View.VISIBLE
                        btnAction.text = "等待買家收貨"
                        btnAction.isEnabled = false
                    } else {
                        btnAction.visibility = View.VISIBLE
                        btnAction.text = "確認收貨"
                        btnAction.isEnabled = true

                        // 🔥 修改這裡！讓按鈕點擊時觸發 onActionClick
                        // 這樣 Fragment 裡的 confirmReceipt 才會被執行
                        btnAction.setOnClickListener {
                            onActionClick(order)
                        }
                    }
                }
                "COMPLETED" -> {
                    tvOrderStatus.text = "已完成"
                    btnAction.visibility = android.view.View.VISIBLE

                    // 🔥 這裡決定顯示什麼按鈕
                    if (order.hasReviewed) {
                        btnAction.text = "已評價"
                        btnAction.isEnabled = false // 或跳轉到查看頁面
                    } else {
                        btnAction.text = "去評價"
                        btnAction.isEnabled = true
                        // 🔥 點擊跳轉到評價頁面
                        btnAction.setOnClickListener {
                            val context = holder.itemView.context
                            val intent = Intent(context, ReviewActivity::class.java)
                            intent.putExtra("ORDER_DATA", order)
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        Log.d("OrderAdapter", "getItemCount: ${orders.size}")
        return orders.size
    }

    fun updateData(newOrders: List<Order>) {
        Log.d("OrderAdapter", "updateData 被調用，新訂單數量: ${newOrders.size}")
        orders = newOrders
        notifyDataSetChanged()
        Log.d("OrderAdapter", "notifyDataSetChanged 已調用")
    }
}