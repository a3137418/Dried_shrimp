package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.data.model.Review
import com.example.dried_shrimp.databinding.ActivityReviewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentOrder: Order? = null
    private var auth = FirebaseAuth.getInstance()
    private var myName: String = "匿名買家"
    private var myAvatar: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order

        // 顯示商品資訊
        val item = currentOrder?.items?.firstOrNull()
        binding.tvProductName.text = item?.name ?: "商品"
        binding.tvProductPrice.text = "$${item?.price ?: 0}"
        if (item?.imageUrl?.isNotEmpty() == true) {
            Glide.with(this).load(item.imageUrl).into(binding.imgProductThumb)
        }
        loadMyInfo()
        binding.btnSubmitReview.setOnClickListener { submitReview() }
    }
    // 🔥 新增這個函式：從 Firestore 讀取當前使用者的資料
    private fun loadMyInfo() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // 1. 抓取名字 (Log 顯示欄位是 "name")
                    myName = document.getString("name") ?: "匿名買家"

                    // 2. 抓取頭像 (Log 顯示欄位是 "photoUrl" 或 "imageUrl")
                    // ★★★ 關鍵修改：改成讀取 "photoUrl" ★★★
                    myAvatar = document.getString("photoUrl")
                        ?: document.getString("imageUrl") // 雙重保險，如果 photoUrl 沒拿到就拿 imageUrl
                                ?: ""

                    android.util.Log.d("ReviewActivity", "成功抓取資料 - 名字: $myName, 頭像: $myAvatar")
                }
            }
            .addOnFailureListener {
                android.util.Log.e("ReviewActivity", "讀取使用者失敗", it)
            }
    }

    private fun submitReview() {
        val rating = binding.ratingBar.rating
        val content = binding.etComment.text.toString()

        if (rating == 0f) {
            Toast.makeText(this, "請選擇評分星數", Toast.LENGTH_SHORT).show()
            return
        }

        val order = currentOrder ?: return
        // 抓出 productId
        val targetProductId = order.items.firstOrNull()?.productId ?: ""

        if (targetProductId.isEmpty()) {
            Toast.makeText(this, "商品資料異常，無法評價", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. 準備評價資料
        val reviewId = db.collection("reviews").document().id
        val reviewData = hashMapOf(
            "reviewId" to reviewId,
            "orderId" to order.orderId,
            "productId" to targetProductId, // 這是關鍵，用來連結商品
            "buyerId" to auth.currentUser?.uid,
            "sellerId" to order.sellerId,
            "rating" to rating,
            "content" to content,
            "timestamp" to System.currentTimeMillis(),
            "buyerName" to myName, // 建議從 User 資料讀取真實暱稱
            "buyerAvatar" to myAvatar       // 建議從 User 資料讀取頭像
        )

        val batch = db.batch()

        // 2. 寫入 reviews 集合 (詳細內容放這裡)
        val reviewRef = db.collection("reviews").document(reviewId)
        batch.set(reviewRef, reviewData)

        // 3. 更新訂單狀態 (標記為已評價)
        val orderRef = db.collection("users").document(order.buyerId)
            .collection("history_orders").document(order.orderId)
        batch.update(orderRef, "hasReviewed", true)

        // 4. 提交上述變更
        batch.commit()
            .addOnSuccessListener {
                // 1. 更新該商品的平均分 (原本就有的)
                updateProductRating(targetProductId, rating)

                // 2. 🔥 新增：更新賣家的總平均分
                updateSellerRating(order.sellerId, rating)
            }
            .addOnFailureListener {
                Toast.makeText(this, "評價提交失敗: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    // 🔥 新增這個函式：用來更新「賣家 (User)」集合裡的評分
    private fun updateSellerRating(sellerId: String, newRating: Float) {
        val sellerRef = db.collection("users").document(sellerId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(sellerRef)

            // 讀取目前的賣家評價數據
            // 注意：我們把欄位取名為 sellerRating 和 sellerReviewCount
            val currentRating = snapshot.getDouble("sellerRating") ?: 0.0
            val currentCount = snapshot.getLong("sellerReviewCount") ?: 0

            // 計算新的平均分數
            val newCount = currentCount + 1
            val totalScore = (currentRating * currentCount) + newRating
            val newAverageRating = totalScore / newCount

            // 更新賣家文件
            transaction.update(sellerRef, "sellerRating", newAverageRating)
            transaction.update(sellerRef, "sellerReviewCount", newCount)
        }.addOnSuccessListener {
            // 這裡不用特別跳 Toast，因為 updateProductRating 那邊會跳
            android.util.Log.d("ReviewActivity", "賣家評分更新成功")
        }.addOnFailureListener { e ->
            android.util.Log.e("ReviewActivity", "賣家評分更新失敗", e)
        }
    }
    // 🔥 新增這個函式：用來更新產品集合裡的數字
    private fun updateProductRating(productId: String, newRating: Float) {
        val productRef = db.collection("products").document(productId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(productRef)

            // 讀取目前的評價數據 (如果沒有就預設為 0)
            val currentRating = snapshot.getDouble("rating") ?: 0.0
            val currentCount = snapshot.getLong("reviewCount") ?: 0

            // 計算新的平均分數
            // 公式：(舊總分 + 新分數) / 新總人數
            val newCount = currentCount + 1
            val totalScore = (currentRating * currentCount) + newRating
            val newAverageRating = totalScore / newCount

            // 更新產品欄位
            transaction.update(productRef, "rating", newAverageRating)
            transaction.update(productRef, "reviewCount", newCount)
        }.addOnSuccessListener {
            Toast.makeText(this, "評價成功！", Toast.LENGTH_SHORT).show()
            finish() // 關閉頁面
        }.addOnFailureListener { e ->
            // 就算更新平均分失敗，評價其實已經寫入了，所以還是讓使用者離開
            Toast.makeText(this, "評價成功 (分數更新延遲)", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateOrderStatus(order: Order) {
        val data = mapOf("hasReviewed" to true)
        val batch = db.batch()

        // 更新 history_orders (這是列表顯示的來源)
        val historyRef = db.collection("users")
            .document(order.buyerId)
            .collection("history_orders")
            .document(order.orderId)

        batch.set(historyRef, data, SetOptions.merge())
        batch.commit().addOnSuccessListener {
            Toast.makeText(this, "評價成功", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}