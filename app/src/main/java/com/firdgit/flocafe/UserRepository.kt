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

    val currentUserEmail: String?
        get() = if (isFirebaseConfigured) auth.currentUser?.email else null

    val currentUserId: String?
        get() = if (isFirebaseConfigured) auth.currentUser?.uid else null

    suspend fun currentUserRole(): UserRole? {
        if (!isFirebaseConfigured) return null
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = firestore.collection("users").document(uid).get().await()
        return if (snapshot.getBoolean("isAdmin") == true) {
            UserRole.ADMIN
        } else {
            UserRole.CUSTOMER
        }
    }

    suspend fun currentUserDisplayName(): String? {
        if (!isFirebaseConfigured) return null
        val uid = auth.currentUser?.uid ?: return null
        val snapshot = firestore.collection("users").document(uid).get().await()
        return snapshot.toObject(UserProfile::class.java)
            ?.displayName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    suspend fun signIn(email: String, password: String): UserRole {
        require(email.isNotBlank()) { "Email is required." }
        require(password.isNotBlank()) { "Password is required." }
        check(isFirebaseConfigured) { "Firebase is not configured for this build." }
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        return currentUserRole() ?: UserRole.CUSTOMER
    }

    suspend fun registerCustomer(
        email: String,
        password: String,
        displayName: String,
    ): UserRole {
        require(email.isNotBlank()) { "Email is required." }
        require(password.length >= 6) { "Password must contain at least 6 characters." }
        check(isFirebaseConfigured) { "Firebase is not configured for this build." }
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val firebaseUser = requireNotNull(result.user) { "Firebase did not return the new user." }
        saveUserProfile(
            UserProfile(
                uid = firebaseUser.uid,
                email = firebaseUser.email.orEmpty(),
                displayName = displayName.trim(),
                isAdmin = false,
            ),
        )
        return UserRole.CUSTOMER
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        require(profile.uid.isNotBlank()) { "A user profile requires a Firebase UID." }
        check(isFirebaseConfigured) { "Firebase is not configured for this build." }
        firestore.collection("users").document(profile.uid).set(profile).await()
    }

    fun signOut() {
        if (isFirebaseConfigured) auth.signOut()
    }
}
