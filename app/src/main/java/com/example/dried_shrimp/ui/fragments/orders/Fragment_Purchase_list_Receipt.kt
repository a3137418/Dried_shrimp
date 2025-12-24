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

        // 2. 設定訂單列表8
        val currentUserId = auth.currentUser?.uid ?: ""

        // 🔥 修正這裡：明確指定 onActionClick，避免 lambda 跑錯位置
        adapter = OrderAdapter(
            orders = emptyList(),
            currentUserId = currentUserId,
            onActionClick = { order ->
                // 點擊按鈕的邏輯：買家點擊「確認收貨」
                confirmReceipt(order)
            }
            // onCancelClick 預設為 null，待收貨狀態不需要取消功能，所以不傳
        )
        // 請確認 XML 裡的 ID 是否為 recyclePurchaselistPendingReceipt 或類似名稱
        // 如果 XML 裡是 recycle_purchaselist_receipt，請自行調整
        binding?.recyclePurchaselistPendingReceipt?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@Fragment_Purchase_list_Receipt.adapter
        }
    }
    // 買家確認收貨的動作
    // ★★★ 關鍵修改：使用 Batch 寫入歷史資料 ★★★
    // 買家確認收貨：從「進行中」移除 -> 移動到「歷史紀錄」
    private fun confirmReceipt(order: Order) {
        val batch = db.batch()

        // 1. 鎖定「來源」：買家的進行中訂單 (原本的位置)
        val buyerSourceRef = db.collection("users").document(order.buyerId)
            .collection("orders").document(order.orderId)

        // 2. 鎖定「目的」：買家的歷史訂單 (新的位置)
        val buyerHistoryRef = db.collection("users").document(order.buyerId)
            .collection("history_orders").document(order.orderId)

        // 3. 全域訂單與賣家訂單 (這兩者通常只更新狀態，不一定需要移動，視您需求而定)
        val globalRef = db.collection("orders").document(order.orderId)
        val sellerRef = db.collection("users").document(order.sellerId)
            .collection("orders").document(order.orderId)

        // 4. 準備新資料 (狀態改為 COMPLETED)
        // ⚠️ hasReviewed 預設為 false 是正確的，因為剛收貨還沒評價
        val completedOrder = order.copy(
            status = "COMPLETED",
            timestamp = System.currentTimeMillis()
        )

        // --- 執行批次操作 ---

        // A. 【新增】到買家歷史紀錄
        batch.set(buyerHistoryRef, completedOrder)

        // B. 【刪除】買家進行中訂單 (🔥 這是讓它從「待收貨」列表消失的關鍵)
        batch.delete(buyerSourceRef)

        // C. 【更新】全域與賣家狀態
        batch.update(globalRef, "status", "COMPLETED")
        batch.update(sellerRef, "status", "COMPLETED")

        // 5. 提交交易
        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "訂單完成！已移至歷史紀錄", Toast.LENGTH_SHORT).show()
                // 成功後重新載入列表，介面上的該筆訂單就會消失
                loadOrders()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "操作失敗: ${e.message}", Toast.LENGTH_SHORT).show()
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