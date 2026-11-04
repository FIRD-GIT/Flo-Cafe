package com.firdgit.flocafe

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val isAdmin: Boolean = false,
)

enum class UserRole {
    ADMIN,
    CUSTOMER,
}
