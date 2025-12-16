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
        val name = binding.etReceiverName.text.toString().trim()
        val phone = binding.etReceiverPhone.text.toString().trim()
        val address = binding.etReceiverAddress.text.toString().trim()
        val buyerId = auth.currentUser?.uid ?: return

        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "請填寫完整收件資訊", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSubmitOrder.isEnabled = false
        binding.btnSubmitOrder.text = "處理中..."

        val batch = db.batch()

        // ★★★ 關鍵邏輯：依照賣家 ID 將商品分組 (拆單) ★★★
        val groupedItems = cartItems.groupBy { it.sellerId }

        // 針對每一組 (每一個賣家)，建立一張獨立的訂單
        for ((sellerId, items) in groupedItems) {

            // 1. 產生訂單 ID
            val orderRef = db.collection("orders").document()
            val orderId = orderRef.id

            // 2. 計算該賣家這張單的總金額
            val subTotal = items.sumOf { it.price * it.quantity }

            val newOrder = Order(
                orderId = orderId,
                buyerId = buyerId,
                sellerId = sellerId, // 寫入賣家 ID
                items = items,
                totalPrice = subTotal, // 這裡存的是該張分單的金額
                receiverName = name,
                receiverPhone = phone,
                receiverAddress = address,
                status = "PENDING",
                timestamp = System.currentTimeMillis()
            )

            // 3. 寫入路徑 A：全域 orders (方便管理員查看，或產生唯一 ID)
            batch.set(orderRef, newOrder)

            // 4. 寫入路徑 B：買家的訂單 (users -> buyer -> orders -> orderId)
            val buyerOrderRef = db.collection("users").document(buyerId)
                .collection("orders").document(orderId)
            batch.set(buyerOrderRef, newOrder)

            // 5. 寫入路徑 C：賣家的訂單 (users -> seller -> orders -> orderId)
            // 這樣賣家只需要讀取自己下面的 orders 集合，就看不到別人的訂單了
            val sellerOrderRef = db.collection("users").document(sellerId)
                .collection("orders").document(orderId)
            batch.set(sellerOrderRef, newOrder)
        }

        // 6. 清空購物車 (針對所有商品)
        for (item in cartItems) {
            val cartRef = db.collection("users").document(buyerId)
                .collection("cart").document(item.productId)
            batch.delete(cartRef)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(this, "下單成功！已依賣家拆分訂單", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnSubmitOrder.isEnabled = true
                binding.btnSubmitOrder.text = "確認下單"
                Toast.makeText(this, "下單失敗: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}