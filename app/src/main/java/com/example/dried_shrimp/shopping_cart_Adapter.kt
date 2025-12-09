//package com.example.dried_shrimp
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.CheckBox
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.Spinner
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//
//
//class shopping_cart_Adapter(private val dataSet: Array<String>) :
//    RecyclerView.Adapter<shopping_cart_Adapter.ViewHolder>() {
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val shopping_cart_checkBox: CheckBox
//        val img_shopping_commodity: ImageView
//        val tv_shopping_cart_name: TextView
//        val spinner_shopping_cart: Spinner
//        val layout_adult_ticket: LinearLayout
//        val tv_shopping_price: TextView
//        init {
//            textView = view.findViewById(R.id.textView)
//        }
//    }
//
//    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
//
//        val view = LayoutInflater.from(viewGroup.context)
//            .inflate(R.layout.item_shopping_cart, viewGroup, false)
//
//        return ViewHolder(view)
//    }
//
//
//    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//
//        viewHolder.textView.text = dataSet[position]
//    }
//
//    override fun getItemCount() = dataSet.size
//
//}