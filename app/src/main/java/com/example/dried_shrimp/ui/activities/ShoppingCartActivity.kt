package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dried_shrimp.R
import com.example.dried_shrimp.databinding.ActivityShoppingCartBinding
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.example.dried_shrimp.manager.CloudCartManager

class ShoppingCartActivity : AppCompatActivity() {
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
        back()
    }


    override fun onResume() {
        super.onResume()
        loadCartData()
    }

    private fun setupRecyclerViews() {
        val guesslike_Adapter = GuessLikeAdapter()
        binding?.sectionGuesslike?.myRecycleLike?.layoutManager = GridLayoutManager(this, 2)
        binding?.sectionGuesslike?.myRecycleLike?.adapter = guesslike_Adapter

    }
    private fun loadCartData() {
        // 呼叫我們剛剛寫的管理員
        CloudCartManager.getCartItems { cartList ->
            // --- 當資料從 Firebase 回來時，會執行這裡 ---

            Log.d("ShoppingCart", "抓到資料了: ${cartList.size} 筆")

            // 1. 如果您的 Adapter 還是用 Array<String>，需要轉換一下 (或是建議 Adapter 也改成接收 CartItem)
            // 假設 Adapter 還是用 String:
            val nameList = cartList.map { it.name }

            // 2. 更新 RecyclerView
            // 這裡假設您的 Adapter 有一個 updateData 的方法
            // adapter.updateData(nameList)
            // adapter.notifyDataSetChanged()
        }
    }

    fun back(){
        val back = binding.shoppingCartBack
        back.setOnClickListener {
            finish()
        }
    }
}