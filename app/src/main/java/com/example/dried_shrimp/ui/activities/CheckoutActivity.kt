package com.example.dried_shrimp.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager // 🔥 新增：解決 LayoutManager 問題
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.CartItem
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.databinding.ActivityCheckoutBinding
import com.example.dried_shrimp.ui.adapters.CheckoutAdapter // 🔥 新增：解決 Unresolved reference 'CheckoutAdapter'
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val db = FirebaseFirestore.getInstance()
    private val checkoutItems = ArrayList<CartItem>()
    private lateinit var adapter: CheckoutAdapter

    // 用來判斷是否為直接購買
    private var isDirectBuy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 1. 初始化列表 (解決 adapter 與 notifyDataSetChanged 錯誤)
        setupRecyclerView()

        // 2. 載入資料
        loadOrderData()

        // 3. 設定按鈕監聽
        setupListener()
    }

    private fun setupRecyclerView() {
        adapter = CheckoutAdapter(checkoutItems)
        binding.rvCheckoutItems.layoutManager = LinearLayoutManager(this)
        binding.rvCheckoutItems.adapter = adapter
    }

    private fun loadOrderData() {
        isDirectBuy = intent.getBooleanExtra("is_direct_buy", false)

        if (isDirectBuy) {
            // --- 情況 A：來自「直接購買」 ---
            val productId = intent.getStringExtra("product_id") ?: ""
            val name = intent.getStringExtra("product_name") ?: "商品"
            val price = intent.getIntExtra("product_price", 0)
            val imageUrl = intent.getStringExtra("product_image") ?: ""
            val sellerId = intent.getStringExtra("seller_id") ?: ""
            val quantity = intent.getIntExtra("quantity", 1)

            val directItem = CartItem(
                productId = productId,
                name = name,
                price = price,
                imageUrl = imageUrl,
                sellerId = sellerId,
                quantity = quantity,
                isChecked = true
            )

            checkoutItems.clear()
            checkoutItems.add(directItem)

        } else {
            // --- 情況 B：來自「購物車結帳」 ---
            val items = intent.getSerializableExtra("CART_ITEMS") as? ArrayList<CartItem>
            if (items != null) {
                checkoutItems.clear()
                checkoutItems.addAll(items)
            }
        }

        // 更新介面 (現在 adapter 已初始化，這裡不會報錯了)
        adapter.notifyDataSetChanged()
        updateTotalAmount()
    }

    private fun updateTotalAmount() {
        var itemsTotal = 0
        // 1. 計算商品總金額
        for (item in checkoutItems) {
            itemsTotal += (item.price * item.quantity)
        }

        // 2. 計算運費
        // 因為我們是「拆單模式」(每個商品變成一張獨立訂單)
        // 所以運費應該是：商品數量 * 60
        // (如果您希望不管買幾個都只收 60，那這裡就維持 val shippingFee = 60，但 submitSplitOrders 那邊也要改成不加運費)
        val shippingFee = 60 * checkoutItems.size

        val finalTotal = itemsTotal + shippingFee

        // 3. 更新介面顯示
        binding.tvSubtotal.text = "$$itemsTotal"
        binding.tvShippingFee.text = "$$shippingFee" // 這裡會顯示總運費 (例如 $120)
        binding.tvTotalAmount.text = "$$finalTotal"
    }

    private fun setupListener() {
        binding.imgBack.setOnClickListener {
            finish()
        }
        binding.btnSubmitOrder.setOnClickListener {
            submitSplitOrders()
        }
    }

    // 提交訂單邏輯 (已整合，刪除了舊的 submitOrder 以免重複)
    private fun submitSplitOrders() {
        val batch = db.batch()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (checkoutItems.isEmpty()) return

        // 迴圈針對「每一個商品」建立獨立訂單
        for (item in checkoutItems) {
            val newOrderId = db.collection("orders").document().id
            val itemTotal = item.price * item.quantity + 60 // 簡易加上運費

            val newOrder = Order(
                orderId = newOrderId,
                buyerId = currentUser?.uid ?: "",
                sellerId = item.sellerId,
                items = listOf(item),
                totalPrice = itemTotal,
                status = "PENDING",
                timestamp = System.currentTimeMillis(),
                hasReviewed = false
            )

            // 寫入三個路徑
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

        batch.commit()
            .addOnSuccessListener {
                // 如果不是直接購買，才需要清空購物車
                if (!isDirectBuy) {
                    clearPurchasedItemsFromCart(checkoutItems)
                }

                Toast.makeText(this, "訂單建立成功！", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "下單失敗: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearPurchasedItemsFromCart(purchasedItems: List<CartItem>) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val batch = db.batch()

        for (item in purchasedItems) {
            val cartItemRef = db.collection("users").document(userId)
                .collection("cart").document(item.productId)
            batch.delete(cartItemRef)
        }

        batch.commit()
            .addOnSuccessListener {
                android.util.Log.d("Checkout", "購物車已清理完畢")
            }
    }
}