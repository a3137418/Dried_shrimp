import androidx.lifecycle.ViewModel
import com.example.dried_shrimp.data.model.Order
import com.example.dried_shrimp.data.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore

// OrderViewModel.kt
class OrderViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // 賣家功能：執行出貨
    fun shipOrder(order: Order, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // 1. 檢查狀態是否正確 (只有「待出貨」的訂單才能出貨)
        if (order.status != OrderStatus.PENDING_SHIPMENT.name) {
            onError("此訂單狀態無法執行出貨")
            return
        }

        val updates = hashMapOf<String, Any>(
            "status" to OrderStatus.SHIPPED.name, // 狀態變更為「待收貨」
            "shippedTime" to System.currentTimeMillis() // 紀錄出貨時間
        )

        db.collection("orders").document(order.orderId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "出貨失敗") }
    }

    // 買家功能：確認收貨
    fun confirmReceipt(order: Order, onSuccess: () -> Unit) {
        val updates = hashMapOf<String, Any>(
            "status" to OrderStatus.COMPLETED.name,
            "completedTime" to System.currentTimeMillis()
        )
        // 這裡通常還會加上「撥款給賣家」的邏輯
        db.collection("orders").document(order.orderId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
    }


}