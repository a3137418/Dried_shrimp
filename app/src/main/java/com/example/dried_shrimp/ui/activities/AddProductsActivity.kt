package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.ActivityAddProductsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddProductsActivity : AppCompatActivity() {
    lateinit var binding : ActivityAddProductsBinding
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // 變數
    private var currentDescription: String = ""
    private var currentDescImageUrl: String = ""
    private var currentCategory: String = "未分類"
    private var currentShippingFee: Int = 60
    private var selectedImageUri: Uri? = null
    private var editingProduct: Product? = null

    // Launcher 定義 (保持不變)
    private val categoryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val selected = result.data?.getStringExtra("SELECTED_CATEGORY")
            if (selected != null) {
                currentCategory = selected
                binding.itemFormRowClassification.tvClassification.text = selected
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            binding.imgPreview.setImageURI(uri)
            binding.imgPreview.visibility = View.VISIBLE
            binding.tvAddPhotoText.visibility = View.GONE
        }
    }

    private val descriptionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val text = result.data?.getStringExtra("NEW_DESC") ?: ""
            val imgUrl = result.data?.getStringExtra("NEW_DESC_IMG") ?: ""
            currentDescription = text
            currentDescImageUrl = imgUrl
            binding.itemFormRowProductdescription.tvValue.text = if (text.isEmpty()) "未填寫" else "已填寫"
        }
    }

    private val shippingFeeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val fee = result.data?.getIntExtra("NEW_FEE", 60) ?: 60
            currentShippingFee = fee
            val tvFee = findViewById<android.widget.TextView>(R.id.tv_shipping_fee_display)
            tvFee.text = "NT$$fee >"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. 接收編輯資料
        editingProduct = intent.getSerializableExtra("EDIT_PRODUCT") as? Product

        // 2. 根據是否為編輯模式，設定按鈕邏輯
        if (editingProduct != null) {
            setupForEdit(editingProduct!!)
        } else {
            // --- 新增模式 ---
            // 左邊「儲存」按鈕先隱藏 (或是您可以留著當存草稿，這邊先隱藏簡化)
            binding.btnSave.visibility = View.GONE

            // 右邊按鈕顯示「上架」
            binding.btnPublish.text = "上架"
            binding.btnPublish.setOnClickListener {
                prepareToPublish(targetStatus = "ON_SHELF")
            }
        }

        setupCommonListeners()
    }

    private fun setupCommonListeners() {
        binding.imgBackAddproducts.setOnClickListener { finish() }

        binding.btnAddPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 分類、描述、運費的監聽器保持不變...
        binding.itemFormRowClassification.root.setOnClickListener {
            val intent = Intent(this, CategorySelectionActivity::class.java)
            categoryLauncher.launch(intent)
        }
        binding.itemFormRowProductdescription.root.setOnClickListener {
            val intent = Intent(this, ProductDescriptionActivity::class.java)
            intent.putExtra("CURRENT_DESC", currentDescription)
            intent.putExtra("CURRENT_DESC_IMG", currentDescImageUrl)
            descriptionLauncher.launch(intent)
        }
        binding.layoutShippingFee.setOnClickListener {
            val intent = Intent(this, ShippingFeeActivity::class.java)
            intent.putExtra("CURRENT_FEE", currentShippingFee)
            shippingFeeLauncher.launch(intent)
        }
    }

    // --- 設定編輯模式 UI 與 按鈕邏輯 (重點在這裡) ---
    private fun setupForEdit(product: Product) {
        // ... (填入舊資料的程式碼與之前相同，省略以節省篇幅) ...
        binding.etProductName.setText(product.name)
        binding.itemFormRowPriceSetting.etInput.setText(product.price.toString())
        binding.itemFormRowQuantity.etInput.setText(product.stock.toString())
        binding.itemFormRowMin.etInput.setText(product.minQuantity.toString())
        currentCategory = product.category
        binding.itemFormRowClassification.tvClassification.text = product.category
        currentDescription = product.description
        currentDescImageUrl = product.descImageUrl
        binding.itemFormRowProductdescription.tvValue.text = if (product.description.isEmpty()) "未填寫" else "已填寫"
        currentShippingFee = product.shippingFee
        binding.layoutShippingFee.findViewById<android.widget.TextView>(R.id.tv_shipping_fee_display).text = "NT$${product.shippingFee} >"
        if (product.imageUrl.isNotEmpty()) {
            binding.tvAddPhotoText.visibility = View.GONE
            binding.imgPreview.visibility = View.VISIBLE
            Glide.with(this).load(product.imageUrl).into(binding.imgPreview)
        }

        // ★★★ 關鍵修改：按鈕切換邏輯 ★★★

        if (product.status == "OFF_SHELF") {
            // --- 情境 A：編輯「未上架」商品 ---

            // 左邊按鈕：顯示為「儲存」 (只存檔，不公開)
            binding.btnSave.visibility = View.VISIBLE
            binding.btnSave.text = "儲存"
            binding.btnSave.setOnClickListener {
                prepareToPublish(targetStatus = "OFF_SHELF")
            }

            // 右邊按鈕：顯示為「重新上架」 (存檔 + 公開)
            binding.btnPublish.text = "重新上架"
            binding.btnPublish.setOnClickListener {
                prepareToPublish(targetStatus = "ON_SHELF")
            }

        } else {
            // --- 情境 B：編輯「架上」商品 ---

            // 隱藏左邊按鈕 (因為已經在架上了，不需要存成草稿)
            binding.btnSave.visibility = View.GONE

            // 右邊按鈕：顯示為「更新」
            binding.btnPublish.text = "更新"
            binding.btnPublish.setOnClickListener {
                // 維持原本狀態 (ON_SHELF)
                prepareToPublish(targetStatus = product.status)
            }
        }
    }

    // --- 準備上傳 (含圖片與Firestore) ---
    private fun prepareToPublish(targetStatus: String) {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.itemFormRowPriceSetting.etInput.text.toString().trim()

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "請輸入完整資訊", Toast.LENGTH_SHORT).show()
            return
        }

        // 鎖定按鈕
        binding.btnPublish.isEnabled = false
        binding.btnSave.isEnabled = false
        binding.btnPublish.text = "處理中..."

        // 判斷圖片邏輯
        if (selectedImageUri != null) {
            uploadImageToStorage(targetStatus)
        } else if (editingProduct != null) {
            saveProductToFirestore(editingProduct!!.imageUrl, targetStatus)
        } else {
            saveProductToFirestore("", targetStatus)
        }
    }

    private fun uploadImageToStorage(targetStatus: String) {
        val uri = selectedImageUri ?: return
        val filename = UUID.randomUUID().toString()
        val ref = storage.reference.child("product_images/$filename")

        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveProductToFirestore(downloadUri.toString(), targetStatus)
                }
            }
            .addOnFailureListener { e ->
                resetButtons()
                Toast.makeText(this, "圖片上傳失敗：${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProductToFirestore(imageUrl: String, targetStatus: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        // 取得 UI 數值
        val name = binding.etProductName.text.toString().trim()
        val price = binding.itemFormRowPriceSetting.etInput.text.toString().toIntOrNull() ?: 0
        val stock = binding.itemFormRowQuantity.etInput.text.toString().toIntOrNull() ?: 0
        val minQty = binding.itemFormRowMin.etInput.text.toString().toIntOrNull() ?: 1

        val docRef = if (editingProduct != null) {
            db.collection("products").document(editingProduct!!.id)
        } else {
            db.collection("products").document()
        }

        val finalProduct = Product(
            id = docRef.id,
            sellerId = user.uid,
            name = name,
            description = currentDescription,
            descImageUrl = currentDescImageUrl,
            category = currentCategory,
            price = price,
            stock = stock,
            minQuantity = minQty,
            shippingFee = currentShippingFee,
            imageUrl = imageUrl,
            status = targetStatus // ★ 這是關鍵：ON_SHELF 或 OFF_SHELF
        )

        val batch = db.batch()
        batch.set(docRef, finalProduct)
        val myStoreRef = db.collection("users").document(user.uid)
            .collection("my_store_products").document(docRef.id)
        batch.set(myStoreRef, finalProduct)

        batch.commit()
            .addOnSuccessListener {
                // 根據狀態顯示不同成功訊息
                val msg = if (targetStatus == "ON_SHELF") "已上架！" else "已儲存修改！"
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                resetButtons()
                Toast.makeText(this, "失敗：${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun resetButtons() {
        binding.btnPublish.isEnabled = true
        binding.btnSave.isEnabled = true
        // 恢復按鈕文字 (簡單處理)
        binding.btnPublish.text = if (editingProduct != null) "更新/上架" else "上架"
    }
}