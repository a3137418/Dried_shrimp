package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dried_shrimp.R

class MoreServiceAdapter() :
    RecyclerView.Adapter<MoreServiceAdapter.ViewHolder>() {
    var data_images :Array<String> = arrayOf(
        "more001","more002","more003","more004","more005",
        "more006","more007","more008","more009","more010",
        "more011","more012",
    )
    val data1 = arrayOf(
        "蝦皮直營","蝦皮會員","蝦皮消消樂","蝦皮分潤計畫","按讚好物",
        "再買一次","直播短影音","瀏覽紀錄","蝦皮實名認證","關務署實名認證資訊",
        "交易支付及退款查詢","品牌會員"
    )
    val data2 = arrayOf(
        "當日到貨免運5折起","銅蝦會員","快來果園種蝦幣種子!","加入來賺10%+非潤金","",
        "","","","","",
        "","",
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val myserive_title1: TextView
        val myserive_content1 : TextView
        val more_image : ImageView

        init {
            // Define click listener for the ViewHolder's View
            myserive_title1 = view.findViewById(R.id.myserive_title1)
            myserive_content1 = view.findViewById(R.id.myserive_content1)
            more_image = view.findViewById(R.id.more_image)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_recycle_serve, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.myserive_title1.text = data1[position]
        viewHolder.myserive_content1.text = data2[position]
        val context = viewHolder.more_image.context
        val resid = context.resources.getIdentifier(data_images[position], "drawable", context.packageName)
        Glide.with(viewHolder.itemView.context) // 1. 提供上下文(Context)
            .load(resid)              // 2. 告訴 Glide 要載入哪個圖片資源
            .into(viewHolder.more_image)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = data1.size

}