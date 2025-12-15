package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import com.example.dried_shrimp.R
import com.example.dried_shrimp.ui.adapters.CategoryAdapter

class CategorySelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_selection)

        // 1. 設定返回按鈕
        findViewById<ImageView>(R.id.img_back).setOnClickListener {
            finish()
        }

        // 2. 準備分類資料 (依照您的截圖)
        val categoryList = listOf(
            "女生衣著",
            "男生衣著",
            "女鞋",
            "男鞋",
            "女生包包/精品",
            "男生包包",
            "電腦與周邊",
            "手機平板與周邊",
            "美妝保養",
            "保健",
            "時尚配件",
            "家用電器",
            "旅行相關用品/行李箱",
            "書籍及雜誌期刊",
            "手錶",
        )

        // 3. 設定 RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rv_categories)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 4. 設定 Adapter 與點擊事件
        val adapter = CategoryAdapter(categoryList) { selectedCategory ->
            // 當使用者點擊某個分類時...

            // 建立要回傳的 Intent
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_CATEGORY", selectedCategory)

            // 設定結果為 OK 並回傳資料
            setResult(RESULT_OK, resultIntent)

            // 關閉目前頁面，回到上一頁
            finish()
        }

        recyclerView.adapter = adapter
    }
}