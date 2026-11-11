package com.firdgit.flocafe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuRepositoryTest {
    private val items = MenuRepository().availableItems()

    @Test
    fun menuContainsTheFourCustomerCategories() {
        assertEquals(
            setOf("Hot Drinks", "Cold Drinks", "Sweet Treats", "Sandwiches"),
            items.map { it.category }.toSet(),
        )
    }

    @Test
    fun drinkCustomisationAndNutritionMetadataArePresent() {
        val latte = items.first { it.id == "latte" }

        assertTrue(latte.sizeOptions.contains("Large"))
        assertTrue(latte.milkOptions.contains("Oat"))
        assertTrue(latte.shotOptions.contains("Extra shot"))
        assertTrue(latte.sweetnessOptions.contains("Less sweet"))
        assertTrue(latte.nutritionNote.contains("not verified"))
        assertTrue(latte.calories > 0)
    }
}
