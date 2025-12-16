package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.dried_shrimp.R
import com.example.dried_shrimp.ui.fragments.Fragment_SellerOrder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SellerOrderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_order)

        // 避免狀態列遮擋
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val tabLayout = findViewById<TabLayout>(R.id.tabLayout_seller)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager_seller)
        val btnBack = findViewById<ImageView>(R.id.img_back_seller)

        // 設定 Adapter
        viewPager.adapter = SellerPagerAdapter(this)

        // 設定分頁標題
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "待出貨"  // TO_SHIP
                1 -> "已出貨"  // SHIPPED
                2 -> "已完成"  // COMPLETED
                3 -> "退款/售後"
                else -> ""
            }
        }.attach()

        // 返回按鈕
        btnBack.setOnClickListener { finish() }
    }

    // 內部類別：Pager Adapter
    inner class SellerPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        // 對應的分頁狀態
        private val statusList = listOf("TO_SHIP", "SHIPPED", "COMPLETED", "RETURNED")

        override fun getItemCount(): Int = statusList.size

        override fun createFragment(position: Int): Fragment {
            return Fragment_SellerOrder.newInstance(statusList[position])
        }
    }
}