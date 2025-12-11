package com.example.dried_shrimp.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dried_shrimp.R
import com.example.dried_shrimp.databinding.ActivityMoreServeBinding
import com.example.dried_shrimp.databinding.ActivityMyStoreBinding


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


        setListeners()
        back()
    }

    private fun setListeners() {
        val b = binding ?: return
        b.viewMysotreFunction.MyProducts.setOnClickListener {
            val intentTab = Intent(this, TabbedMyStoreActivity::class.java)
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