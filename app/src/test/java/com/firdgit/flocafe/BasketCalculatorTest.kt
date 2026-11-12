package com.firdgit.flocafe

import org.junit.Assert.assertEquals
import org.junit.Test

class BasketCalculatorTest {
    @Test
    fun totalUsesPriceTimesQuantityAcrossLines() {
        val latte = MenuItem("latte", "Latte", "Coffee", 3.20)
        val cookie = MenuItem("cookie", "Cookie", "Treat", 1.80)

        assertEquals(
            8.20,
            BasketCalculator.total(
                listOf(
                    BasketLine(latte, 2),
                    BasketLine(cookie, 1),
                ),
            ),
            0.001,
        )
    }

    @Test
    fun emptyBasketTotalIsZero() {
        assertEquals(0.0, BasketCalculator.total(emptyList()), 0.001)
    }
}
