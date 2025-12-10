package com.example.dried_shrimp.ui.fragments

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
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.example.dried_shrimp.ui.activities.AccountSettingActivity
import com.example.dried_shrimp.ui.activities.Login
import com.example.dried_shrimp.ui.activities.MoreServeActivity
import com.example.dried_shrimp.ui.activities.RegisterActivity
import com.example.dried_shrimp.ui.activities.TabbedPurchaseListActivity
import com.example.dried_shrimp.ui.adapters.UserServiceAdapter
import com.google.firebase.auth.FirebaseAuth

class Fragment_user2 : Fragment() {

    private var binding: FragmentUser2Binding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUser2Binding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setListeners()
        updateUserUi()
    }

    override fun onResume() {
        super.onResume()
        // 從 Login / RegisterActivity 回來時再更新一次
        updateUserUi()
    }

    /**
     * 根據 Firebase 使用者狀態切換「未登入 / 已登入」版面
     */
    private fun updateUserUi() {
        val b = binding ?: return
        val user = FirebaseAuth.getInstance().currentUser

        // 這兩個是 <include> 對應的 binding
        val guestRoot = b.viewUserGuest.root
        val loginRoot = b.viewUserLogin.root

        if (user == null) {
            // 未登入：顯示 guest，隱藏 login
            guestRoot.visibility = View.VISIBLE
            loginRoot.visibility = View.GONE
        } else {
            // 已登入：顯示 login，隱藏 guest
            guestRoot.visibility = View.GONE
            loginRoot.visibility = View.VISIBLE

            // 已登入 layout 裡的元件
            val tvUserName: TextView = b.viewUserLogin.tvUserName
            val tvUserEmail: TextView = b.viewUserLogin.tvUserEmail
            val btnLogout: Button = b.viewUserLogin.btnLogout

            tvUserName.text = user.displayName ?: "皮蝦用戶"
            tvUserEmail.text = user.email ?: ""

            btnLogout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                updateUserUi()
            }
        }
    }

    /**
     * 設定 RecyclerView（更多服務 / 猜你喜歡）
     */
    private fun setupRecyclerViews() {
        val b = binding ?: return

        val myServiceAdapter = UserServiceAdapter()
        b.sectionMoreServices.myRecycleServe.layoutManager =
            GridLayoutManager(requireContext(), 2)
        b.sectionMoreServices.myRecycleServe.adapter = myServiceAdapter

        val customAdapter = GuessLikeAdapter()
        b.sectionGuesslike.myRecycleLike.layoutManager =
            GridLayoutManager(requireContext(), 2)
        b.sectionGuesslike.myRecycleLike.adapter = customAdapter
    }

    /**
     * 其它點擊事件（登入 / 註冊 / 購買清單 tab / 更多服務）
     */
    private fun setListeners() {
        val b = binding ?: return

        // 🔹 未登入版的登入 / 註冊按鈕（view_user_guest.xml 裡的）
        b.viewUserGuest.btnLogin.setOnClickListener {
            startActivity(Intent(requireContext(), Login::class.java))
        }
        b.viewUserGuest.btnRegister.setOnClickListener {
            startActivity(Intent(requireContext(), RegisterActivity::class.java))
        }

        // 🔹 右上角設定 icon
        b.imgSetting.setOnClickListener {
            startActivity(Intent(requireContext(), AccountSettingActivity::class.java))
        }

        // 🔹 購買清單 四個狀態 → 對應 TabbedPurchaseListActivity 的不同 Tab

        // 待付款
        b.sectionPurchaseList.imgPayment.setOnClickListener {
            val intentTab = Intent(context, TabbedPurchaseListActivity::class.java)
            intentTab.putExtra("tab_index", 0)
            startActivity(intentTab)
        }

        // 待出貨
        b.sectionPurchaseList.imgPendingShipment.setOnClickListener {
            val intentTab = Intent(context, TabbedPurchaseListActivity::class.java)
            intentTab.putExtra("tab_index", 1)
            startActivity(intentTab)
        }

        // 待收貨
        b.sectionPurchaseList.imgReceiving.setOnClickListener {
            val intentTab = Intent(context, TabbedPurchaseListActivity::class.java)
            intentTab.putExtra("tab_index", 2)
            startActivity(intentTab)
        }

        // 評價
        b.sectionPurchaseList.imgShoppingCompleted.setOnClickListener {
            val intentTab = Intent(context, TabbedPurchaseListActivity::class.java)
            intentTab.putExtra("tab_index", 3)
            startActivity(intentTab)
        }

        // 購買清單_查看全部
        b.sectionPurchaseList.tvseeall1.setOnClickListener {
            val intent = Intent(requireContext(), TabbedPurchaseListActivity::class.java)
            startActivity(intent)
        }

        // 更多服務_查看全部
        b.sectionMoreServices.tvseeall2.setOnClickListener {
            val intent = Intent(requireContext(), MoreServeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
