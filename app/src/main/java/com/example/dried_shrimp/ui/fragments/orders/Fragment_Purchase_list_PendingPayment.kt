package com.example.dried_shrimp.ui.fragments.orders

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentPurchaselistPendingPaymentBinding
import com.example.dried_shrimp.ui.activities.PaymentActivity
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.example.dried_shrimp.ui.adapters.OrderAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.example.dried_shrimp.data.model.Order

class Fragment_Purchase_list_PendingPayment: Fragment() {
    private var binding: FragmentPurchaselistPendingPaymentBinding ?=null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var guesslike_Adapter: GuessLikeAdapter
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: OrderAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPurchaselistPendingPaymentBinding.inflate(inflater,container,false)
        // 初始化 Adapter，點擊按鈕時跳轉到付款頁面
        val currentUserId = auth.currentUser?.uid ?: ""
        adapter = OrderAdapter(emptyList(),currentUserId) { order ->
            val intent = Intent(context, PaymentActivity::class.java)
            intent.putExtra("ORDER_DATA", order)
            startActivity(intent)
        }
        binding?.recyclePurchaselistPendingPayment?.layoutManager = LinearLayoutManager(context)
        binding?.recyclePurchaselistPendingPayment?.adapter = adapter
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


        // ★★★ 關鍵：綁定 ID ★★★
        // 請將 binding?.後面的名字 改成您第一步在 XML 裡看到的 ID
        // 例如：recycle_order_completed 或 recyclePurchaselistOrderCompleted
        binding?.recyclePurchaselistPendingPayment?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapter
        }

    }
    private fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return

        // ★ 修改：從 users -> {uid} -> orders 讀取
        // 不需要再寫 .whereEqualTo("buyerId", userId) 了，因為已經在你的資料夾下了
        db.collection("users").document(userId).collection("orders")
            .whereEqualTo("status", "PENDING") // 只要篩選狀態
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