package com.firdgit.flocafe

object DrinkRecommendation {
    data class Preferences(
        val milk: String,
        val flavour: String,
        val sweetness: String,
    )

    fun recommend(preferences: Preferences): String = when {
        preferences.flavour == "Vanilla" && preferences.sweetness == "Extra sweet" ->
            "Vanilla Latte"
        preferences.milk == "Oat" && preferences.flavour == "Caramel" ->
            "Oat Caramel Latte"
        preferences.flavour == "Chocolate" ->
            "Chocolate Mocha"
        preferences.flavour == "No flavour" && preferences.sweetness == "Unsweetened" ->
            "Black Americano"
        preferences.milk == "Oat" ->
            "Oat Milk Latte"
        else -> "Classic Cappuccino"
    }
}
