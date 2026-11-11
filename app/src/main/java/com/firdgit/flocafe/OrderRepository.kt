package com.firdgit.flocafe

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val userRepository: UserRepository,
) {
    private val firestore = FirebaseFirestore.getInstance()

    data class OrderSummary(
        val id: String,
        val status: String,
        val total: Double,
        val fingerprint: String = "",
        val createdAtMillis: Long? = null,
        val items: List<OrderItemSummary> = emptyList(),
    )

    data class OrderItemSummary(
        val menuItemId: String,
        val name: String,
        val unitPrice: Double,
        val quantity: Int,
        val customisation: String,
    )

    suspend fun submitOrder(lines: List<BasketLine>): String {
        require(lines.isNotEmpty()) { "Add an item before checkout." }
        check(userRepository.isFirebaseConfigured) { "Firebase is not configured for this build." }
        val customerUid = requireNotNull(userRepository.currentUserId) {
            "Sign in before submitting an order."
        }
        val order = firestore.collection("orders").document()
        val items = lines.map { line ->
            mapOf(
                "menuItemId" to line.item.id,
                "nameSnapshot" to line.item.name,
                "unitPrice" to line.item.price,
                "quantity" to line.quantity,
                "customisation" to line.customisation,
            )
        }
        order.set(
            mapOf(
                "customerUid" to customerUid,
                "createdAt" to FieldValue.serverTimestamp(),
                "status" to "Pending",
                "total" to lines.sumOf { it.item.price * it.quantity },
                "items" to items,
                "fingerprint" to OrderFingerprint.from(lines),
            ),
        ).await()
        return order.id
    }

    suspend fun findRecentDuplicate(
        lines: List<BasketLine>,
        withinMinutes: Long = 10,
    ): OrderSummary? {
        val wantedFingerprint = OrderFingerprint.from(lines)
        val cutoff = System.currentTimeMillis() - (withinMinutes * 60_000)
        return customerOrders().firstOrNull {
            it.fingerprint == wantedFingerprint &&
                it.createdAtMillis != null &&
                it.createdAtMillis >= cutoff
        }
    }

    suspend fun currentCustomerOrders(): List<OrderSummary> {
        check(userRepository.isFirebaseConfigured) { "Firebase is not configured for this build." }
        return customerOrders()
    }

    suspend fun allOrders(): List<OrderSummary> {
        check(userRepository.isFirebaseConfigured) { "Firebase is not configured for this build." }
        return firestore.collection("orders")
            .get()
            .await()
            .documents
            .map(::toOrderSummary)
    }

    suspend fun updateStatus(orderId: String, status: String) {
        require(status in setOf("Pending", "Preparing", "Ready for Collection", "Collected")) {
            "Unsupported order status."
        }
        check(userRepository.isFirebaseConfigured) { "Firebase is not configured for this build." }
        firestore.collection("orders").document(orderId).update("status", status).await()
    }

    private suspend fun customerOrders(): List<OrderSummary> {
        val customerUid = requireNotNull(userRepository.currentUserId) {
            "Sign in before loading orders."
        }
        return firestore.collection("orders")
            .whereEqualTo("customerUid", customerUid)
            .get()
            .await()
            .documents
            .map(::toOrderSummary)
    }

    private fun toOrderSummary(document: com.google.firebase.firestore.DocumentSnapshot): OrderSummary =
        OrderSummary(
            id = document.id,
            status = document.getString("status").orEmpty(),
            total = document.getDouble("total") ?: 0.0,
            fingerprint = document.getString("fingerprint").orEmpty(),
            createdAtMillis = document.getTimestamp("createdAt")?.toDate()?.time,
            items = (document.get("items") as? List<*>)
                .orEmpty()
                .mapNotNull { item ->
                    val values = item as? Map<*, *> ?: return@mapNotNull null
                    OrderItemSummary(
                        menuItemId = values["menuItemId"]?.toString().orEmpty(),
                        name = values["nameSnapshot"]?.toString().orEmpty(),
                        unitPrice = (values["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                        quantity = (values["quantity"] as? Number)?.toInt() ?: 0,
                        customisation = values["customisation"]?.toString().orEmpty(),
                    )
                },
        )

}
