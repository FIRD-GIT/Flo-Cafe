package com.firdgit.flocafe

class MenuRepository {
    fun availableItems(): List<MenuItem> = listOf(
        MenuItem("latte", "Café latte", "Espresso with steamed milk", 3.20),
        MenuItem("americano", "Americano", "Espresso with hot water", 2.60),
        MenuItem("banana-bread", "Banana bread", "Toasted slice with butter", 2.40),
    )
}
