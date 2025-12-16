package com.example.dried_shrimp.ui.fragments.orders

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentPurchaselistOrderCompletedBinding
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.example.dried_shrimp.ui.adapters.OrderAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.ui.activities.PaymentActivity

class Fragment_Purchase_list_OrderCompleted: Fragment() {
    private var binding: FragmentPurchaselistOrderCompletedBinding ?=null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var guesslike_Adapter: GuessLikeAdapter
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("OrderCompleted", "onCreateView 被調用")
        binding = FragmentPurchaselistOrderCompletedBinding.inflate(inflater,container,false)
        val currentUserId = auth.currentUser?.uid ?: ""
        Log.d("OrderCompleted", "當前用戶 ID: $currentUserId")

        adapter = OrderAdapter(emptyList(), currentUserId) { order ->
            val intent = Intent(context, PaymentActivity::class.java)
            intent.putExtra("ORDER_DATA", order)
            startActivity(intent)
        }
        val view = binding?.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("OrderCompleted", "onViewCreated 被調用")

        // 初始化 RecyclerViews
        setupRecyclerViews()

        // 載入資料
        loadOrders()      // 載入已完成訂單
        loadAllProducts() // 載入猜你喜歡
    }

    override fun onResume() {
        super.onResume()
        Log.d("OrderCompleted", "onResume 被調用")
        loadOrders()
    }

    private fun setupRecyclerViews() {
        Log.d("OrderCompleted", "setupRecyclerViews 開始")

        // 1. 設定「猜你喜歡」
        guesslike_Adapter = GuessLikeAdapter(emptyList())
        binding?.sectionGuesslike?.myRecycleLike?.layoutManager = GridLayoutManager(requireContext(), 2)
        binding?.sectionGuesslike?.myRecycleLike?.adapter = guesslike_Adapter
        Log.d("OrderCompleted", "猜你喜歡 RecyclerView 設定完成")

        // 2. 設定「已完成訂單列表」
        val currentUserId = auth.currentUser?.uid ?: ""
        Log.d("OrderCompleted", "設定訂單列表，用戶 ID: $currentUserId")

        binding?.recyclePurchaselistOrderCompleted?.apply {
            Log.d("OrderCompleted", "RecyclerView 不為 null，開始設定")
            layoutManager = LinearLayoutManager(context)
            adapter = this@Fragment_Purchase_list_OrderCompleted.adapter
            Log.d("OrderCompleted", "RecyclerView 設定完成")
        } ?: Log.e("OrderCompleted", "❌ RecyclerView 為 null！")
    }

    private fun loadOrders() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Log.e("OrderCompleted", "❌ 用戶未登入")
            return
        }

        Log.d("OrderCompleted", "開始載入訂單，用戶 ID: $userId")

        db.collection("users").document(userId).collection("history_orders")
            .whereEqualTo("status", "COMPLETED")
            .get()
            .addOnSuccessListener { result ->
                Log.d("OrderCompleted", "✅ 查詢成功，文檔數量: ${result.size()}")

                if (result.isEmpty) {
                    Log.w("OrderCompleted", "⚠️ 沒有已完成的訂單")
                    Toast.makeText(context, "目前沒有已完成的訂單", Toast.LENGTH_SHORT).show()
                } else {
                    val list = result.toObjects(Order::class.java)
                    Log.d("OrderCompleted", "轉換後的訂單數量: ${list.size}")

                    // 印出第一筆訂單資訊
                    if (list.isNotEmpty()) {
                        Log.d("OrderCompleted", "第一筆訂單: ${list[0]}")
                    }

                    val sortedList = list.sortedByDescending { it.timestamp }
                    Log.d("OrderCompleted", "排序後準備更新 Adapter")
                    adapter.updateData(sortedList)
                    Log.d("OrderCompleted", "Adapter 已更新")
                }
            }
            .addOnFailureListener { e ->
                Log.e("OrderCompleted", "❌ 載入訂單失敗", e)
                Toast.makeText(context, "載入失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadAllProducts() {
        Log.d("OrderCompleted", "開始載入商品")

        db.collection("products")
            .whereEqualTo("status", "ON_SHELF")
            .limit(10)
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                Log.d("OrderCompleted", "✅ 商品查詢成功，數量: ${result.size()}")

                if (!result.isEmpty) {
                    val productList = result.toObjects(Product::class.java)
                    guesslike_Adapter.updateData(productList)
                    Log.d("OrderCompleted", "商品 Adapter 已更新")
                }
            }
            .addOnFailureListener { e: Exception ->
                Log.e("OrderCompleted", "❌ 載入商品失敗", e)
                if (isAdded) {
                    Toast.makeText(requireContext(), "載入失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("OrderCompleted", "onDestroyView 被調用")
        binding = null
    }
}