package com.firdgit.flocafe

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FeedbackRepository(
    private val userRepository: UserRepository,
) {
    private val firestore = FirebaseFirestore.getInstance()

    data class Feedback(
        val id: String,
        val orderId: String,
        val customerUid: String,
        val rating: Int,
        val comment: String,
        val createdAtMillis: Long?,
    )

    suspend fun submit(orderId: String, rating: Int, comment: String) {
        require(rating in 1..5) { "Rating must be between 1 and 5." }
        require(comment.trim().isNotBlank()) { "Please add a short comment." }
        val uid = requireNotNull(userRepository.currentUserId) { "Sign in before leaving feedback." }
        check(userRepository.isFirebaseConfigured) { "Firebase is not configured for this build." }
        firestore.collection("feedback").document().set(
            mapOf(
                "orderId" to orderId,
                "customerUid" to uid,
                "rating" to rating,
                "comment" to comment.trim(),
                "createdAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }

    suspend fun allForAdmin(): List<Feedback> {
        check(userRepository.isFirebaseConfigured) { "Firebase is not configured for this build." }
        return firestore.collection("feedback").get().await().documents.map { document ->
            Feedback(
                id = document.id,
                orderId = document.getString("orderId").orEmpty(),
                customerUid = document.getString("customerUid").orEmpty(),
                rating = (document.getLong("rating") ?: 0L).toInt(),
                comment = document.getString("comment").orEmpty(),
                createdAtMillis = document.getTimestamp("createdAt")?.toDate()?.time,
            )
        }
    }
}
