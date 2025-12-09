package com.example.dried_shrimp

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dried_shrimp.databinding.ActivityShoppingCartBinding


class shopping_cart : AppCompatActivity() {
    lateinit var binding : ActivityShoppingCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. 初始化 Binding
        binding = ActivityShoppingCartBinding.inflate(layoutInflater)

        // 2. 使用 binding.root 來設定畫面，而不是 R.layout.xxx
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupRecyclerViews()
    }




    private fun setupRecyclerViews() {
        val guesslike_Adapter = guesslike_Adapter()
        binding?.sectionGuesslike?.myRecycleLike?.layoutManager = GridLayoutManager(this,2)
        binding?.sectionGuesslike?.myRecycleLike?.adapter = guesslike_Adapter

    }


    fun back(){
        val back = binding.shoppingCartBack
        back.setOnClickListener {
            finish()
        }
    }
}