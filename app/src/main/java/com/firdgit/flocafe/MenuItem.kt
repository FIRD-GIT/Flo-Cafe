package com.firdgit.flocafe

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val available: Boolean = true,
    val category: String = "Other",
    val customisationOptions: List<String> = listOf("Standard"),
    val imageResId: Int = 0,
)

data class BasketLine(
    val item: MenuItem,
    var quantity: Int,
    var customisation: String = "Standard",
)
