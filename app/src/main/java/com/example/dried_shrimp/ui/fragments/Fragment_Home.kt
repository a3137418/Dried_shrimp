package com.example.dried_shrimp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentHomeBinding
import com.example.dried_shrimp.ui.activities.ShoppingCartActivity
import com.example.dried_shrimp.ui.adapters.HomeProductAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Fragment_Home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // 透過這個屬性，我們可以直接用 binding 存取，不用一直打 ?.
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 統一使用這一個 Adapter 變數
    private lateinit var productAdapter: HomeProductAdapter

    // 用來暫存所有從網路抓下來的商品 (供搜尋過濾用)
    private var allProductList: List<Product> = ArrayList()

    val sizeInDp = 24

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 初始化 RecyclerView
        setupRecyclerView()

        // 2. 設定搜尋框圖示大小
        setupSearchBarIcon()

        // 3. 設定搜尋監聽器
        binding.etserach.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val keyword = v.text.toString().trim()
                if (keyword.isNotEmpty()) {
                    searchProducts(keyword)
                } else {
                    // 如果清空搜尋，就顯示原本所有商品
                    productAdapter.updateData(allProductList)
                }
                true
            } else {
                false
            }
        }

        // 4. 設定購物車點擊事件
        binding.imgCart.setOnClickListener {
            val intent = Intent(context, ShoppingCartActivity::class.java)
            startActivity(intent)
        }

        // 5. 初始載入資料 (這行也可以不用，因為 onResume 會呼叫)
        loadAllProducts()
    }

    // 每次回到頁面 (onResume) 都重新抓資料
    override fun onResume() {
        super.onResume()
        Log.d("Fragment_Home", "回到首頁，重新整理商品資料...")
        loadAllProducts()
    }

    private fun setupRecyclerView() {
        // 初始化
        productAdapter = HomeProductAdapter(emptyList())

        binding.recycleHome.apply {
            layoutManager = GridLayoutManager(context, 2)

            // 🔥 關鍵檢查：這裡指定的 adapter 必須跟上面是同一個變數
            adapter = productAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupSearchBarIcon() {
        val sizeInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, sizeInDp.toFloat(), resources.displayMetrics
        ).toInt()

        val searchIcon = ContextCompat.getDrawable(requireContext(), R.drawable.search)
        val cameraIcon = ContextCompat.getDrawable(requireContext(), R.drawable.camera)

        searchIcon?.setBounds(0, 0, sizeInPx, sizeInPx)
        cameraIcon?.setBounds(0, 0, sizeInPx, sizeInPx)

        binding.etserach.setCompoundDrawables(searchIcon, null, cameraIcon, null)
    }

    // 統一用這個函式載入資料
    private fun loadAllProducts() {
        // 先不要加 .whereEqualTo，我們全部抓下來檢查
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val rawList = result.toObjects(Product::class.java)

                    val validList = ArrayList<Product>()

                    // 🔥 加入這段 Log 迴圈
                    for (product in rawList) {
                        // 針對這款 PS5 做特別檢查
                        if (product.name.contains("PS5")) {
                            Log.e("DEBUG_PS5", "=======================================")
                            Log.e("DEBUG_PS5", "商品名稱: ${product.name}")
                            Log.e("DEBUG_PS5", "資料庫狀態: [${product.status}] vs 條件: [ON_SHELF]")
                            Log.e("DEBUG_PS5", "資料庫庫存: [${product.stock}] (型態: ${product.stock::class.simpleName})")
                            Log.e("DEBUG_PS5", "判斷結果: 狀態是否OK? ${product.status == "ON_SHELF"} | 庫存是否>0? ${product.stock > 0}")
                            Log.e("DEBUG_PS5", "=======================================")
                        }

                        // 原本的過濾邏輯
                        if (product.status == "ON_SHELF" && product.stock > 0) {
                            validList.add(product)
                        }
                    }

                    // 更新 Adapter
                    allProductList = validList
                    productAdapter.updateData(validList)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Fragment_Home", "載入失敗", e)
            }
    }

    private fun searchProducts(input: String) {
        val keywords = mutableListOf(input)

        // --- 同義詞擴充 ---
        if (input.contains("衣服", ignoreCase = true) || input.contains("服飾", ignoreCase = true)) {
            keywords.add("上衣"); keywords.add("T-shirt"); keywords.add("褲"); keywords.add("裙"); keywords.add("洋裝"); keywords.add("外套")
        }
        if (input.contains("電腦", ignoreCase = true) || input.contains("3C", ignoreCase = true)) {
            keywords.add("筆電"); keywords.add("滑鼠"); keywords.add("鍵盤"); keywords.add("螢幕")
        }
        if (input.contains("吃", ignoreCase = true) || input.contains("食", ignoreCase = true)) {
            keywords.add("零食"); keywords.add("餅乾"); keywords.add("飲料"); keywords.add("海鮮")
        }

        // --- 開始過濾 ---
        val filteredList = allProductList.filter { product ->
            val name = product.name ?: ""
            val description = product.description ?: ""
            val category = product.category ?: ""

            keywords.any { k ->
                name.contains(k, ignoreCase = true) ||
                        description.contains(k, ignoreCase = true) ||
                        category.contains(k, ignoreCase = true)
            }
        }

        // --- 顯示結果 ---
        if (filteredList.isEmpty()) {
            Toast.makeText(context, "找不到「$input」相關商品", Toast.LENGTH_SHORT).show()
        }

        // 更新 Adapter 顯示搜尋結果
        productAdapter.updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}