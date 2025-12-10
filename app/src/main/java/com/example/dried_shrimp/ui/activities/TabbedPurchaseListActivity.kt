package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.dried_shrimp.ui.adapters.PurchasePagerAdapter
import com.example.dried_shrimp.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TabbedPurchaseListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabbed_purchase_list)


        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.purchase_list_viewPager)

        val adapter = PurchasePagerAdapter(this)
        viewPager.adapter = adapter


        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val index = intent.getIntExtra("tab_index", 0)
            viewPager.currentItem = index
            tab.text = when (position) {
                0 -> "待付款"
                1 -> "待出貨"
                2 -> "待收貨"
                3 -> "已完成"
                4 -> "退貨/退款"
                5 -> "不成立"
                else -> ""
            }
        }.attach()

        back()
    }
    fun back(){
        val back = findViewById<ImageView>(R.id.purchase_img_back)
        back.setOnClickListener {
            finish()
        }
    }
}