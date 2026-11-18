package com.firdgit.flocafe

object AnalyticsCalculator {
    data class Summary(
        val orderCount: Int,
        val collectedCount: Int,
        val totalSales: Double,
        val averageOrderValue: Double,
        val averageRating: Double,
        val topItemName: String,
    )

    fun summarise(
        orders: List<OrderRepository.OrderSummary>,
        feedback: List<FeedbackRepository.Feedback>,
    ): Summary {
        val itemCounts = orders.flatMap { it.items }
            .groupingBy { it.name }
            .eachCount()
        val topItem = itemCounts.maxWithOrNull(
            compareBy<Map.Entry<String, Int>> { it.value }.thenByDescending { it.key.lowercase() },
        )?.key.orEmpty()
        val sales = orders.sumOf { it.total }
        val averageRating = if (feedback.isEmpty()) {
            0.0
        } else {
            feedback.sumOf { it.rating }.toDouble() / feedback.size
        }
        return Summary(
            orderCount = orders.size,
            collectedCount = orders.count { it.status == "Collected" },
            totalSales = sales,
            averageOrderValue = if (orders.isEmpty()) 0.0 else sales / orders.size,
            averageRating = averageRating,
            topItemName = topItem.ifBlank { "No items yet" },
        )
    }
}
