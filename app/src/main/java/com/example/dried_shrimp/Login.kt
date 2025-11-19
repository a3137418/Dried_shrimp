package com.example.dried_shrimp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dried_shrimp.databinding.ActivityLoginBinding

class Login : AppCompatActivity() {
    lateinit var binding : ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        back()


        val tv_register = binding.tvRegister

        val fullText = "還沒有皮蝦帳號嗎?註冊"
        val spannable = SpannableString(fullText)

        val start = fullText.indexOf("註冊")
        val end = start + 2 // "註冊"兩個字

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@Login, register::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false       // 是否要下底線（預設有）
                ds.color = Color.parseColor("#FF7F00") // 設定點擊文字顏色（可改）
            }
        }

        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tv_register.text = spannable
        tv_register.movementMethod = LinkMovementMethod.getInstance()
        tv_register.highlightColor = Color.TRANSPARENT  // 點擊時不出現藍色底
    }

    fun back(){
        val back = binding.loginBack
        back.setOnClickListener {
            finish()
        }
    }
}