package com.example.dried_shrimp.ui.fragments.myproducts

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentMyproductsOntheshelfBinding
import com.example.dried_shrimp.ui.activities.AddProductsActivity
import com.example.dried_shrimp.ui.adapters.MyProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Fragment_myproducts_ontheshelf : Fragment() {

    private var _binding: FragmentMyproductsOntheshelfBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 這裡使用 GuessLikeAdapter 作為範例，您之後可以換成 MyProductsAdapter
    private lateinit var adapter: MyProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyproductsOntheshelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化 RecyclerView
        setupRecyclerView()

    }
    override fun onResume() {
        super.onResume()
        loadProducts()
    }
    private fun setupRecyclerView() {
        // 這裡填入 Adapter 需要的參數
        adapter = MyProductAdapter(
            productList = emptyList(), // 1. 初始空清單

            // 2. 當使用者點擊「編輯」時
            onEditClick = { product ->
                val intent = Intent(context, AddProductsActivity::class.java)
                intent.putExtra("EDIT_PRODUCT", product)
                startActivity(intent)
            },

            // 3. 當使用者點擊「下架」時
            onStatusChangeClick = { product ->
                // 執行下架動作：將狀態改為 OFF_SHELF
                updateProductStatus(product.id, "OFF_SHELF")
            }
        )

        binding.recycleOntheshelf.layoutManager = LinearLayoutManager(context)
        binding.recycleOntheshelf.adapter = adapter

        // 設定完 Adapter 後再載入資料
        loadProducts()
    }
    // 新增一個 helper 方法來處理狀態更新
    private fun updateProductStatus(productId: String, newStatus: String) {
        db.collection("products").document(productId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(context, "商品已下架", Toast.LENGTH_SHORT).show()
                loadProducts() // 重新載入列表，讓該商品從「架上」消失
            }
            .addOnFailureListener {
                Toast.makeText(context, "操作失敗", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProducts() {
        val userId = auth.currentUser?.uid
        if (userId == null) return

        // 3. 關鍵查詢：賣家是自己 + 狀態是「架上」
        db.collection("products")
            .whereEqualTo("sellerId", userId)
            .whereEqualTo("status", "ON_SHELF") // 請確保資料庫欄位是 status
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Product::class.java)
                // 加入這行 Log，看看抓到幾筆
                android.util.Log.d("DEBUG", "抓到資料筆數: ${list.size}")
                adapter.updateData(list)

                // 如果沒有資料，可以顯示「無資料」的提示文字
                if (list.isEmpty()) {
                    // binding.tvEmptyState.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "載入失敗: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}