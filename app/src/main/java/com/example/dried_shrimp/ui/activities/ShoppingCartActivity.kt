package com.example.dried_shrimp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dried_shrimp.R
import com.example.dried_shrimp.data.model.CartItem
import com.example.dried_shrimp.databinding.ActivityShoppingCartBinding
import com.example.dried_shrimp.ui.adapters.CartAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ShoppingCartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShoppingCartBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: CartAdapter
    private val cartList = mutableListOf<CartItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShoppingCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setlistener()
        setupRecyclerView()
        loadCartItems()
    }

    override fun onResume() {
        super.onResume()
        loadCartItems()
    }

    fun setlistener(){
        binding.imgBack.setOnClickListener { finish() }

        binding.btnCheckout.setOnClickListener {
            // ★ 修改 1：篩選出「已勾選」的商品
            val selectedItems = cartList.filter { it.isChecked }

            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "您尚未勾選任何商品", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, CheckoutActivity::class.java)

                // ★ 修改 2：只傳送已勾選的商品 (ArrayList)
                intent.putExtra("CART_ITEMS", ArrayList(selectedItems))

                // ★ 修改 3：計算已勾選商品的總金額
                var total = 0
                for (item in selectedItems) {
                    total += (item.price * item.quantity)
                }
                intent.putExtra("TOTAL_PRICE", total)

                startActivity(intent)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(cartList,
            onMinusClick = { item ->
                if (item.quantity > 1) {
                    updateQuantity(item, item.quantity - 1)
                } else {
                    showDeleteDialog(item)
                }
            },
            onPlusClick = { item ->
                updateQuantity(item, item.quantity + 1)
            },
            onDeleteClick = { item ->
                showDeleteDialog(item)
            },
            // ★ 如果您的 Adapter 有支援勾選回調，請加在這裡，例如：
            /*
            onCheckChange = { item, isChecked ->
                item.isChecked = isChecked
                calculateTotalPrice() // 勾選改變時，重新計算總金額
                // 建議同步更新到 Firestore
                updateCheckStatus(item, isChecked)
            }
            */
        )
        // 暫時解法：如果 Adapter 沒有 checkbox callback，我們假設 Adapter 內部會改變 item.isChecked
        // 但為了即時更新總金額，Adapter 最好能通知 Activity

        binding.rvCart.layoutManager = LinearLayoutManager(this)
        binding.rvCart.adapter = adapter
    }

    private fun loadCartItems() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("cart")
            .get()
            .addOnSuccessListener { result ->
                cartList.clear()
                val list = result.toObjects(CartItem::class.java)
                cartList.addAll(list)

                adapter.notifyDataSetChanged()
                calculateTotalPrice()
            }
            .addOnFailureListener {
                Toast.makeText(this, "載入失敗: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateQuantity(item: CartItem, newQty: Int) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("cart").document(item.productId)
            .update("quantity", newQty)
            .addOnSuccessListener {
                item.quantity = newQty
                adapter.notifyDataSetChanged()
                calculateTotalPrice()
            }
    }

    // 選用：更新勾選狀態到資料庫 (如果您的 CartAdapter 有實作勾選監聽)
    private fun updateCheckStatus(item: CartItem, isChecked: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("cart").document(item.productId)
            .update("isChecked", isChecked)
    }

    private fun deleteCartItem(item: CartItem) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId)
            .collection("cart").document(item.productId)
            .delete()
            .addOnSuccessListener {
                cartList.remove(item)
                adapter.notifyDataSetChanged()
                calculateTotalPrice()
                Toast.makeText(this, "已刪除", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateTotalPrice() {
        var total = 0
        for (item in cartList) {
            // ★ 修改 4：只計算已勾選的項目
            if (item.isChecked) {
                total += (item.price * item.quantity)
            }
        }
        binding.tvTotalPrice.text = "$$total"
    }

    private fun showDeleteDialog(item: CartItem) {
        AlertDialog.Builder(this)
            .setTitle("移除商品")
            .setMessage("確定要將「${item.name}」移出購物車嗎？")
            .setPositiveButton("移除") { _, _ ->
                deleteCartItem(item)
            }
            .setNegativeButton("取消", null)
            .show()
    }
}