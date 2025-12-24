package com.example.dried_shrimp.ui.fragments.orders

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentPurchaselistPendingPaymentBinding
import com.example.dried_shrimp.ui.activities.PaymentActivity
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.example.dried_shrimp.ui.adapters.OrderAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Fragment_Purchase_list_PendingPayment : Fragment() {

    private var binding: FragmentPurchaselistPendingPaymentBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var guesslike_Adapter: GuessLikeAdapter
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPurchaselistPendingPaymentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化列表設定 (包含 adapter 初始化)
        setupRecyclerViews()

        // 載入資料
        loadOrders()
    }

    override fun onResume() {
        super.onResume()
        loadOrders() // 確保付款或取消回來後，列表會更新
    }

    private fun setupRecyclerViews() {
        // 1. 設定猜你喜歡 (Guess Like)
        guesslike_Adapter = GuessLikeAdapter(emptyList())
        binding?.sectionGuesslike?.myRecycleLike?.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = guesslike_Adapter
        }
        loadAllProducts()

        // 2. 設定訂單列表 (Order List)
        // 🔥 修正 1: 這裡必須獲取 currentUserId，解決 Unresolved reference 錯誤
        val currentUserId = auth.currentUser?.uid ?: ""

        // 🔥 修正 2: 這裡明確傳入參數，解決 No value passed 錯誤
        adapter = OrderAdapter(
            orders = emptyList(),
            currentUserId = currentUserId,
            onActionClick = { order ->
                // 按下「去付款」的邏輯
                val intent = Intent(context, PaymentActivity::class.java)
                intent.putExtra("ORDER_DATA", order)
                startActivity(intent)
            },
            onCancelClick = { order ->
                // 按下「取消訂單」的邏輯
                showCancelDialog(order)
            }
        )

        binding?.recyclePurchaselistPendingPayment?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@Fragment_Purchase_list_PendingPayment.adapter
        }
    }

    // --- 載入訂單資料 ---
    private fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("orders")
            .whereEqualTo("status", "PENDING")
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Order::class.java)
                adapter.updateData(list.sortedByDescending { it.timestamp })
            }
            .addOnFailureListener {
                Toast.makeText(context, "無法載入訂單: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- 載入猜你喜歡 ---
    private fun loadAllProducts() {
        db.collection("products")
            .whereEqualTo("status", "ON_SHELF")
            .limit(10)
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                if (!result.isEmpty) {
                    val productList = result.toObjects(Product::class.java)
                    guesslike_Adapter.updateData(productList)
                }
            }
    }

    // --- 取消訂單邏輯區 ---
    private fun showCancelDialog(order: Order) {
        if (context == null) return

        AlertDialog.Builder(requireContext())
            .setTitle("取消訂單")
            .setMessage("確定要取消這筆訂單嗎？此操作無法復原。")
            .setPositiveButton("確定取消") { _, _ ->
                deleteOrder(order)
            }
            .setNegativeButton("再想想", null)
            .show()
    }

    private fun deleteOrder(order: Order) {
        val batch = db.batch()

        // 1. 刪除全域訂單
        val globalRef = db.collection("orders").document(order.orderId)
        batch.delete(globalRef)

        // 2. 刪除買家訂單
        val buyerRef = db.collection("users").document(order.buyerId)
            .collection("orders").document(order.orderId)
        batch.delete(buyerRef)

        // 3. 刪除賣家訂單 (如果有的話)
        if (order.sellerId.isNotEmpty()) {
            val sellerRef = db.collection("users").document(order.sellerId)
                .collection("orders").document(order.orderId)
            batch.delete(sellerRef)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "訂單已取消", Toast.LENGTH_SHORT).show()
                loadOrders() // 重新整理列表，該訂單會消失
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "取消失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}