package com.firdgit.flocafe

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val available: Boolean = true,
)

data class BasketLine(
    val item: MenuItem,
    var quantity: Int,
)
