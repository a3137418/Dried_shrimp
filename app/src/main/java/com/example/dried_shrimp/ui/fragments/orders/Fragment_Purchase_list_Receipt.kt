package com.example.dried_shrimp.ui.fragments.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentPurchaselistPendingReceiptBinding
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.example.dried_shrimp.ui.adapters.OrderAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Fragment_Purchase_list_Receipt: Fragment() {
    private var binding: FragmentPurchaselistPendingReceiptBinding ?=null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var guesslike_Adapter: GuessLikeAdapter
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: OrderAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPurchaselistPendingReceiptBinding.inflate(inflater,container,false)
        val view =binding?.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        loadOrders()
    }
    override fun onResume() {
        super.onResume()
        loadOrders() // 每次回來 (例如付完款回來) 都要重新整理
    }

    private fun setupRecyclerViews() {
        guesslike_Adapter = GuessLikeAdapter(emptyList())
        binding?.sectionGuesslike?.myRecycleLike?.layoutManager = GridLayoutManager(requireContext(),2)
        binding?.sectionGuesslike?.myRecycleLike?.adapter = guesslike_Adapter
        loadAllProducts()

        // 2. 設定訂單列表
        val currentUserId = auth.currentUser?.uid ?: ""

        adapter = OrderAdapter(emptyList(), currentUserId) { order ->
            // 點擊按鈕的邏輯：買家點擊「確認收貨」
            confirmReceipt(order)
        }
        // 請確認 XML 裡的 ID 是否為 recyclePurchaselistPendingReceipt 或類似名稱
        // 如果 XML 裡是 recycle_purchaselist_receipt，請自行調整
        binding?.recyclePurchaselistPendingReceipt?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@Fragment_Purchase_list_Receipt.adapter
        }
    }
    // 買家確認收貨的動作
    // ★★★ 關鍵修改：使用 Batch 寫入歷史資料 ★★★
    private fun confirmReceipt(order: Order) {
        val batch = db.batch()

        // 1. 全域
        val globalRef = db.collection("orders").document(order.orderId)
        batch.update(globalRef, "status", "COMPLETED")

        // 2. 買家
        val buyerRef = db.collection("users").document(order.buyerId)
            .collection("orders").document(order.orderId)
        batch.update(buyerRef, "status", "COMPLETED")

        // 3. 賣家 (★ 重要：也要通知賣家訂單完成了)
        val sellerRef = db.collection("users").document(order.sellerId)
            .collection("orders").document(order.orderId)
        batch.update(sellerRef, "status", "COMPLETED")

        // 4. 歷史紀錄 (原本的功能)
        val userHistoryRef = db.collection("users").document(order.buyerId)
            .collection("history_orders").document(order.orderId)
        batch.set(userHistoryRef, order.copy(status = "COMPLETED"))

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "訂單完成！", Toast.LENGTH_SHORT).show()
                loadOrders()
            }
    }
    private fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return

        // ★ 修改：從 users -> {uid} -> orders 讀取
        // 不需要再寫 .whereEqualTo("buyerId", userId) 了，因為已經在你的資料夾下了
        db.collection("users").document(userId).collection("orders")
            .whereEqualTo("status", "SHIPPED") // 只要篩選狀態
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Order::class.java)
                adapter.updateData(list.sortedByDescending { it.timestamp })
            }
    }


    private fun loadAllProducts() {
        db.collection("products")
            .whereEqualTo("status", "ON_SHELF") // ★ 加入這行，確保不推薦下架商品
            .limit(10) // 通常猜你喜歡會限制數量
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                // 4. 明確指定 result: QuerySnapshot 解決推斷錯誤
                if (!result.isEmpty) {
                    // 5. 確保 Product 已 import，這樣 ::class.java 就不會報錯
                    val productList = result.toObjects(Product::class.java)
                    guesslike_Adapter.updateData(productList)
                }
            }
            .addOnFailureListener { e: Exception ->
                // 6. 明確指定 e: Exception
                if (isAdded) {
                    Toast.makeText(requireContext(), "載入失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}