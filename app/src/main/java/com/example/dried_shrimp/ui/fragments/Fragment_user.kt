package com.example.dried_shrimp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dried_shrimp.R
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.example.dried_shrimp.ui.adapters.UserServiceAdapter

class Fragment_user: Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_user, null)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var img_myaccount: ImageView = view.findViewById(R.id.img_myaccount)
        var btlogin: Button = view.findViewById(R.id.btlogin)
        var btregister: Button = view.findViewById(R.id.btregister)
        var tvPurchase_list: TextView = view.findViewById(R.id.tvPurchase_list)
        var tvseeall1: TextView = view.findViewById(R.id.tvseeall1)
        var img_payment:ImageView = view.findViewById(R.id.img_payment)
        var img_pending_shipment: ImageView = view.findViewById(R.id.img_pending_shipment)
        var img_receiving: ImageView = view.findViewById(R.id.img_receiving)
        var img_evaluate: ImageView = view.findViewById(R.id.img_evaluate)
        var img_tvETickets: ImageView = view.findViewById(R.id.img_tvETickets)
        var img_ticket: ImageView = view.findViewById(R.id.img_ticket)
        var img_free_delivery: ImageView = view.findViewById(R.id.img_free_delivery)
        var img_percent_30: ImageView = view.findViewById(R.id.img_percent_30)
        var img_percent_50: ImageView = view.findViewById(R.id.img_percent_50)
        var img_coin: ImageView = view.findViewById(R.id.img_coin)
        var img_ticket2: ImageView = view.findViewById(R.id.img_ticket2)
        var img_wallet: ImageView = view.findViewById(R.id.img_wallet)
        var tvmore_Serve: TextView = view.findViewById(R.id.tvmore_Serve)
        var tvseeall2: TextView = view.findViewById(R.id.tvseeall2)
        var my_recycle_Serve: RecyclerView = view.findViewById(R.id.my_recycle_Serve)
        var my_recycle_like: RecyclerView = view.findViewById(R.id.my_recycle_like)
        var tvBuy_and_return: TextView = view.findViewById(R.id.tvBuy_and_return)
        var tvHelp_Center: TextView = view.findViewById(R.id.tvHelp_Center)
        var tvcustomer: TextView = view.findViewById(R.id.tvcustomer)
        var tvcall_customer: TextView = view.findViewById(R.id.tvcall_customer)



        fun adapter() {
            val myserive_Adapter = UserServiceAdapter()
            my_recycle_Serve.layoutManager = GridLayoutManager(context, 2)
            my_recycle_Serve.adapter = myserive_Adapter

            val customAdapter = GuessLikeAdapter()
            my_recycle_like.layoutManager = GridLayoutManager(context,2)
            my_recycle_like.adapter = customAdapter
        }

        adapter()


    }
}