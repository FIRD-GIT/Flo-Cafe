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
    val calories: Int = 0,
    val nutritionNote: String = "AI-generated estimate for prototype use — not verified",
    val sizeOptions: List<String> = listOf("Regular", "Large"),
    val milkOptions: List<String> = listOf("Semi-skimmed", "Oat", "Soya", "Almond"),
    val shotOptions: List<String> = listOf("Standard", "Extra shot"),
    val temperatureOptions: List<String> = listOf("Hot", "Iced"),
    val sweetnessOptions: List<String> = listOf("Regular sweetness", "Less sweet", "Unsweetened"),
    val syrupOptions: List<String> = listOf("No syrup", "Vanilla", "Caramel"),
    val toppingOptions: List<String> = listOf("No topping", "Whipped cream"),
)

data class BasketLine(
    val item: MenuItem,
    var quantity: Int,
    var customisation: String = "Standard",
)
