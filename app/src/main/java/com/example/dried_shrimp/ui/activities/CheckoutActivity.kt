package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.CartItem
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.databinding.ActivityCheckoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 從上一頁傳來的資料
    private var cartItems = arrayListOf<CartItem>()
    private var totalPrice = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 1. 接收資料
        // 注意：因為 CartItem 必須是 Serializable，我們才能這樣傳
        cartItems = intent.getSerializableExtra("CART_ITEMS") as? ArrayList<CartItem> ?: arrayListOf()
        totalPrice = intent.getIntExtra("TOTAL_PRICE", 0)
        setlistener()
        setupUI()


    }
    fun setlistener(){
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.btnSubmitOrder.setOnClickListener {
            submitOrder()
        }
    }
    private fun setupUI() {
        binding.tvItemCount.text = "共 ${cartItems.size} 項商品"
        binding.tvFinalPrice.text = "$$totalPrice"
    }

    private fun submitOrder() {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch() // 使用 Batch 確保全部成功或全部失敗
        val currentUser = FirebaseAuth.getInstance().currentUser

        // 取得前一頁傳來的商品列表 (只包含已勾選的)
        val checkoutItems = intent.getSerializableExtra("CART_ITEMS") as? ArrayList<CartItem> ?: return

        if (checkoutItems.isEmpty()) return

        // 🔥 關鍵邏輯：迴圈針對「每一個商品」建立一張獨立訂單
        for (item in checkoutItems) {

            // 1. 產生新的 Order ID
            val newOrderId = db.collection("orders").document().id

            // 2. 計算單項總價
            val itemTotal = item.price * item.quantity

            // 3. 建立 Order 物件 (只包含這一個商品)
            val newOrder = Order(
                orderId = newOrderId,
                buyerId = currentUser?.uid ?: "",
                sellerId = item.sellerId, // 確保這張單只屬於該商品的賣家
                items = listOf(item),     // 清單內只有這一個 CartItem
                totalPrice = itemTotal,
                status = "PENDING",       // 初始狀態
                timestamp = System.currentTimeMillis(),
                hasReviewed = false
            )

            // 4. 寫入三個路徑 (全域、買家、賣家)
            val globalRef = db.collection("orders").document(newOrderId)
            batch.set(globalRef, newOrder)

            val buyerRef = db.collection("users").document(newOrder.buyerId)
                .collection("orders").document(newOrderId)
            batch.set(buyerRef, newOrder)

            if (newOrder.sellerId.isNotEmpty()) {
                val sellerRef = db.collection("users").document(newOrder.sellerId)
                    .collection("orders").document(newOrderId)
                batch.set(sellerRef, newOrder)
            }
        }

        // 5. 提交並清空購物車
        batch.commit()
            .addOnSuccessListener {
                // 清除購物車中「已購買」的商品 (透過 Batch 再跑一次刪除)
                clearPurchasedItemsFromCart(checkoutItems)

                Toast.makeText(this, "訂單建立成功！請前往付款", Toast.LENGTH_SHORT).show()
                // 這裡看您流程，通常是跳回訂單列表，或是跳到付款頁
                // 如果是跳到付款頁，因為有多張訂單，通常會先跳回列表讓使用者逐一付款
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "下單失敗: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun clearPurchasedItemsFromCart(purchasedItems: List<com.example.dried_shrimp.data.model.CartItem>) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return
        val batch = db.batch() // 使用批次處理一次刪除多筆

        for (item in purchasedItems) {
            // 鎖定購物車路徑：users -> {uid} -> cart -> {productId}
            val cartItemRef = db.collection("users").document(userId)
                .collection("cart").document(item.productId)

            // 加入刪除排程
            batch.delete(cartItemRef)
        }

        // 提交刪除
        batch.commit()
            .addOnSuccessListener {
                android.util.Log.d("Checkout", "購物車已清理完畢")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Checkout", "清理購物車失敗", e)
            }
    }
}