package com.example.dried_shrimp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dried_shrimp.databinding.FragmentUser2Binding
import com.google.firebase.auth.FirebaseAuth

class Fragment_user2: Fragment(){
    private var binding: FragmentUser2Binding ?= null
    private lateinit var userContainer: FrameLayout

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
        updateUserUi()
        setupRecyclerViews()
        setlisteners()


    }
    override fun onResume() {
        super.onResume()
        // 從 Login / register 回來時再更新一次
        updateUserUi()
    }

    private fun updateUserUi() {
        val user = FirebaseAuth.getInstance().currentUser
        val userContainer = binding?.userLayoutContainer

        // 先清掉舊的
        userContainer?.removeAllViews()

        if (user == null) {
            // 未登入：載入 guest layout
            val guestView = layoutInflater.inflate(
                R.layout.view_user_guest,
                userContainer,
                false
            )
            userContainer?.addView(guestView)

            val btnLogin = guestView.findViewById<Button>(R.id.btnLogin)
            val btnRegister = guestView.findViewById<Button>(R.id.btnRegister)

            btnLogin.setOnClickListener {
                startActivity(Intent(requireContext(), Login::class.java))
            }
            btnRegister.setOnClickListener {
                startActivity(Intent(requireContext(), register::class.java))
            }

        } else {
            // 已登入：載入 login layout
            val loginView = layoutInflater.inflate(
                R.layout.view_user_login,
                userContainer,
                false
            )
            userContainer?.addView(loginView)

            val tvUserName = loginView.findViewById<TextView>(R.id.tvUserName)
            val tvUserEmail = loginView.findViewById<TextView>(R.id.tvUserEmail)
            val btnLogout = loginView.findViewById<Button>(R.id.btnLogout)

            tvUserName.text = user.displayName ?: "皮蝦用戶"
            tvUserEmail.text = user.email ?: ""

            btnLogout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                updateUserUi()
            }
        }
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
        val btlogin: Button ?= binding?.viewUserGuest?.btnLogin
        btlogin?.setOnClickListener {
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
        }
        //註冊
        val btregister: Button ?= binding?.viewUserGuest?.btnRegister
        btregister?.setOnClickListener {
            val intent = Intent(requireContext(), register::class.java)
            startActivity(intent)
        }
        //待付款
        val img_payment = binding?.sectionPurchaseList?.imgPayment
        img_payment?.setOnClickListener {
            val intent_tab = Intent(context, Tabbed_purchase_list::class.java)
            intent_tab.putExtra("tab_index", 0)   // 要打開的 Tab 位置
            startActivity(intent_tab)
        }
        //待出貨
        val img_pending_shipment = binding?.sectionPurchaseList?.imgPendingShipment
        img_pending_shipment?.setOnClickListener {
            val intent_tab = Intent(context, Tabbed_purchase_list::class.java)
            intent_tab.putExtra("tab_index", 1)   // 要打開的 Tab 位置
            startActivity(intent_tab)
        }
        //待收貨
        val img_receiving = binding?.sectionPurchaseList?.imgReceiving
        img_receiving?.setOnClickListener {
            val intent_tab = Intent(context, Tabbed_purchase_list::class.java)
            intent_tab.putExtra("tab_index", 2)   // 要打開的 Tab 位置
            startActivity(intent_tab)
        }
        //評價
        val img_shopping_completed = binding?.sectionPurchaseList?.imgShoppingCompleted
        img_shopping_completed?.setOnClickListener {
            val intent_tab = Intent(context, Tabbed_purchase_list::class.java)
            intent_tab.putExtra("tab_index", 3)   // 要打開的 Tab 位置
            startActivity(intent_tab)
        }

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