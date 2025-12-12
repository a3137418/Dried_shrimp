package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.ActivityAddProductsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddProductsActivity : AppCompatActivity() {
    lateinit var binding : ActivityAddProductsBinding
    private val db = FirebaseFirestore.getInstance()
    // 1. 定義一個變數來暫存選到的分類
    private var currentCategory: String = "未分類"

    // 2. 註冊 Activity Result Launcher (用來接收回傳值)
    private val categoryLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 取得回傳的分類名稱
            val selected = result.data?.getStringExtra("SELECTED_CATEGORY")
            if (selected != null) {
                currentCategory = selected
                // 更新畫面上顯示的文字 (假設您的 TextView ID 是 tv_value)
                binding.itemFormRowClassification.tvClassification.text = selected
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupListeners()

    }
    fun setupListeners(){
        //返回鍵
        val back = binding.imgBackAddproducts
        back.setOnClickListener {
            finish()
        }
        // 2. 「上架」按鈕
        binding.btnPublish.setOnClickListener {
            publishProduct()
        }
        // 3. 設定「分類」欄位的點擊事件
        // 注意：這裡使用 binding.includeID.root 來監聽整個橫條的點擊
        binding.itemFormRowClassification.root.setOnClickListener {
            val intent = Intent(this, CategorySelectionActivity::class.java)
            categoryLauncher.launch(intent) // 啟動選擇頁面
        }
    }
    private fun publishProduct() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show()
            return
        }
        // --- A. 抓取資料 ---
        // 1. 商品名稱 (直接從 EditText 抓)
        val name = binding.etProductName.text.toString().trim()

        // 2. 價格 (從 include 的 et_input 抓)
        val priceStr = binding.itemFormRowPriceSetting.etInput.text.toString().trim()

        // 3. 數量 (從 include 的 et_input 抓)
        val stockStr = binding.itemFormRowQuantity.etInput.text.toString().trim()

        // 4. 最低購買數量 (從 include 的 et_input 抓，若沒填預設為 1)
        val minQtyStr = binding.itemFormRowMin.etInput.text.toString().trim()

        // 5. 描述與分類 (目前畫面是箭頭，暫時給預設值，或是之後您實作跳頁後存回來的變數)
        // 假設您的 include 裡是用 TextView 顯示選擇結果 (例如 ID 是 tv_value)
        // val description = binding.itemFormRowProductdescription.tvValue.text.toString()
        val description = "這是很棒的商品" // (暫時寫死，讓功能先跑通)
        val category = "未分類"           // (暫時寫死)

        // --- B. 防呆檢查 ---
        if (name.isEmpty()) {
            Toast.makeText(this, "請輸入商品名稱", Toast.LENGTH_SHORT).show()
            return
        }
        if (priceStr.isEmpty()) {
            Toast.makeText(this, "請輸入價格", Toast.LENGTH_SHORT).show()
            return
        }
        if (stockStr.isEmpty()) {
            Toast.makeText(this, "請輸入商品數量", Toast.LENGTH_SHORT).show()
            return
        }

        // 轉換數字
        val price = priceStr.toIntOrNull() ?: 0
        val stock = stockStr.toIntOrNull() ?: 0
        val minQty = minQtyStr.toIntOrNull() ?: 1

        // --- C. 建立 Product 物件 ---
        // 先產生一個空的 Document ID
        val newDocRef = db.collection("products").document()

        val newProduct = Product(
            id = newDocRef.id,
            sellerId = user.uid,
            name = name,
            description = description,
            category = currentCategory,
            price = price,
            stock = stock,
            minQuantity = minQty,
            shippingFee = 210 // 暫時寫死，對應您畫面上的運費

        )

        // --- D. 上傳到 Firebase (Batch 寫入) ---
        val batch = db.batch()

        // 1. 寫入總商品池 (products)
        batch.set(newDocRef, newProduct)

        // 2. 寫入個人賣場 (users/{uid}/my_store_products)
        val myStoreRef = db.collection("users").document(user.uid)
            .collection("my_store_products").document(newDocRef.id)
        batch.set(myStoreRef, newProduct)

        // 禁止按鈕連點
        binding.btnPublish.isEnabled = false

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "上架成功！", Toast.LENGTH_LONG).show()
                finish() // 關閉頁面
            }
            .addOnFailureListener { e ->
                binding.btnPublish.isEnabled = true
                Toast.makeText(this, "上架失敗：${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}