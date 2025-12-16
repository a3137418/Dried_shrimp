package com.example.dried_shrimp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dried_shrimp.R
import com.example.dried_shrimp.databinding.FragmentHomeBinding
import com.example.dried_shrimp.ui.activities.ShoppingCartActivity
import com.example.dried_shrimp.ui.adapters.HomeProductAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot // 1. 補上這個 import
import com.example.dried_shrimp.data.model.Product // 2. 補上這個 import
import com.google.firebase.auth.FirebaseAuth
class Fragment_Home: Fragment() {
    private var _binding: FragmentHomeBinding? = null
    val sizeInDp = 24
    // 1. 新增變數
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: HomeProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return _binding?.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var etserach = _binding?.etserach
        var img_cart = _binding?.imgCart
        var img_chat = _binding?.imgChat
        var recycle_home =_binding?.recycleHome
        // --- 修改開始：設定新的 Adapter ---
        adapter = HomeProductAdapter(emptyList()) // 一開始先給空清單
        recycle_home?.layoutManager = GridLayoutManager(context, 2)
        recycle_home?.adapter = adapter

        // 從 Firebase 載入真實商品
        loadAllProducts()

        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, sizeInDp.toFloat(), resources.displayMetrics
        ).toInt()
        val searchIcon = ContextCompat.getDrawable(context, R.drawable.search)
        val cameraIcon = ContextCompat.getDrawable(context, R.drawable.camera)

        searchIcon?.setBounds(0, 0, sizeInPx, sizeInPx)
        cameraIcon?.setBounds(0, 0, sizeInPx, sizeInPx)
        etserach?.setCompoundDrawables(searchIcon, null, cameraIcon, null)


        img_cart?.setOnClickListener {
            val intent = Intent(context, ShoppingCartActivity::class.java)
            startActivity(intent)
        }
    }

    // 新增載入商品的函式
    private fun loadAllProducts() {
        db.collection("products")
            .whereEqualTo("status", "ON_SHELF") // ★ 加入這行，確保不推薦下架商品
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->

                //  明確指定 result: QuerySnapshot 解決推斷錯誤
                if (!result.isEmpty) {
                    // 確保 Product 已 import，這樣 ::class.java 就不會報錯
                    val productList = result.toObjects(Product::class.java)
                    adapter.updateData(productList)
                }
            }
            .addOnFailureListener { e: Exception ->
                // 6. 明確指定 e: Exception
                if (isAdded) {
                    Toast.makeText(requireContext(), "載入失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Toast.makeText(requireContext(), "載入失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}