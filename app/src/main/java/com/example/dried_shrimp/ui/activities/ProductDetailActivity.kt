package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log // 引入 Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.CartItem
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.data.model.Review
import com.example.dried_shrimp.databinding.ActivityProductDetailBinding
import com.example.dried_shrimp.ui.adapters.ProductReviewAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        // 2. 接收資料 (先接收物件，確保 ID 來源正確)
        currentProduct = intent.getSerializableExtra("PRODUCT_DATA") as? Product

        // 3. 取得 Product ID 並載入評價
        // 邏輯：先看 Intent 有沒有傳 ID 字串，沒有的話就從 Product 物件拿
        val productId = intent.getStringExtra("PRODUCT_ID") ?: currentProduct?.id

        if (!productId.isNullOrEmpty()) {
            loadProductReviews(productId)
        } else {
            Log.e("ProductDetail", "錯誤：找不到 Product ID，無法載入評價")
        }

        // 4. 設定 UI 監聽與顯示
        setupListener()
    }

    private fun setupListener() {
        // 顯示資料
        if (currentProduct != null) {
            val product = currentProduct!!
            binding.tvDetailName.text = product.name
            binding.tvDetailPrice.text = "$${product.price}"
            binding.tvDetailStock.text = "庫存: ${product.stock}"
            binding.tvDetailShipping.text = "運費: ${product.shippingFee}"
            binding.tvDetailDescription.text = if (product.description.isEmpty()) "賣家沒有撰寫描述" else product.description

            // 🔥 新增：顯示平均星星 (請確保 XML 裡有 ratingBarProduct 這個 ID)
            // 如果您的 XML 還沒加 RatingBar，這行可能會報錯，請記得去 XML 加
            // binding.ratingBarProduct.rating = product.rating.toFloat()
            // binding.tvRatingCount.text = "(${product.reviewCount})"

            // 如果 XML 裡暫時沒有 ratingBar，可以用文字顯示：
            // binding.tvDetailName.text = "${product.name} (★${product.rating})"

            if (product.imageUrl.isNotEmpty()) {
                Glide.with(this).load(product.imageUrl).into(binding.imgDetailPhoto)
            }
            // 描述附圖
            if (product.descImageUrl.isNotEmpty()) {
                binding.imgDetailDescPhoto.visibility = View.VISIBLE
                Glide.with(this).load(product.descImageUrl).into(binding.imgDetailDescPhoto)
            } else {
                binding.imgDetailDescPhoto.visibility = View.GONE
            }
        }

        // 加入購物車按鈕
        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }

        // 直接購買按鈕
        binding.btnDirectBuy.setOnClickListener {
            directBuy()
        }

        // 返回按鈕
        binding.imgBack.setOnClickListener { finish() }

        // 聊聊按鈕
        binding.btnChat.setOnClickListener {
            val product = currentProduct
            if (product == null) {
                Toast.makeText(this, "商品資料讀取錯誤", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (auth.currentUser == null) {
                Toast.makeText(this, "請先登入才能使用聊聊功能", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (product.sellerId == auth.currentUser?.uid) {
                Toast.makeText(this, "這是您自己的商品，無法進行聊聊", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, ChatMainActivity::class.java)
            intent.putExtra("chat_target_name", "賣家")
            intent.putExtra("chat_target_id", product.sellerId)
            intent.putExtra("chat_target_type", "seller")
            intent.putExtra("product_name", product.name)
            startActivity(intent)
        }
    }

    private fun addToCart() {
        val user = auth.currentUser
        val product = currentProduct

        if (user == null) {
            Toast.makeText(this, "請先登入才能購物", Toast.LENGTH_SHORT).show()
            return
        }

        if (product == null) {
            Toast.makeText(this, "商品資料錯誤", Toast.LENGTH_SHORT).show()
            return
        }

        if (product.sellerId == user.uid) {
            Toast.makeText(this, "您不能將自己的商品加入購物車", Toast.LENGTH_SHORT).show()
            return
        }

        if (product.stock <= 0) {
            Toast.makeText(this, "此商品已售完", Toast.LENGTH_SHORT).show()
            return
        }

        val cartItemRef = db.collection("users").document(user.uid)
            .collection("cart").document(product.id)

        cartItemRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val currentQty = document.getLong("quantity")?.toInt() ?: 1
                    if (currentQty >= product.stock) {
                        Toast.makeText(this, "購物車數量已達庫存上限", Toast.LENGTH_SHORT).show()
                    } else {
                        cartItemRef.update("quantity", currentQty + 1)
                            .addOnSuccessListener {
                                Toast.makeText(this, "已更新購物車數量！", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    val newCartItem = CartItem(
                        productId = product.id,
                        name = product.name,
                        price = product.price,
                        imageUrl = product.imageUrl,
                        quantity = 1,
                        isChecked = true,
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

    private fun directBuy() {
        val user = auth.currentUser
        val product = currentProduct

        if (user == null) {
            Toast.makeText(this, "請先登入才能購買", Toast.LENGTH_SHORT).show()
            return
        }
        if (product == null) {
            Toast.makeText(this, "商品資料錯誤", Toast.LENGTH_SHORT).show()
            return
        }
        if (product.sellerId == user.uid) {
            Toast.makeText(this, "您不能購買自己的商品", Toast.LENGTH_SHORT).show()
            return
        }
        if (product.stock <= 0) {
            Toast.makeText(this, "此商品已售完", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, CheckoutActivity::class.java)
        intent.putExtra("product_id", product.id)
        intent.putExtra("product_name", product.name)
        intent.putExtra("product_price", product.price)
        intent.putExtra("product_image", product.imageUrl)
        intent.putExtra("seller_id", product.sellerId)
        intent.putExtra("quantity", 1)
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
        // 🔥 加入 Log 方便除錯
        Log.d("ProductDetail", "開始載入評價，商品ID: $productId")

        db.collection("reviews")
            .whereEqualTo("productId", productId)
            .get()
            .addOnSuccessListener { result ->
                Log.d("ProductDetail", "搜尋完成，共找到 ${result.size()} 筆評價")

                if (!result.isEmpty) {
                    val reviewList = result.toObjects(Review::class.java)
                    val sortedList = reviewList.sortedByDescending { it.timestamp }

                    reviewAdapter.updateData(sortedList)

                    // 這裡會顯示括號內的數量 (5)
                    binding.tvReviewCount.text = "(${sortedList.size})"
                } else {
                    binding.tvReviewCount.text = "(0)"
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProductDetail", "評價載入失敗", e)
            }
    }
}