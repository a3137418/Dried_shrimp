package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.CartItem
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.ActivityProductDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.ui.adapters.ProductReviewAdapter
import com.example.dried_shrimp.data.model.Review // 確保有這行
class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentProduct: Product? = null
    private lateinit var reviewAdapter: ProductReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 1. 初始化 RecyclerView
        setupReviewList()
        // 2. 載入評價 (假設您有取得 productId)
        // 這裡請確認您原本是怎麼取得 productId 的，通常是 intent.getStringExtra("PRODUCT_ID")
        val productId = intent.getStringExtra("PRODUCT_ID")
        if (productId != null) {
            loadProductReviews(productId)
        }
        setupListener()
    }

    private fun setupListener() {
        // 1. 接收資料
        currentProduct = intent.getSerializableExtra("PRODUCT_DATA") as? Product

        // 2. 顯示資料
        if (currentProduct != null) {
            val product = currentProduct!!
            binding.tvDetailName.text = product.name
            binding.tvDetailPrice.text = "$${product.price}"
            binding.tvDetailStock.text = "庫存: ${product.stock}"
            binding.tvDetailShipping.text = "運費: ${product.shippingFee}"
            binding.tvDetailDescription.text = if (product.description.isEmpty()) "賣家沒有撰寫描述" else product.description

            if (product.imageUrl.isNotEmpty()) {
                Glide.with(this).load(product.imageUrl).into(binding.imgDetailPhoto)
            }
            // 描述附圖
            if (product.descImageUrl.isNotEmpty()) {
                binding.imgDetailDescPhoto.visibility = android.view.View.VISIBLE
                Glide.with(this).load(product.descImageUrl).into(binding.imgDetailDescPhoto)
            } else {
                binding.imgDetailDescPhoto.visibility = android.view.View.GONE
            }
        }

        // 3. 加入購物車按鈕 (保留這一個就好，原本下面有一個重複的)
        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }

        // 4. 直接購買按鈕
        binding.btnDirectBuy.setOnClickListener {
            directBuy()
        }

        // 返回按鈕
        binding.imgBack.setOnClickListener { finish() }

        // ★★★ 修改：聊聊按鈕 (連結到真實賣家) ★★★
        binding.btnChat.setOnClickListener {
            // 1. 檢查商品資料是否存在
            val product = currentProduct
            if (product == null) {
                Toast.makeText(this, "商品資料讀取錯誤", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2. 檢查使用者是否登入 (未登入不能聊天)
            if (auth.currentUser == null) {
                Toast.makeText(this, "請先登入才能使用聊聊功能", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. 防呆：不能跟自己聊天
            if (product.sellerId == auth.currentUser?.uid) {
                Toast.makeText(this, "這是您自己的商品，無法進行聊聊", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. 準備跳轉到 ChatMainActivity
            val intent = Intent(this, ChatMainActivity::class.java)

            // 5. 傳遞關鍵資料
            // 顯示的標題 (如果有賣場名稱 product.storeName 更好，沒有就顯示 "賣家")
            intent.putExtra("chat_target_name", "賣家")

            // 賣家的 ID (這最重要！用來區分不同的聊天室)
            intent.putExtra("chat_target_id", product.sellerId)

            // 告訴聊天室這是 "seller" 模式 (這樣就不會觸發 AI 自動回覆)
            intent.putExtra("chat_target_type", "seller")

            // 帶入商品名稱，讓輸入框預設填入 "我想詢問關於..."
            intent.putExtra("product_name", product.name)

            startActivity(intent)
        }
    }

    private fun addToCart() {
        val user = auth.currentUser
        val product = currentProduct

        // 1. 基本檢查
        if (user == null) {
            Toast.makeText(this, "請先登入才能購物", Toast.LENGTH_SHORT).show()
            return
        }

        if (product == null) {
            Toast.makeText(this, "商品資料錯誤", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 防呆：不能買自己的商品
        if (product.sellerId == user.uid) {
            Toast.makeText(this, "您不能將自己的商品加入購物車", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. 檢查庫存
        if (product.stock <= 0) {
            Toast.makeText(this, "此商品已售完", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. 定義 Firestore 購物車的路徑
        val cartItemRef = db.collection("users").document(user.uid)
            .collection("cart").document(product.id)

        // 5. 先讀取一次，看看購物車裡面有沒有這個商品
        cartItemRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // --- 情境 A: 購物車已經有這個商品 -> 數量 +1 ---
                    val currentQty = document.getLong("quantity")?.toInt() ?: 1

                    // 檢查是否超過庫存
                    if (currentQty >= product.stock) {
                        Toast.makeText(this, "購物車數量已達庫存上限", Toast.LENGTH_SHORT).show()
                    } else {
                        cartItemRef.update("quantity", currentQty + 1)
                            .addOnSuccessListener {
                                Toast.makeText(this, "已更新購物車數量！", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // --- 情境 B: 購物車還沒有這個商品 -> 新增一筆 ---
                    // ★★★ 關鍵修正：這裡必須填入 sellerId ★★★
                    val newCartItem = CartItem(
                        productId = product.id,
                        name = product.name,
                        price = product.price,
                        imageUrl = product.imageUrl,
                        quantity = 1,
                        isChecked = true,

                        // ★ 這行最重要！把商品的賣家 ID 存進購物車
                        sellerId = product.sellerId
                    )

                    cartItemRef.set(newCartItem)
                        .addOnSuccessListener {
                            Toast.makeText(this, "成功加入購物車！", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "加入失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "連線錯誤: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    // ★★★ 新增這個函式 ★★★
    private fun directBuy() {
        val user = auth.currentUser
        val product = currentProduct

        // 1. 檢查登入
        if (user == null) {
            Toast.makeText(this, "請先登入才能購買", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. 檢查商品資料
        if (product == null) {
            Toast.makeText(this, "商品資料錯誤", Toast.LENGTH_SHORT).show()
            return
        }

        // 3. 防呆：不能買自己的商品
        if (product.sellerId == user.uid) {
            Toast.makeText(this, "您不能購買自己的商品", Toast.LENGTH_SHORT).show()
            return
        }

        // 4. 檢查庫存
        if (product.stock <= 0) {
            Toast.makeText(this, "此商品已售完", Toast.LENGTH_SHORT).show()
            return
        }

        // 5. 跳轉到結帳頁面 (CheckoutActivity)
        // 注意：這裡假設你已經有 CheckoutActivity。如果還沒有，請告訴我，我們需要建立一個。
        val intent = Intent(this, CheckoutActivity::class.java)

        // 6. 傳遞商品資訊過去
        intent.putExtra("product_id", product.id)
        intent.putExtra("product_name", product.name)
        intent.putExtra("product_price", product.price)
        intent.putExtra("product_image", product.imageUrl)
        intent.putExtra("seller_id", product.sellerId)
        intent.putExtra("quantity", 1) // 直接購買預設數量為 1

        // ★ 關鍵標記：告訴結帳頁，這是「直接購買」(不用去讀購物車資料庫)
        intent.putExtra("is_direct_buy", true)

        startActivity(intent)
    }
    private fun setupReviewList() {
        reviewAdapter = ProductReviewAdapter(emptyList())
        // 請確認您的 XML 裡面的 RecyclerView ID 是 rvProductReviews
        binding.rvProductReviews.apply {
            layoutManager = LinearLayoutManager(this@ProductDetailActivity)
            adapter = reviewAdapter
        }
    }

    private fun loadProductReviews(productId: String) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        db.collection("reviews")
            .whereEqualTo("productId", productId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val reviewList = result.toObjects(Review::class.java)
                    // 按照時間新到舊排序
                    val sortedList = reviewList.sortedByDescending { it.timestamp }

                    reviewAdapter.updateData(sortedList)

                    // 更新標題旁的數量，例如：商品評價 (5)
                    // 請確認您的 XML 裡有這個 ID: tvReviewCount
                    binding.tvReviewCount.text = "(${sortedList.size})"
                } else {
                    binding.tvReviewCount.text = "(0)"
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("ProductDetail", "評價載入失敗", e)
            }
    }
}