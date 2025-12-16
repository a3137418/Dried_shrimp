package com.example.dried_shrimp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.ui.adapters.SellerOrderAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Fragment_SellerOrder : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: SellerOrderAdapter
    private var statusType: String = ""

    companion object {
        fun newInstance(status: String): Fragment_SellerOrder {
            val fragment = Fragment_SellerOrder()
            val args = Bundle()
            args.putString("STATUS_TYPE", status)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusType = arguments?.getString("STATUS_TYPE") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_seller_order, container, false)

        // 綁定 ID
        recyclerView = view.findViewById(R.id.rv_order_list)
        tvEmpty = view.findViewById(R.id.tv_empty_orders)

        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadSellerOrders()
    }

    private fun setupRecyclerView() {
        // ★★★ 修改重點 1：Adapter 的點擊事件邏輯 ★★★
        // 假設您的 SellerOrderAdapter 最後一個參數是 (Order) -> Unit
        adapter = SellerOrderAdapter(requireContext(), emptyList()) { order ->
            // 當賣家點擊按鈕時執行
            if (statusType == "TO_SHIP") {
                showShipDialog(order)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    // ★★★ 修改重點 2：新增確認視窗 ★★★
    private fun showShipDialog(order: Order) {
        AlertDialog.Builder(requireContext())
            .setTitle("確認出貨")
            .setMessage("確定要將訂單 ${order.orderId} 標記為已出貨嗎？")
            .setPositiveButton("確認出貨") { _, _ ->
                shipOrder(order)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // ★★★ 修改重點 3：新增出貨邏輯 (同時更新三個路徑) ★★★
    private fun shipOrder(order: Order) {
        val batch = db.batch()

        // 1. 更新全域訂單
        val globalRef = db.collection("orders").document(order.orderId)
        batch.update(globalRef, "status", "SHIPPED")

        // 2. 更新買家訂單 (users -> buyer -> orders)
        val buyerRef = db.collection("users").document(order.buyerId)
            .collection("orders").document(order.orderId)
        batch.update(buyerRef, "status", "SHIPPED")

        // 3. 更新賣家訂單 (users -> seller -> orders)
        val sellerRef = db.collection("users").document(order.sellerId)
            .collection("orders").document(order.orderId)
        batch.update(sellerRef, "status", "SHIPPED")

        // 執行更新
        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "出貨成功！", Toast.LENGTH_SHORT).show()
                loadSellerOrders() // 更新列表
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "出貨失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSellerOrders() {
        val myUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(myUserId).collection("orders")
            .whereEqualTo("status", statusType)
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Order::class.java)
                val sortedList = list.sortedByDescending { it.timestamp }
                adapter.updateData(sortedList)

                if (sortedList.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                tvEmpty.text = "讀取失敗: ${it.message}"
                tvEmpty.visibility = View.VISIBLE
            }
    }
}