package com.example.dried_shrimp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.data.model.Category
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentCategoryBinding
import com.example.dried_shrimp.ui.adapters.CategoryMenuAdapter
import com.example.dried_shrimp.ui.adapters.HomeProductAdapter
import com.google.firebase.firestore.FirebaseFirestore

class Fragment_Category : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    // 左側選單 Adapter
    private lateinit var menuAdapter: CategoryMenuAdapter
    // 右側商品 Adapter (直接復用首頁的)
    private lateinit var productAdapter: HomeProductAdapter

    // 暫存所有商品資料
    private var allProductList: List<Product> = ArrayList()

    // 🔥 修正 1: 將原本的函式改為變數，並只取出「名稱」轉成 List<String> 給 Adapter 用
    // 這樣後面才能直接用 categoryList[0]
    private val categoryList: List<String> by lazy {
        getAllCategories().map { it.name }
    }
    fun getAllCategories(): List<Category> {
        return listOf(
            Category("1", "女生衣著"),
            Category("2", "男生衣著"),
            Category("3", "女鞋"),
            Category("4", "男鞋"),
            Category("5", "女生包包/精品"),
            Category("6", "男生包包"),
            Category("7", "電腦與周邊"),
            Category("8", "手機平板與周邊"),
            Category("9", "美妝保養"),
            Category("10", "保健"),
            Category("11", "時尚配件"),
            Category("12", "家用電器"),
            Category("13", "旅行相關用品/行李箱"),
            Category("14", "書籍及雜誌期刊"),
            Category("15", "手錶")
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLeftMenu()
        setupRightContent()
        loadAllProducts() // 下載資料
    }

    // 1. 設定左側分類選單
    private fun setupLeftMenu() {
        // 🔥 修正 2: 這裡傳入的是 List<String> (上面轉好的 categoryList)
        menuAdapter = CategoryMenuAdapter(categoryList) { selectedCategory ->
            // 當點擊分類時，執行過濾
            filterProducts(selectedCategory)
        }

        binding.rvCategoryMenu.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = menuAdapter
        }
    }

    // 2. 設定右側商品列表
    private fun setupRightContent() {
        productAdapter = HomeProductAdapter(emptyList())

        binding.rvCategoryProducts.apply {
            // 右邊用 Grid 顯示，設為 2 欄 (因為寬度只有 3/4，2欄剛好)
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }
    }

    // 3. 從 Firebase 載入所有商品
    private fun loadAllProducts() {
        db.collection("products")
            //.whereEqualTo("status", "ON_SHELF") // 暫時註解，先抓全部來測試，以免狀態不符抓不到
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val rawList = result.toObjects(Product::class.java)

                    // 過濾邏輯：只留有庫存且上架的 (您可以在這裡加回狀態判斷)
                    allProductList = rawList.filter { it.stock > 0 && it.status == "ON_SHELF" }

                    // 🔥 修正 3: 確保 list 不為空才去取第 0 個，避免 Crash
                    if (categoryList.isNotEmpty()) {
                        filterProducts(categoryList[0])
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "載入失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 4. 過濾邏輯
    private fun filterProducts(category: String) {
        val filteredList = if (category == "全部") {
            allProductList
        } else {
            allProductList.filter { product ->
                // 忽略大小寫比較
                product.category.equals(category, ignoreCase = true)
            }
        }

        // 更新右側列表
        productAdapter.updateData(filteredList)

        // 如果該分類沒商品，可以跳個提示
        if (filteredList.isEmpty() && allProductList.isNotEmpty()) {
            // Toast.makeText(context, "此分類暫無商品", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}