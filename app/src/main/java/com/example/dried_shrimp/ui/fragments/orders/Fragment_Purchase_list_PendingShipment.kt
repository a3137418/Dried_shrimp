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
import com.example.dried_shrimp.databinding.FragmentPurchaselistPendingShipmentBinding
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.example.dried_shrimp.ui.adapters.OrderAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Fragment_Purchase_list_PendingShipment: Fragment() {
    private var binding: FragmentPurchaselistPendingShipmentBinding ?=null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: OrderAdapter
    private lateinit var guesslike_Adapter: GuessLikeAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPurchaselistPendingShipmentBinding.inflate(inflater,container,false)
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
        // 2. 訂單列表設定 (★ 補上這裡)
        val currentUserId = auth.currentUser?.uid ?: ""

        // 🔥 修正這裡：補上 onActionClick 參數
        adapter = OrderAdapter(
            orders = emptyList(),
            currentUserId = currentUserId,
            onActionClick = { _ ->
                // 在「待出貨」狀態，買家按鈕通常是鎖住的 (isEnabled = false)
                // 所以這裡不會被觸發，留空即可
            }
            // onCancelClick 預設為 null，待出貨通常不給直接取消，所以不傳
        )
        binding?.recyclePurchaselistPendingShipment?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@Fragment_Purchase_list_PendingShipment.adapter
        }
    }
    private fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return

        // ★ 修改：從 users -> {uid} -> orders 讀取
        // 不需要再寫 .whereEqualTo("buyerId", userId) 了，因為已經在你的資料夾下了
        db.collection("users").document(userId).collection("orders")
            .whereEqualTo("status", "TO_SHIP") // 只要篩選狀態
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