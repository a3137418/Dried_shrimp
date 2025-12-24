package com.example.dried_shrimp.ui.fragments.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentPurchaselistReturnthegoodsBinding
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.example.dried_shrimp.ui.adapters.OrderAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.example.dried_shrimp.data.model.Order

class Fragment_Purchase_list_Returnthegoods: Fragment() {
    private var binding: FragmentPurchaselistReturnthegoodsBinding ?=null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // ★ 補上 auth
    private lateinit var adapter: OrderAdapter    // ★ 補上 adapter
    private lateinit var guesslike_Adapter: GuessLikeAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPurchaselistReturnthegoodsBinding.inflate(inflater,container,false)
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
        loadOrders() // ★ 補上載入資料
    }
    private fun setupRecyclerViews() {
        guesslike_Adapter = GuessLikeAdapter(emptyList())
        binding?.sectionGuesslike?.myRecycleLike?.layoutManager = GridLayoutManager(requireContext(),2)
        binding?.sectionGuesslike?.myRecycleLike?.adapter = guesslike_Adapter
        loadAllProducts()
        // 2. 訂單列表 (★ 補上)
        val currentUserId = auth.currentUser?.uid ?: ""
        adapter = OrderAdapter(
            orders = emptyList(),
            currentUserId = currentUserId,
            onActionClick = { _ ->
            }
            // onCancelClick 預設為 null，待收貨狀態不需要取消功能，所以不傳
        )

        binding?.recyclePurchaselistReturnthegoods?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@Fragment_Purchase_list_Returnthegoods.adapter
        }
    }
    // ★ 補上讀取訂單邏輯
    private fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return

        // ★ 修改：從 users -> {uid} -> orders 讀取
        // 不需要再寫 .whereEqualTo("buyerId", userId) 了，因為已經在你的資料夾下了
        db.collection("users").document(userId).collection("orders")
            .whereEqualTo("status", "RETURNED") // 只要篩選狀態
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