package com.firdgit.flocafe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class OrderFingerprintTest {
    private val latte = MenuItem(
        id = "latte",
        name = "Café latte",
        description = "Espresso with steamed milk",
        price = 3.20,
    )
    private val bread = MenuItem(
        id = "banana-bread",
        name = "Banana bread",
        description = "Toasted slice with butter",
        price = 2.40,
    )

    @Test
    fun sameItemsInDifferentOrderHaveTheSameFingerprint() {
        val first = listOf(BasketLine(latte, 1, "Less sugar"), BasketLine(bread, 2))
        val second = listOf(BasketLine(bread, 2), BasketLine(latte, 1, "Less sugar"))

        assertEquals(OrderFingerprint.from(first), OrderFingerprint.from(second))
    }

    @Test
    fun quantityOrCustomisationChangesTheFingerprint() {
        val standard = listOf(BasketLine(latte, 1, "Standard"))
        val changedQuantity = listOf(BasketLine(latte, 2, "Standard"))
        val changedCustomisation = listOf(BasketLine(latte, 1, "Extra shot"))

        assertNotEquals(OrderFingerprint.from(standard), OrderFingerprint.from(changedQuantity))
        assertNotEquals(OrderFingerprint.from(standard), OrderFingerprint.from(changedCustomisation))
    }
}
