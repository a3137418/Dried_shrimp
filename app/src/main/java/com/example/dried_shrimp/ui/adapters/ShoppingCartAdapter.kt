package com.example.dried_shrimp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.R

class ShoppingCartAdapter() :
    RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>() {
    var shopping_data: MutableList<String> = mutableListOf()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        lateinit var  shopping_cart_checkBox: CheckBox
        lateinit var img_shopping_commodity: ImageView
        lateinit var  tv_shopping_cart_name: TextView
        lateinit var  spinner_shopping_cart: Spinner
        lateinit var  layout_adult_ticket: LinearLayout
        lateinit var  tv_shopping_price: TextView
        init {
            shopping_cart_checkBox = view.findViewById(R.id.shopping_cart_checkBox)
            img_shopping_commodity = view.findViewById(R.id.img_shopping_commodity)
            tv_shopping_cart_name = view.findViewById(R.id.tv_shopping_cart_name)
            spinner_shopping_cart = view.findViewById(R.id.spinner_shopping_cart)
            layout_adult_ticket = view.findViewById(R.id.layout_adult_ticket)
            tv_shopping_price = view.findViewById(R.id.tv_shopping_price)
        }


    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_shopping_cart, viewGroup, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.tv_shopping_cart_name.text = shopping_data[position]
    }

    override fun getItemCount() = shopping_data.size

}