package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dried_shrimp.databinding.ActivityAccountSettingBinding

class AccountSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAccountSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccountSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 上方 header 避免與狀態列重疊
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginHeader) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, statusBarHeight, view.paddingRight, view.paddingBottom)
            insets
        }
        back()
    }

    fun back(){
        val back = binding.accountBack
        back.setOnClickListener {
            finish()
        }
    }
}