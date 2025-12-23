package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.databinding.ActivityPaymentBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private val db = FirebaseFirestore.getInstance()
    // 用來暫存從上一頁傳來的訂單資料
    private var currentOrder: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 1. 接收訂單資料 (從 Intent 取得)
        currentOrder = intent.getSerializableExtra("ORDER_DATA") as? Order

        // 防呆：如果沒拿到資料就關閉頁面
        if (currentOrder == null) {
            Toast.makeText(this, "訂單資料錯誤", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        // 顯示金額
        binding.tvPaymentAmount.text = "$${currentOrder!!.totalPrice}"

        // 返回按鈕
        binding.imgBack.setOnClickListener { finish() }

        // 確認付款按鈕
        binding.btnConfirmPay.setOnClickListener {
            processPayment()
        }
    }

    // ★★★ 核心邏輯：處理付款與扣庫存 (已修正為同步更新三方路徑) ★★★
    private fun processPayment() {
        val order = currentOrder ?: return

        // 1. 鎖定按鈕，避免重複點擊
        binding.btnConfirmPay.isEnabled = false
        binding.btnConfirmPay.text = "交易處理中..."

        // 2. 執行 Firestore Transaction (交易)
        db.runTransaction { transaction ->

            // (A) --- 庫存檢查與扣除 (維持不變) ---
            val productRefs = order.items.map { item ->
                val ref = db.collection("products").document(item.productId)
                val snapshot = transaction.get(ref) // 讀取最新資料

                // 檢查該商品是否還存在
                if (!snapshot.exists()) {
                    throw FirebaseFirestoreException("商品已下架: ${item.name}", FirebaseFirestoreException.Code.ABORTED)
                }

                // 檢查庫存是否足夠
                val currentStock = snapshot.getLong("stock")?.toInt() ?: 0
                if (currentStock < item.quantity) {
                    throw FirebaseFirestoreException("庫存不足: ${item.name}", FirebaseFirestoreException.Code.ABORTED)
                }

                // 準備好要更新的資料
                Triple(ref, currentStock - item.quantity, item.name)
            }

            // (B) --- 執行寫入：扣除庫存 ---
            for ((ref, newStock, _) in productRefs) {
                transaction.update(ref, "stock", newStock)
            }

            // (C) --- 執行寫入：更新訂單狀態 (狀態改為 TO_SHIP) ---
            // ★★★ 修正這裡：同時更新 全域、買家、賣家 三個路徑 ★★★

            // 1. 全域訂單
            val globalRef = db.collection("orders").document(order.orderId)
            transaction.update(globalRef, "status", "TO_SHIP")

            // 2. 買家訂單 (users -> buyer -> orders)
            val buyerRef = db.collection("users").document(order.buyerId)
                .collection("orders").document(order.orderId)
            transaction.update(buyerRef, "status", "TO_SHIP")

            // 3. 賣家訂單 (users -> seller -> orders)
            val sellerRef = db.collection("users").document(order.sellerId)
                .collection("orders").document(order.orderId)
            transaction.update(sellerRef, "status", "TO_SHIP")

            // Transaction 結束
        }.addOnSuccessListener {
            // --- 交易成功 ---
            Toast.makeText(this, "付款成功！商品將盡快出貨", Toast.LENGTH_LONG).show()
            finish()

        }.addOnFailureListener { e ->
            // --- 交易失敗 ---
            binding.btnConfirmPay.isEnabled = true
            binding.btnConfirmPay.text = "確認付款"

            val errorMsg = if (e is FirebaseFirestoreException) e.message else "付款失敗，請稍後再試"
            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
        }
    }
}