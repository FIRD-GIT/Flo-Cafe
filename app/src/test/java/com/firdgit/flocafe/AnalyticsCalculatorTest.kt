package com.firdgit.flocafe

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyticsCalculatorTest {
    @Test
    fun calculatesFirestoreOrderAnalytics() {
        val latte = OrderRepository.OrderItemSummary("latte", "Café latte", 3.20, 2, "Standard")
        val cookie = OrderRepository.OrderItemSummary("cookie", "Chocolate chip cookie", 1.80, 1, "Standard")
        val orders = listOf(
            OrderRepository.OrderSummary("a", "Collected", 6.40, items = listOf(latte)),
            OrderRepository.OrderSummary("b", "Preparing", 1.80, items = listOf(cookie)),
        )
        val feedback = listOf(
            FeedbackRepository.Feedback("f1", "a", "customer", 5, "Good", null),
            FeedbackRepository.Feedback("f2", "b", "customer", 3, "Okay", null),
        )

        val summary = AnalyticsCalculator.summarise(orders, feedback)

        assertEquals(2, summary.orderCount)
        assertEquals(1, summary.collectedCount)
        assertEquals(8.20, summary.totalSales, 0.001)
        assertEquals(4.10, summary.averageOrderValue, 0.001)
        assertEquals(4.0, summary.averageRating, 0.001)
        assertEquals("Café latte", summary.topItemName)
    }

    @Test
    fun emptyAnalyticsUseSafeDefaults() {
        val summary = AnalyticsCalculator.summarise(emptyList(), emptyList())

        assertEquals(0, summary.orderCount)
        assertEquals(0.0, summary.totalSales, 0.001)
        assertEquals(0.0, summary.averageRating, 0.001)
        assertEquals("No items yet", summary.topItemName)
    }
}
