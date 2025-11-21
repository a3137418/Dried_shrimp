package com.example.dried_shrimp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        // 載入到主畫面的 fragment 區域
        loadFragment(Fragment_Home(), R.id.fragment_container)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(Fragment_Home(), R.id.fragment_container)
                R.id.nav_category -> loadFragment(Fragment_Home(), R.id.fragment_container)
                R.id.nav_live ->{
                    loadFragment(Fragment_Live(), R.id.fragment_container)
//                    val intent = Intent(this, ShortVideoActivity::class.java)
//                    startActivity(intent)
                }
//                R.id.nav_order -> loadFragment(OrderFragment())
                R.id.nav_user -> loadFragment(Fragment_user2(), R.id.fragment_container)
            }
            true
        }

        // ★ 讀取從 Login 傳來的目標
        val target = intent.getStringExtra("Login_successful")

        if (target == "fragment_user2") {
            // 直接載入 fragment_user2
            loadFragment(Fragment_user2(), R.id.fragment_container)
        } else {
            // 一般情況載入首頁
            loadFragment(Fragment_Home(), R.id.fragment_container)
        }
    }

    private fun loadFragment(fragment: Fragment,containerId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }

}