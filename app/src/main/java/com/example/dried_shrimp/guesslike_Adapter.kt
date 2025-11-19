package com.example.dried_shrimp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class guesslike_Adapter() :
    RecyclerView.Adapter<guesslike_Adapter.ViewHolder>() {
    var data_images :Array<String> = arrayOf(
        "car001","car002","car003","car004","car005",
        "car006","car007","car008","car009","car010",
        "car011","car012","car013","car014","car015",
        "car016","car017","car018","car019","car020",
    )
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val item_image: ImageView

        init {
            item_image = view.findViewById(R.id.item_image)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_guesslike, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val context = viewHolder.item_image.context
        val resid = context.resources.getIdentifier(data_images[position], "drawable", context.packageName)
        Glide.with(viewHolder.itemView.context) // 1. 提供上下文(Context)
            .load(resid)              // 2. 告訴 Glide 要載入哪個圖片資源
            .into(viewHolder.item_image)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = data_images.size

}