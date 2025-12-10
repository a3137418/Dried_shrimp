package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.dried_shrimp.R
import com.example.dried_shrimp.ui.adapters.VideoPagerAdapter

class ShortVideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_short_video)

        val viewPager: ViewPager2 = findViewById(R.id.viewPagerVideos)

        // 這裡是您的影片資料來源。在真實專案中，您會從網路、資料庫或本地儲存中獲取。
        // 我們這裡用假資料作為範例。
        val videoList = listOf(
            "android.resource://$packageName/${R.raw.video1}",
            "android.resource://$packageName/${R.raw.video2}",
            "android.resource://$packageName/${R.raw.video3}",
//            "https://www.example.com/path/to/your/video4.mp4" // 也支援網路路徑
        )

        // 建立並設定 Adapter
        val adapter = VideoPagerAdapter(this, videoList)
        viewPager.adapter = adapter
    }
}