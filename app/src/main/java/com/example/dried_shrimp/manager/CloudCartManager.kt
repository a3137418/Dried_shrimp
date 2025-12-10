package com.example.dried_shrimp.manager

import android.util.Log
import com.example.dried_shrimp.data.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object CloudCartManager {

    // 取得資料庫實體
    private val db = FirebaseFirestore.getInstance()
    // 取得目前的 User ID
    private val currentUserUid: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    // --- 功能 1：加入購物車 ---
    fun addToCart(item: CartItem, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val uid = currentUserUid
        if (uid == null) {
            onFailure("尚未登入，無法加入購物車")
            return
        }

        // 寫入路徑：users -> {uid} -> cart -> {自動產生ID}
        db.collection("users").document(uid).collection("cart")
            .add(item)
            .addOnSuccessListener {
                Log.d("CloudCart", "加入成功")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("CloudCart", "加入失敗", e)
                onFailure(e.localizedMessage ?: "未知錯誤")
            }
    }

    // --- 功能 2：讀取購物車 (即時監聽) ---
    // 這裡使用 callback 把抓到的資料傳回去給 Activity
    fun getCartItems(onDataReceived: (List<CartItem>) -> Unit) {
        val uid = currentUserUid ?: return // 如果沒登入就直接結束

        db.collection("users").document(uid).collection("cart")
            .get() // 如果想要即時更新，可以改用 .addSnapshotListener
            .addOnSuccessListener { result ->
                // 把 Firebase 的文件轉成我們的 CartItem 物件列表
                val list = result.toObjects(CartItem::class.java)
                onDataReceived(list)
            }
            .addOnFailureListener { e ->
                Log.e("CloudCart", "讀取失敗", e)
            }
    }
}