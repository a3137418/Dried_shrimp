package com.example.dried_shrimp

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button

class TicketActivity : AppCompatActivity() {

    // 定義變數
    private var quantity: Int = 1
    private val minQuantity = 1
    private val maxQuantity = 10 // 假設最多買10張

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.view_quantity_selector)

        // 綁定元件 (這裡示範用 findViewById，也可以用 ViewBinding)
        val btnDecrease = findViewById<Button>(R.id.btn_decrease)
        val btnIncrease = findViewById<Button>(R.id.btn_increase)
        val tvQuantity = findViewById<TextView>(R.id.tv_quantity)

        // 初始化顯示
        tvQuantity.text = quantity.toString()

        // --- 減號按鈕點擊事件 ---
        btnDecrease.setOnClickListener {
            if (quantity > minQuantity) {
                quantity--
                tvQuantity.text = quantity.toString()
            } else {
                // 選用：提示使用者不能再減了
                 Toast.makeText(this, "最少需要 1 位", Toast.LENGTH_SHORT).show()
            }
        }

        // --- 加號按鈕點擊事件 ---
        btnIncrease.setOnClickListener {
            if (quantity < maxQuantity) {
                quantity++
                tvQuantity.text = quantity.toString()
            } else {
                // 選用：提示達到上限
                 Toast.makeText(this, "最多只能買 10 張", Toast.LENGTH_SHORT).show()
            }
        }
    }
}