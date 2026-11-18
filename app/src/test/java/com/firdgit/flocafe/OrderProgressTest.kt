package com.firdgit.flocafe

import org.junit.Assert.assertEquals
import org.junit.Test

class OrderProgressTest {
    @Test
    fun mapsCustomerStagesToVisualPercentages() {
        assertEquals(25, OrderProgress.percent("Pending"))
        assertEquals(50, OrderProgress.percent("Preparing"))
        assertEquals(75, OrderProgress.percent("Ready for Collection"))
        assertEquals(100, OrderProgress.percent("Collected"))
    }

    @Test
    fun unknownStatusUsesZeroProgress() {
        assertEquals(0, OrderProgress.percent(""))
        assertEquals(0, OrderProgress.percent("Unknown"))
    }
}
