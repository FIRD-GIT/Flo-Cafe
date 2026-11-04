package com.firdgit.flocafe

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderRepository(
    private val userRepository: UserRepository,
) {
    private val firestore = FirebaseFirestore.getInstance()

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
            )
        }
        order.set(
            mapOf(
                "customerUid" to customerUid,
                "createdAt" to FieldValue.serverTimestamp(),
                "status" to "Pending",
                "total" to lines.sumOf { it.item.price * it.quantity },
                "items" to items,
            ),
        ).await()
        return order.id
    }
}
