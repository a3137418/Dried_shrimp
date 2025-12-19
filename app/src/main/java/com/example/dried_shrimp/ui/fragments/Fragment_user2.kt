package com.example.dried_shrimp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.dried_shrimp.data.model.Product
import com.example.dried_shrimp.databinding.FragmentUser2Binding
import com.example.dried_shrimp.ui.adapters.GuessLikeAdapter
import com.example.dried_shrimp.ui.activities.AccountSettingActivity
import com.example.dried_shrimp.ui.activities.Login
import com.example.dried_shrimp.ui.activities.MoreServeActivity
import com.example.dried_shrimp.ui.activities.MyStoreActivity
import com.example.dried_shrimp.ui.activities.RegisterActivity
import com.example.dried_shrimp.ui.activities.ShoppingCartActivity
import com.example.dried_shrimp.ui.activities.TabbedPurchaseListActivity
import com.example.dried_shrimp.R
import com.example.dried_shrimp.ui.activities.EditProfileActivity
import com.example.dried_shrimp.ui.adapters.UserServiceAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Fragment_user2 : Fragment() {
    private var binding: FragmentUser2Binding? = null
    private val db = FirebaseFirestore.getInstance()
    private lateinit var customAdapter: GuessLikeAdapter
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
        var img_cart = binding?.userImgCart



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

        val guestRoot = b.viewUserGuest.root
        val loginRoot = b.viewUserLogin.root

        if (user == null) {
            // ... (未登入邏輯保持不變) ...
            guestRoot.visibility = View.VISIBLE
            loginRoot.visibility = View.GONE
            b.MyStore.visibility = View.GONE
        } else {
            // === 已登入狀態 ===
            guestRoot.visibility = View.GONE
            loginRoot.visibility = View.VISIBLE
            b.MyStore.visibility = View.VISIBLE

            // 1. 綁定 UI 元件 (原本的)
            val tvUserName = b.viewUserLogin.tvUserName
            val tvUserEmail = b.viewUserLogin.tvUserEmail
            val imgMyaccountLogin = b.viewUserLogin.imgMyaccountLogin
            val btnLogout = b.viewUserLogin.btnLogout

            // ★ 新增：綁定新的編輯圖示
            // 注意：因為這是 include 進來的，如果找不到 ID，可能需要檢查 view_user_login.xml 是否已存檔
            val imgEditAvatar = b.viewUserLogin.imgEditAvatar
            val imgEditName = b.viewUserLogin.imgEditName

            // 2. 顯示資料
            tvUserName.text = user.displayName ?: "皮蝦用戶"
            tvUserEmail.text = user.email ?: ""

            if (user.photoUrl != null) {
                Glide.with(this).load(user.photoUrl).circleCrop().into(imgMyaccountLogin)
            } else {
                imgMyaccountLogin.setImageResource(R.drawable.user)
            }

            // 3. 設定點擊事件 -> 跳轉到 AccountSettingActivity
            val editProfileIntent = Intent(requireContext(), EditProfileActivity::class.java)

            // 讓這四個元件點了都能去設定頁
            imgMyaccountLogin.setOnClickListener { startActivity(editProfileIntent) }
            tvUserName.setOnClickListener { startActivity(editProfileIntent) }

            // ★ 新增：新圖示的點擊事件
            imgEditAvatar.setOnClickListener { startActivity(editProfileIntent) }
            imgEditName.setOnClickListener { startActivity(editProfileIntent) }

            // 登出
            btnLogout.setOnClickListener {
                FirebaseAuth.getInstance().signOut()
                updateUserUi()
            }
            // ★★★ 新增：呼叫同步 Google 頭像的函式 ★★★
            checkAndSyncGooglePhoto(user)
        }
    }

    // ★★★ 新增這個函式：自動將 Google 頭像同步到 Firestore ★★★
    private fun checkAndSyncGooglePhoto(user: com.google.firebase.auth.FirebaseUser) {
        val googlePhotoUrl = user.photoUrl?.toString()

        // 1. 如果 Google 帳號本身沒頭像，就不用同步了
        if (googlePhotoUrl.isNullOrEmpty()) return

        val userRef = db.collection("users").document(user.uid)

        // 2. 讀取 Firestore 目前的資料
        userRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val dbPhoto = document.getString("photoUrl")
                val dbImage = document.getString("imageUrl")

                // 3. 如果資料庫裡完全沒照片 (null 或空字串)
                if (dbPhoto.isNullOrEmpty() && dbImage.isNullOrEmpty()) {
                    // 4. 自動把 Google 的照片寫進去
                    val updates = hashMapOf<String, Any>(
                        "photoUrl" to googlePhotoUrl,
                        "imageUrl" to googlePhotoUrl,
                        "name" to (user.displayName ?: "皮蝦用戶") // 順便同步名字
                    )

                    userRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            android.util.Log.d("SyncPhoto", "Google 頭像已同步到 Firestore")
                        }
                }
            } else {
                // 5. 如果連文件都沒有 (例如剛註冊)，直接建立
                val newUser = hashMapOf<String, Any>(
                    "photoUrl" to googlePhotoUrl,
                    "imageUrl" to googlePhotoUrl,
                    "name" to (user.displayName ?: "皮蝦用戶"),
                    "email" to (user.email ?: "")
                )
                userRef.set(newUser, com.google.firebase.firestore.SetOptions.merge())
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



        // 從 Firebase 載入真實商品
        loadAllProducts()
        customAdapter = GuessLikeAdapter(emptyList())
        b.sectionGuesslike.myRecycleLike.layoutManager = GridLayoutManager(requireContext(), 2)
        b.sectionGuesslike.myRecycleLike.isNestedScrollingEnabled = false
        b.sectionGuesslike.myRecycleLike.adapter = customAdapter
        loadAllProducts()
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

        b.userImgCart.setOnClickListener {
            val intent = Intent(context, ShoppingCartActivity::class.java)
            startActivity(intent)
        }
        b.MyStore.setOnClickListener {
            val intent = Intent(context, MyStoreActivity::class.java)
            startActivity(intent)
        }
    }
    private fun loadAllProducts() {
        db.collection("products")
            .whereEqualTo("status", "ON_SHELF") // ★ 加入這行，確保不推薦下架商品
            .limit(10) // 通常猜你喜歡會限制數量
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                // 4. 明確指定 result: QuerySnapshot 解決推斷錯誤
                if (!result.isEmpty) {
                    // 5. 確保 Product 已 import，這樣 ::class.java 就不會報錯
                    val productList = result.toObjects(Product::class.java)
                    customAdapter.updateData(productList)
                }
            }
            .addOnFailureListener { e: Exception ->
                // 6. 明確指定 e: Exception
                if (isAdded) {
                    Toast.makeText(requireContext(), "載入失敗: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
