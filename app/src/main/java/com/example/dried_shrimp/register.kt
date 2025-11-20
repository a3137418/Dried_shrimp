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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.dried_shrimp.databinding.ActivityRegisterBinding
class register : AppCompatActivity() {

    lateinit var binding : ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 上方 header 避免與狀態列重疊
        ViewCompat.setOnApplyWindowInsetsListener(binding.registerHeader) { view, insets ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.setPadding(view.paddingLeft, statusBarHeight, view.paddingRight, view.paddingBottom)
            insets
        }
        // ⭐ 底部指紋認證區避免被導航列蓋住
        ViewCompat.setOnApplyWindowInsetsListener(binding.linearLayout2) { view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottomInset)
            insets
        }
        Login_page()
        back()
    }

    fun Login_page(){
        val tvLogin = binding.tvLogin
        val fullText = "已經有皮蝦帳號了嗎?登入"
        val spannable = SpannableString(fullText)
        val start = fullText.indexOf("登入")
        val end = start + 2 // "登入"兩個字
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@register, Login::class.java)
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false       // 是否要下底線（預設有）
                ds.color = Color.parseColor("#FF7F00") // 設定點擊文字顏色（可改）
            }
        }
        spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvLogin.text = spannable
        tvLogin.movementMethod = LinkMovementMethod.getInstance()
        tvLogin.highlightColor = Color.TRANSPARENT  // 點擊時不出現藍色底
    }
    fun back(){
        val back = binding.registerBack
        back.setOnClickListener {
            finish()
        }
    }
}