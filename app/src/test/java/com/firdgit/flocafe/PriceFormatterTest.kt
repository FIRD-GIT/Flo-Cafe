package com.firdgit.flocafe

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceFormatterTest {
    @Test
    fun wholePoundAmountsDoNotShowUnnecessaryDecimals() {
        assertEquals("£3", PriceFormatter.format(3.0))
    }

    @Test
    fun fractionalAmountsAlwaysShowTwoDecimalPlaces() {
        assertEquals("£3.20", PriceFormatter.format(3.2))
        assertEquals("£2.60", PriceFormatter.format(2.6))
    }
}
