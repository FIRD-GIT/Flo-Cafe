package com.firdgit.flocafe

class MenuRepository {
    fun availableItems(): List<MenuItem> = listOf(
        MenuItem(
            id = "latte",
            name = "Café latte",
            description = "Espresso with steamed milk",
            price = 3.20,
            category = "Coffee",
            customisationOptions = listOf("Standard", "Less sugar", "Extra shot"),
            imageResId = R.drawable.product_latte,
        ),
        MenuItem(
            id = "americano",
            name = "Americano",
            description = "Espresso with hot water",
            price = 2.60,
            category = "Coffee",
            customisationOptions = listOf("Standard", "Less sugar", "Extra shot"),
            imageResId = R.drawable.product_americano,
        ),
        MenuItem(
            id = "banana-bread",
            name = "Banana bread",
            description = "Toasted slice with butter",
            price = 2.40,
            category = "Bakery",
            customisationOptions = listOf("Standard", "Toasted", "No butter"),
            imageResId = R.drawable.product_banana_bread,
        ),
    )
}
