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
import android.view.inputmethod.EditorInfo
class Fragment_Home: Fragment() {
    private var _binding: FragmentHomeBinding? = null
    val sizeInDp = 24
    // ★ 新增這個變數：用來暫存所有從網路抓下來的商品
    private var allProductList: List<Product> = ArrayList()
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

        var recycle_home =_binding?.recycleHome
        // --- 修改開始：設定新的 Adapter ---
        adapter = HomeProductAdapter(emptyList()) // 一開始先給空清單
        recycle_home?.layoutManager = GridLayoutManager(context, 2)
        recycle_home?.adapter = adapter

        // 從 Firebase 載入真實商品
        loadAllProducts()
        // ★★★ 新增：搜尋功能監聽器 ★★★
        etserach?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 1. 取得輸入的文字
                val keyword = v.text.toString().trim()

                // 2. 判斷是否為空
                if (keyword.isNotEmpty()) {
                    searchProducts(keyword) // 有文字就搜尋
                } else {
                    loadAllProducts() // 沒文字就載入全部
                }

                // 3. 回傳 true 表示我們已經處理了這個動作
                true
            } else {
                false
            }
        }
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

    // ★★★ 新增：搜尋專用的函式 ★★★
    private fun searchProducts(input: String) : List<String> {
        // --- 1. 製作「同義詞」擴充清單 ---
        // 預設搜尋列表只包含使用者打的字
        val keywords = mutableListOf(input) // 先把使用者打的字加進去

        // 這裡設定你的「類別邏輯」
        // 如果輸入包含 "衣服" 或 "服飾"，就加入相關分類
        if (input.contains("衣服", ignoreCase = true) || input.contains("服飾", ignoreCase = true)) {
            keywords.add("上衣")
            keywords.add("T-shirt")
            keywords.add("褲")
            keywords.add("裙")
            keywords.add("洋裝")
            keywords.add("外套")
        }

        // 如果輸入包含 "電腦" 或 "3C"，就加入相關分類
        if (input.contains("電腦", ignoreCase = true) || input.contains("3C", ignoreCase = true)) {
            keywords.add("筆電")
            keywords.add("滑鼠")
            keywords.add("鍵盤")
            keywords.add("螢幕")
        }

        // 如果輸入 "吃" 或 "食品"，就加入相關分類
        if (input.contains("吃", ignoreCase = true) || input.contains("食", ignoreCase = true)) {
            keywords.add("零食")
            keywords.add("餅乾")
            keywords.add("飲料")
            keywords.add("海鮮") // 像是乾蝦
        }

        // (你也可以自行擴充其他類別，例如搜「吃的」就多找「零食」、「餅乾」)

        // --- 2. 開始過濾 ---
        val filteredList = allProductList.filter { product ->

            // 取出商品的三大資訊
            val name = product.name ?: ""
            val description = product.description ?: ""
            val category = product.category ?: "" // 確保這欄位存在

            // ★ 檢查：只要「商品資訊」符合「搜尋清單」裡的「任何一個字」就算成功
            // any 意思是：只要符合其中一個條件就回傳 true
            keywords.any { keywords ->
                name.contains(keywords, ignoreCase = true) ||
                        description.contains(keywords, ignoreCase = true) ||
                        category.contains(keywords, ignoreCase = true)
            }
        }

        // --- 3. 處理結果 ---
        if (filteredList.isEmpty()) {
            Toast.makeText(context, "找不到「$input」相關商品", Toast.LENGTH_SHORT).show()
        }

        adapter.updateData(filteredList)
        return keywords
    }
    // 新增載入商品的函式
    private fun loadAllProducts() {
        db.collection("products")
            .whereEqualTo("status", "ON_SHELF")
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                if (!result.isEmpty) {
                    val productList = result.toObjects(Product::class.java)

                    // ★ 1. 把抓到的資料備份起來
                    allProductList = productList

                    // ★ 2. 顯示在畫面上
                    adapter.updateData(productList)
                }
            }
            .addOnFailureListener { e: Exception ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "載入失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}