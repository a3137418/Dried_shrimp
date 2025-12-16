package com.example.dried_shrimp.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Order

class SellerOrderAdapter(
    private val context: Context,
    private var orderList: List<Order>,
    // ★★★ 修正點：回調函式改為傳回 Order 物件，讓 Fragment 處理邏輯 ★★★
    private val onActionClick: (Order) -> Unit
) : RecyclerView.Adapter<SellerOrderAdapter.ViewHolder>() {

    fun updateData(newList: List<Order>) {
        orderList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_seller_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]

        holder.tvOrderId.text = "訂單: ${order.orderId}"
        holder.tvBuyerName.text = "買家: ${order.receiverName}" // 建議顯示收件人姓名，比較直觀
        holder.tvTotal.text = "金額: $${order.totalPrice}"

        val summary = if (order.items.isNotEmpty()) {
            "${order.items[0].name} 等 ${order.items.size} 件商品"
        } else "無商品"
        holder.tvItems.text = summary

        // --- 按鈕邏輯 ---
        when (order.status) {
            "TO_SHIP" -> {
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "安排出貨"
                holder.btnAction.isEnabled = true

                // ★ 當點擊時，將這張 Order 傳回給 Fragment 處理
                holder.btnAction.setOnClickListener {
                    onActionClick(order)
                }
            }
            "SHIPPED" -> {
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "等待收貨"
                holder.btnAction.isEnabled = false // 只能看不能按
            }
            "COMPLETED" -> {
                holder.btnAction.visibility = View.VISIBLE
                holder.btnAction.text = "訂單完成"
                holder.btnAction.isEnabled = false
            }
            else -> {
                holder.btnAction.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = orderList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tv_seller_order_id)
        val tvBuyerName: TextView = itemView.findViewById(R.id.tv_buyer_name)
        val tvItems: TextView = itemView.findViewById(R.id.tv_order_items)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        val btnAction: Button = itemView.findViewById(R.id.btn_seller_action)
    }
}