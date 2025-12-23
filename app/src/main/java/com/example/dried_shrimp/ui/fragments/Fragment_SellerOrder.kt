package com.example.dried_shrimp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.databinding.FragmentSellerOrderBinding // 自動生成的 Binding 類別
import com.example.dried_shrimp.ui.adapters.SellerOrderAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Fragment_SellerOrder : Fragment() {

    // 🔹 View Binding 宣告
    private var _binding: FragmentSellerOrderBinding? = null
    private val binding get() = _binding!!

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
    ): View {
        // 🔹 初始化 Binding
        _binding = FragmentSellerOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔹 修正：處理底部導航列遮擋問題 (解決「下面都被遮到了」)
        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutTotalSummary) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // 為合計欄位增加底部的系統導航列高度
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        loadSellerOrders()
    }

    override fun onResume() {
        super.onResume()
        loadSellerOrders()
    }

    private fun setupRecyclerView() {
        adapter = SellerOrderAdapter(requireContext(), emptyList()) { order ->
            if (statusType == "TO_SHIP") {
                showShipDialog(order)
            }
        }
        // 🔹 使用 binding 存取元件
        binding.rvOrderList.layoutManager = LinearLayoutManager(context)
        binding.rvOrderList.adapter = adapter
    }

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

    private fun shipOrder(order: Order) {
        val batch = db.batch()

        val globalRef = db.collection("orders").document(order.orderId)
        batch.update(globalRef, "status", "SHIPPED")

        val buyerRef = db.collection("users").document(order.buyerId)
            .collection("orders").document(order.orderId)
        batch.update(buyerRef, "status", "SHIPPED")

        val sellerRef = db.collection("users").document(order.sellerId)
            .collection("orders").document(order.orderId)
        batch.update(sellerRef, "status", "SHIPPED")

        batch.commit()
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(context, "出貨成功！", Toast.LENGTH_SHORT).show()
                    loadSellerOrders()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(context, "出貨失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loadSellerOrders() {
        val myUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(myUserId).collection("orders")
            .whereEqualTo("status", statusType)
            .get()
            .addOnSuccessListener { result ->
                if (_binding == null) return@addOnSuccessListener

                val list = result.toObjects(Order::class.java)
                val sortedList = list.sortedByDescending { it.timestamp }
                adapter.updateData(sortedList)

                // 🔹 合計金額計算邏輯
                if (statusType == "COMPLETED") {
                    val total = list.sumOf { it.totalPrice } // 使用 Order.kt 裡的 totalPrice
                    binding.tvCompletedTotalAmount.text = "$$total"
                    binding.layoutTotalSummary.visibility = View.VISIBLE
                } else {
                    binding.layoutTotalSummary.visibility = View.GONE
                }

                // 🔹 更新空狀態顯示
                if (sortedList.isEmpty()) {
                    binding.tvEmptyOrders.visibility = View.VISIBLE
                    binding.rvOrderList.visibility = View.GONE
                } else {
                    binding.tvEmptyOrders.visibility = View.GONE
                    binding.rvOrderList.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                if (_binding != null) {
                    binding.tvEmptyOrders.text = "讀取失敗: ${e.message}"
                    binding.tvEmptyOrders.visibility = View.VISIBLE
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 🔹 釋放 Binding 避免記憶體洩漏
        _binding = null
    }
}