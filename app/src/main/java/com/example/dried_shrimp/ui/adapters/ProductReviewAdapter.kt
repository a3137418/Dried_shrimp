package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.data.model.Review
import com.example.dried_shrimp.databinding.ItemProductReviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductReviewAdapter(
    private var reviews: List<Review>
) : RecyclerView.Adapter<ProductReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(val binding: ItemProductReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemProductReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        holder.binding.apply {
            tvReviewerName.text = review.buyerName.ifEmpty { "匿名用戶" }
            tvReviewContent.text = review.comment
            rbReviewRating.rating = review.rating

            // 格式化時間戳記
            try {
                val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                tvReviewDate.text = sdf.format(Date(review.timestamp))
            } catch (e: Exception) {
                tvReviewDate.text = ""
            }
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}