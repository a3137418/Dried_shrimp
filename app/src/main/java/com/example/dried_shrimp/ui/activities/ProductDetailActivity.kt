package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.ActivityProductDetailBinding

class ProductDetailActivity : AppCompatActivity() {
    lateinit var binding : ActivityProductDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setlistener()
    }
    fun setlistener(){
        // 1. 接收資料
        // 注意：getSerializableExtra 在新版 Android 有更嚴格的寫法，但為了相容性我們先用基本款
        val product = intent.getSerializableExtra("PRODUCT_DATA") as? Product

        // 2. 綁定 UI 元件
        val imgPhoto = binding.imgDetailPhoto
        val tvPrice = binding.tvDetailPrice
        val tvName = binding.tvDetailName
        val tvStock = binding.tvDetailStock
        val tvShipping = binding.tvDetailShipping
        val tvDescription = binding.tvDetailDescription
        val imgBack = binding.imgBack
        val imgDescPhoto = binding.imgDetailDescPhoto


        // 3. 顯示資料
        if (product != null) {
            tvName.text = product.name
            tvPrice.text = "$${product.price}"
            tvStock.text = "庫存: ${product.stock}"
            tvShipping.text = "運費: $${product.shippingFee}"
            tvDescription.text = if (product.description.isEmpty()) "賣家沒有撰寫描述" else product.description

            // 載入圖片
            if (product.imageUrl.isNotEmpty()) {
                Glide.with(this).load(product.imageUrl).into(imgPhoto)
            }
            // --- 新增：顯示描述圖片 ---
            if (product.descImageUrl.isNotEmpty()) {
                imgDescPhoto.visibility = android.view.View.VISIBLE
                Glide.with(this).load(product.descImageUrl).into(imgDescPhoto)
            } else {
                imgDescPhoto.visibility = android.view.View.GONE
            }
        }

        // 4. 返回按鈕
        imgBack.setOnClickListener { finish() }
    }
}