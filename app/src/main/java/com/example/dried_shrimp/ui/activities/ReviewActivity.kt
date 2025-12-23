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

        binding.btnSubmitReview.setOnClickListener { submitReview() }
    }

    private fun submitReview() {
        val order = currentOrder ?: return
        val rating = binding.ratingBar.rating
        val comment = binding.etReviewComment.text.toString()

        if (rating == 0f) {
            Toast.makeText(this, "請選擇星等", Toast.LENGTH_SHORT).show()
            return
        }

        val review = Review(
            reviewId = db.collection("reviews").document().id,
            orderId = order.orderId,
            productId = order.items.firstOrNull()?.productId ?: "",
            buyerId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            rating = rating,
            comment = comment
        )

        // 1. 寫入評價
        db.collection("reviews").document(review.reviewId).set(review)
            .addOnSuccessListener {
                // 2. 更新訂單狀態為「已評價」
                updateOrderStatus(order)
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