package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dried_shrimp.R

import com.example.dried_shrimp.databinding.ActivityShippingFeeBinding
import kotlin.toString

class ShippingFeeActivity : AppCompatActivity() {
    lateinit var binding : ActivityShippingFeeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShippingFeeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setlistener()
    }
    fun setlistener(){
        val etFee = binding.etShippingFee
        val tvSave = binding.tvSave
        val imgBack = binding.imgBack
        // 1. 如果之前有設定過，顯示舊的金額
        val oldFee = intent.getIntExtra("CURRENT_FEE", 0)
        if (oldFee > 0) {
            etFee.setText(oldFee.toString())
        }

        // 2. 儲存按鈕
        tvSave.setOnClickListener {
            val feeText = etFee.text.toString().trim()
            if (feeText.isEmpty()) {
                Toast.makeText(this, "請輸入運費", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fee = feeText.toIntOrNull() ?: 60

            // 回傳資料
            val resultIntent = Intent()
            resultIntent.putExtra("NEW_FEE", fee)
            setResult(RESULT_OK, resultIntent)

            finish()
        }

        imgBack.setOnClickListener { finish() }
    }
}