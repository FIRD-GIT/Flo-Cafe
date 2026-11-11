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

    @Test
    fun fixedSweetTreatsDoNotRequireCustomisation() {
        assertTrue(items.first { it.id == "cookie" }.isCustomisable.not())
        assertTrue(items.first { it.id == "brownie" }.isCustomisable.not())
        assertTrue(items.first { it.id == "chicken-sandwich" }.isCustomisable)
        assertTrue(items.first { it.id == "chicken-sandwich" }.toppingOptions.contains("Extra cheese"))
    }

    @Test
    fun customisationGroupsMatchTheProductType() {
        val latte = items.first { it.id == "latte" }
        val sandwich = items.first { it.id == "chicken-sandwich" }
        val cookie = items.first { it.id == "cookie" }

        assertTrue(latte.customisationGroups.any { it.title == "Milk" })
        assertTrue(latte.customisationGroups.any { it.title == "Espresso shots" })
        assertEquals(
            setOf("Preparation", "Ingredients"),
            sandwich.customisationGroups.map { it.title }.toSet(),
        )
        assertTrue(cookie.customisationGroups.isEmpty())
    }
}
