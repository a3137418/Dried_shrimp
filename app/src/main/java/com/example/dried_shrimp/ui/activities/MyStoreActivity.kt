package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.databinding.ActivityMyStoreBinding
import com.google.firebase.auth.FirebaseAuth


class MyStoreActivity : AppCompatActivity() {
    lateinit var binding : ActivityMyStoreBinding
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
        updateStoreInfo()
        setListeners()
        back()
    }
    private fun updateStoreInfo(){
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null){
            // (A) 設定店名 / 使用者名稱
            // 假設您的 Layout 裡有一個 TextView 叫 tvStoreName
            val userName = user.displayName ?: "未命名賣場"
            binding.viewUserMystore.tvUserName.text = "${userName} 的賣場"

            // (B) 設定大頭貼 (如果有 Imageview 叫 imgStoreAvatar)
            if (user.photoUrl != null) {
                Glide.with(this)
                    .load(user.photoUrl)
                    .circleCrop() // 圓形剪裁
                    .into(binding.viewUserMystore.imgMyaccountMystore)
            }
        } else {
            // 如果意外沒登入卻跑進來，可以趕他出去
            Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setListeners() {
        val b = binding ?: return
        b.viewMysotreFunction.MyProducts.setOnClickListener {
            val intentTab = Intent(this, TabbedMyProductsActivity::class.java)
            intentTab.putExtra("tab_index", 0)
            startActivity(intentTab)
        }

    }

    fun back(){
        val back = binding.MyStoreBack
        back.setOnClickListener {
            finish()
        }
    }
}