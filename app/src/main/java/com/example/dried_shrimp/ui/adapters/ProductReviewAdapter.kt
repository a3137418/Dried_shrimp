package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.data.model.Review
import com.example.dried_shrimp.databinding.ItemProductReviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bumptech.glide.Glide // 🔥 記得 Import Glide
import com.example.dried_shrimp.R // 記得 Import R (為了預設圖)
class ProductReviewAdapter(
    private var reviews: List<Review>
) : RecyclerView.Adapter<ProductReviewAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProductReviewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews[position]
        holder.binding.apply {
            tvBuyerName.text = review.buyerName
            tvReviewContent.text = review.content
            ratingBar.rating = review.rating

            // 格式化時間
            val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            tvReviewDate.text = sdf.format(Date(review.timestamp))
            // 🔥 新增：載入頭像
            // 判斷是否有頭像網址
            if (review.buyerAvatar.isNotEmpty()) {
                Glide.with(root.context)
                    .load(review.buyerAvatar)
                    .placeholder(R.drawable.user) // 載入中顯示的預設圖 (請確認您有這張圖)
                    .error(R.drawable.user)       // 錯誤時顯示的圖
                    .circleCrop()                 // 自動裁切成圓形
                    .into(imgBuyerAvatar)
            } else {
                // 如果沒頭像，顯示預設圖
                imgBuyerAvatar.setImageResource(R.drawable.user)
            }
        }
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}