package com.example.dried_shrimp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dried_shrimp.databinding.FragmentUser2Binding

class Fragment_user2: Fragment(){
    private var binding: FragmentUser2Binding ?= null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUser2Binding.inflate(inflater,container,false)
        val view =binding?.root
        return view

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setlisteners()


    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun setupRecyclerViews() {
        val myserive_Adapter = user_serive_Adapter()
        binding?.sectionMoreServices?.myRecycleServe?.layoutManager = GridLayoutManager(requireContext(), 2)
        binding?.sectionMoreServices?.myRecycleServe?.adapter = myserive_Adapter

        val customAdapter = guesslike_Adapter()
        binding?.sectionGuesslike?.myRecycleLike?.layoutManager = GridLayoutManager(requireContext(),2)
        binding?.sectionGuesslike?.myRecycleLike?.adapter = customAdapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    fun setlisteners(){
        //登入
        val btlogin: Button ?= binding?.btlogin
        btlogin?.setOnClickListener {
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
        }
        //註冊
        val btregister: Button ?= binding?.btregister
        btregister?.setOnClickListener {
            val intent = Intent(requireContext(), register::class.java)
            startActivity(intent)
        }
        //待付款
        val img_payment = binding?.sectionPurchaseList?.imgPayment
        img_payment?.setOnClickListener {
            val intent = Intent(requireContext(), Tabbed_purchase_list::class.java)
            startActivity(intent)
        }
        //待出貨
        //待收貨
        //評價


        //購買清單_查看全部
        val tvseeall1_textview : TextView? = binding?.sectionPurchaseList?.tvseeall1
        tvseeall1_textview?.setOnClickListener {
            val intent = Intent(requireContext(), Tabbed_purchase_list::class.java)
            startActivity(intent)
        }
        //更多服務_查看全部
        val tvseeall2_textview : TextView? = binding?.sectionMoreServices?.tvseeall2
        tvseeall2_textview?.setOnClickListener {
            val intent = Intent(requireContext(), Activity_more_serve::class.java)
            startActivity(intent)
        }
    }


}