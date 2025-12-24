package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.databinding.ActivityMyStoreBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyStoreActivity : AppCompatActivity() {
    lateinit var binding : ActivityMyStoreBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 1. 初始化 Binding
        binding = ActivityMyStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始載入
        updateStoreInfo()
        setListeners()
        back()
    }

    // 每次回到頁面時，重新抓取資料 (確保評分是最新的)
    override fun onResume() {
        super.onResume()
        updateStoreInfo()
    }

    private fun updateStoreInfo(){
        val user = auth.currentUser
        if(user != null){
            // (A) 設定基本資料 (從 Auth 快速讀取)
            val userName = user.displayName ?: "未命名賣場"
            val userEmail = user.email
            binding.viewUserMystore.tvUserName.text = "${userName} 的賣場"
            binding.viewUserMystore.tvUserEmail.text = userEmail

            // 設定大頭貼
            if (user.photoUrl != null) {
                Glide.with(this)
                    .load(user.photoUrl)
                    .circleCrop()
                    .into(binding.viewUserMystore.imgMyaccountMystore)
            }

            // (B) 🔥 新增：從 Firestore 讀取賣場評分與數量
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // 讀取 sellerRating (賣家平均分) 和 sellerReviewCount (總評價數)
                        // 若欄位不存在則預設為 0
                        val sellerRating = document.getDouble("sellerRating") ?: 0.0
                        val sellerReviewCount = document.getLong("sellerReviewCount") ?: 0

                        // 綁定到 UI (星星與數字)
                        binding.viewUserMystore.rbStoreRating.rating = sellerRating.toFloat()
                        binding.viewUserMystore.tvStoreReviewCount.text = "($sellerReviewCount)"

                        Log.d("MyStoreActivity", "賣場評分已更新: $sellerRating 分, $sellerReviewCount 則評價")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MyStoreActivity", "讀取賣場評分失敗", e)
                }

        } else {
            Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setListeners() {
        // 我的商品管理 (點擊跳轉到 Tab 頁面)
        binding.viewMysotreFunction.MyProducts.setOnClickListener {
            val intent = Intent(this, TabbedMyProductsActivity::class.java)
            startActivity(intent)
        }

        // 賣家訂單中心
        binding.viewUserMystore.btnSellerOrders.setOnClickListener {
            val intent = Intent(this, SellerOrderActivity::class.java)
            startActivity(intent)
        }

        // (您可以繼續加入其他按鈕的監聽，例如銷售報表等)
    }

    fun back(){
        val back = binding.MyStoreBack
        back.setOnClickListener {
            finish()
        }
    }
}