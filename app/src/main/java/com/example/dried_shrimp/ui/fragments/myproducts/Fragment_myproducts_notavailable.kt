package com.example.dried_shrimp.ui.fragments.myproducts

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentMyproductsNotavailableBinding
import com.example.dried_shrimp.ui.activities.AddProductsActivity // 假設您的新增商品頁面是這個
import com.example.dried_shrimp.ui.adapters.MyProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Fragment_myproducts_notavailable : Fragment() {

    private var _binding: FragmentMyproductsNotavailableBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: MyProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyproductsNotavailableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 設定 RecyclerView (包含按鈕邏輯)
        setupRecyclerView()

        // 2. 載入資料
        loadProducts()
    }

    private fun setupRecyclerView() {
        adapter = MyProductAdapter(
            productList = emptyList(),

            // 1. 編輯按鈕：跳轉到 AddProductsActivity (把商品資料傳過去)
            onEditClick = { product ->
                val intent = Intent(context, AddProductsActivity::class.java)
                intent.putExtra("EDIT_PRODUCT", product)
                startActivity(intent)
            },

            // 2. 右邊按鈕：執行「刪除」邏輯
            onStatusChangeClick = { product ->
                showDeleteConfirmDialog(product)
            }
        )

        binding.recycleNotavailable.layoutManager = LinearLayoutManager(context)
        binding.recycleNotavailable.adapter = adapter
    }

    private fun loadProducts() {
        val userId = auth.currentUser?.uid ?: return

        // 查詢：未上架 (OFF_SHELF)
        db.collection("products")
            .whereEqualTo("sellerId", userId)
            .whereEqualTo("status", "OFF_SHELF")
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(Product::class.java)
                adapter.updateData(list)
            }
            .addOnFailureListener {
                Toast.makeText(context, "載入失敗", Toast.LENGTH_SHORT).show()
            }
    }

    // --- 功能：顯示刪除確認對話框 ---
    private fun showDeleteConfirmDialog(product: Product) {
        // Fragment 中使用 requireContext() 或 context
        AlertDialog.Builder(requireContext())
            .setTitle("刪除商品")
            .setMessage("確定要刪除「${product.name}」嗎？刪除後無法復原。")
            .setPositiveButton("刪除") { _, _ ->
                deleteProductFromFirebase(product)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // --- 功能：執行 Firestore 刪除 ---
    private fun deleteProductFromFirebase(product: Product) {
        val userId = auth.currentUser?.uid ?: return

        // 使用 Batch 批次寫入，確保同時刪除成功
        val batch = db.batch()

        // 1. 刪除全域商品池的資料 (這也是這個 Fragment 主要顯示的來源)
        val globalRef = db.collection("products").document(product.id)
        batch.delete(globalRef)

        // 2. 如果您有維護個人的 my_store_products 集合，也要一起刪除 (參照您的 MyStoreActivity)
        val myStoreRef = db.collection("users").document(userId)
            .collection("my_store_products").document(product.id)
        batch.delete(myStoreRef)

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "已刪除", Toast.LENGTH_SHORT).show()
                // 刪除成功後，重新載入列表，讓商品從畫面消失
                loadProducts()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "刪除失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // (選用功能) 如果您想讓按鈕變成「重新上架」，可以使用這個方法
    private fun updateProductStatus(productId: String, newStatus: String) {
        db.collection("products").document(productId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(context, "商品已重新上架", Toast.LENGTH_SHORT).show()
                loadProducts() // 重新載入，該商品會移到「架上商品」頁面
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}