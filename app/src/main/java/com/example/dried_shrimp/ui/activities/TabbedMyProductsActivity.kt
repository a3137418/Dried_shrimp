package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dried_shrimp.R
import com.example.dried_shrimp.databinding.ActivityTabbedMyProductsBinding
import com.example.dried_shrimp.ui.adapters.MyproductsPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class TabbedMyProductsActivity : AppCompatActivity() {



    lateinit var binding : ActivityTabbedMyProductsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 取得 WindowController
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // 設定為 true：代表狀態列背景是淺色，所以系統會把文字/圖示改成「黑色」
        // 設定為 false：代表狀態列背景是深色，系統會把文字/圖示改成「白色」
        windowInsetsController.isAppearanceLightStatusBars = true
        // 1. 初始化 Binding
        binding = ActivityTabbedMyProductsBinding.inflate(layoutInflater)
        // 2. 使用 binding.root 來設定畫面，而不是 R.layout.xxx
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val tabLayout = binding.tabLayoutMyProducts
        val viewPager = binding.MyProductsViewPager
        val adapter = MyproductsPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val index = intent.getIntExtra("tab_index", 0)
            viewPager.currentItem = index
            tab.text = when (position) {
                0 -> "架上商品"
                1 -> "已售完"
                2 -> "審核中"
                3 -> "已違規"
                4 -> "未上架"
                else -> ""
            }
        }.attach()
        listener()
        back()
    }

    fun listener(){
        val btn_add = binding.btnAddproducts
        btn_add.setOnClickListener {
            val intent = Intent(this, AddProductsActivity::class.java)
            startActivity(intent)
        }
    }
    fun back(){
        val back = binding.imgBackMyProducts
        back.setOnClickListener {
            finish()
        }
    }
}