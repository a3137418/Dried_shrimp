package com.example.dried_shrimp.ui.activities

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

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
            binding.tvDetailShipping.text = "運費: $${product.shippingFee}"
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

        // 返回按鈕
        binding.imgBack.setOnClickListener { finish() }

        // 3. 加入購物車按鈕
        binding.btnAddToCart.setOnClickListener {
            addToCart()
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
}