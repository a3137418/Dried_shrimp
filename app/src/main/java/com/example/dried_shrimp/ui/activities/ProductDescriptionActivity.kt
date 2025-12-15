package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.databinding.ActivityProductDescriptionBinding
import com.example.dried_shrimp.databinding.ActivityProductDetailBinding
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ProductDescriptionActivity : AppCompatActivity() {
    lateinit var binding: ActivityProductDescriptionBinding
    private val storage = FirebaseStorage.getInstance()
    private var selectedUri: Uri? = null
    private var uploadedImageUrl: String = "" // 存已經上傳好的網址

    private lateinit var imgPreview: ImageView
    private lateinit var tvDone: TextView

    // 選圖
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedUri = uri
            imgPreview.visibility = View.VISIBLE
            imgPreview.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDescriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etDescription = findViewById<EditText>(R.id.et_description)
        tvDone = binding.tvDone
        val imgBack = binding.imgBack
        val btnAddPhoto = binding.btnAddDescPhoto
        imgPreview =binding.imgDescPreview

        // 1. 接收舊資料 (文字 + 圖片網址)
        val oldText = intent.getStringExtra("CURRENT_DESC")
        val oldImgUrl = intent.getStringExtra("CURRENT_DESC_IMG")

        if (oldText != null) etDescription.setText(oldText)

        if (!oldImgUrl.isNullOrEmpty()) {
            uploadedImageUrl = oldImgUrl // 記錄舊的網址
            imgPreview.visibility = View.VISIBLE
            Glide.with(this).load(oldImgUrl).into(imgPreview)
        }

        // 2. 按鈕監聽
        imgBack.setOnClickListener { finish() }

        btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        tvDone.setOnClickListener {
            val newText = etDescription.text.toString().trim()

            // 判斷是否需要上傳新圖片
            if (selectedUri != null) {
                uploadImageAndFinish(newText)
            } else {
                // 沒選新圖，直接回傳舊的網址 (如果有)
                finishWithResult(newText, uploadedImageUrl)
            }
        }
    }

    private fun uploadImageAndFinish(text: String) {
        tvDone.text = "上傳中..."
        tvDone.isEnabled = false

        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("product_desc_images/$filename")

        ref.putFile(selectedUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    finishWithResult(text, uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "圖片上傳失敗", Toast.LENGTH_SHORT).show()
                tvDone.text = "完成"
                tvDone.isEnabled = true
            }
    }

    private fun finishWithResult(text: String, imgUrl: String) {
        val resultIntent = Intent()
        resultIntent.putExtra("NEW_DESC", text)
        resultIntent.putExtra("NEW_DESC_IMG", imgUrl) // 回傳圖片網址
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}