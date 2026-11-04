package com.firdgit.flocafe

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    context: Context,
) {
    private val appContext = context.applicationContext
    val isFirebaseConfigured: Boolean
        get() = FirebaseApp.getApps(appContext).isNotEmpty()

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun currentUserRole(): UserRole? {
        if (!isFirebaseConfigured) return null
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = firestore.collection("users").document(uid).get().await()
        val profile = snapshot.toObject(UserProfile::class.java) ?: return UserRole.CUSTOMER
        return if (profile.isAdmin) UserRole.ADMIN else UserRole.CUSTOMER
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        require(profile.uid.isNotBlank()) { "A user profile requires a Firebase UID." }
        check(isFirebaseConfigured) { "Firebase is not configured for this build." }
        firestore.collection("users").document(profile.uid).set(profile).await()
    }
}
