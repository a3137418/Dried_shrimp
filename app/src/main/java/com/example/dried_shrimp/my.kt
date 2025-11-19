package com.example.dried_shrimp

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class my : AppCompatActivity() {
    lateinit var img_myaccount: ImageView
    lateinit var btlogin: Button
    lateinit var btregister: Button
    lateinit var tvPurchase_list: TextView
    lateinit var tvseeall1: TextView
    lateinit var img_payment:ImageView
    lateinit var img_pending_shipment: ImageView
    lateinit var img_receiving: ImageView
    lateinit var img_evaluate: ImageView
    lateinit var img_tvETickets: ImageView
    lateinit var img_ticket: ImageView
    lateinit var img_free_delivery: ImageView
    lateinit var img_percent_30: ImageView
    lateinit var img_percent_50: ImageView
    lateinit var img_coin: ImageView
    lateinit var img_ticket2: ImageView
    lateinit var img_wallet: ImageView
    lateinit var tvmore_Serve: TextView
    lateinit var tvseeall2: TextView
    lateinit var my_recycle_Serve: RecyclerView
    lateinit var my_recycle_like: RecyclerView
    lateinit var tvBuy_and_return: TextView
    lateinit var tvHelp_Center: TextView
    lateinit var tvcustomer: TextView
    lateinit var tvcall_customer: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findview()
        adapter()
    }

    fun findview(){
        img_myaccount = findViewById(R.id.img_myaccount)
        btlogin = findViewById(R.id.btlogin)
        btregister = findViewById(R.id.btregister)
        tvPurchase_list = findViewById(R.id.tvPurchase_list)
        tvseeall1 = findViewById(R.id.tvseeall1)
        img_payment = findViewById(R.id.img_payment)
        img_pending_shipment = findViewById(R.id.img_pending_shipment)
        img_receiving = findViewById(R.id.img_receiving)
        img_evaluate = findViewById(R.id.img_evaluate)
        img_tvETickets = findViewById(R.id.img_tvETickets)
        img_ticket = findViewById(R.id.img_ticket)
        img_free_delivery = findViewById(R.id.img_free_delivery)
        img_percent_30 = findViewById(R.id.img_percent_30)
        img_percent_50 = findViewById(R.id.img_percent_50)
        img_coin = findViewById(R.id.img_coin)
        img_ticket2 = findViewById(R.id.img_ticket2)
        img_wallet = findViewById(R.id.img_wallet)
        tvmore_Serve = findViewById(R.id.tvmore_Serve)
        tvseeall2 = findViewById(R.id.tvseeall2)
        my_recycle_Serve = findViewById(R.id.my_recycle_Serve)
        my_recycle_like = findViewById(R.id.my_recycle_like)
        tvBuy_and_return = findViewById(R.id.tvBuy_and_return)
        tvHelp_Center = findViewById(R.id.tvHelp_Center)
        tvcustomer = findViewById(R.id.tvcustomer)
        tvcall_customer = findViewById(R.id.tvcall_customer)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.my_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        whenitem(item)
        return super.onOptionsItemSelected(item)
    }
    fun whenitem(item: MenuItem){
        when(item.itemId){
            R.id.settings ->{

            }
            R.id.shopping_cart ->{

            }
            R.id.chat ->{

            }
        }
    }
    fun adapter() {
        val myserive_Adapter = user_serive_Adapter()
        my_recycle_Serve.layoutManager = GridLayoutManager(this, 2)
        my_recycle_Serve.adapter = myserive_Adapter

        val customAdapter = guesslike_Adapter()
        my_recycle_like.layoutManager = GridLayoutManager(this,2)
        my_recycle_like.adapter = customAdapter
    }
}