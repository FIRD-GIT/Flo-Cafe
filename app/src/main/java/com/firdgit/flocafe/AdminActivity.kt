package com.firdgit.flocafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var ordersText: TextView
    private lateinit var orderActions: LinearLayout
    private lateinit var reviewsText: TextView
    private lateinit var userRepository: UserRepository
    private lateinit var orderRepository: OrderRepository
    private var allOrders = emptyList<OrderRepository.OrderSummary>()
    private var allFeedback = emptyList<FeedbackRepository.Feedback>()
    private var orderSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        statusText = findViewById(R.id.adminStatusText)
        ordersText = findViewById(R.id.adminOrdersText)
        orderActions = findViewById(R.id.orderActions)
        reviewsText = findViewById(R.id.adminReviewsText)
        userRepository = UserRepository(this)
        orderRepository = OrderRepository(userRepository)

        findViewById<Button>(R.id.logoutButton).setOnClickListener { signOut() }
        val searchInput = findViewById<android.widget.EditText>(R.id.adminOrderSearch)
        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                orderSearchQuery = s?.toString().orEmpty()
                renderAdminOrders(allOrders)
            }
            override fun afterTextChanged(s: android.text.Editable?) = Unit
        })
        showAdminOrders()
        showReviews()
    }

    private fun showReviews() {
        lifecycleScope.launch {
            runCatching { FeedbackRepository(userRepository).allForAdmin() }
                .onSuccess { reviews ->
                    allFeedback = reviews
                    renderAnalytics(allOrders)
                    reviewsText.text = if (reviews.isEmpty()) {
                        getString(R.string.no_reviews)
                    } else {
                        getString(
                            R.string.admin_reviews,
                            reviews.joinToString("\n\n") {
                                getString(
                                    R.string.review_summary,
                                    it.rating,
                                    it.orderId.takeLast(6).uppercase(),
                                    it.comment,
                                )
                            },
                        )
                    }
                }
                .onFailure { error ->
                    reviewsText.text = error.message ?: getString(R.string.reviews_failed)
                }
        }
    }

    private fun showAdminOrders() {
        orderActions.removeAllViews()
        lifecycleScope.launch {
            runCatching { orderRepository.allOrders() }
                .onSuccess { orders ->
                    allOrders = orders
                    renderAdminOrders(orders)
                    renderAnalytics(orders)
                }
                .onFailure { error ->
                    ordersText.text = error.message ?: getString(R.string.orders_failed)
                }
        }
    }

    private fun renderAdminOrders(orders: List<OrderRepository.OrderSummary>) {
        orderActions.removeAllViews()
        val query = orderSearchQuery.trim().lowercase()
        val filtered = orders.filter {
            query.isEmpty() ||
                it.id.lowercase().contains(query) ||
                it.status.lowercase().contains(query)
        }
        ordersText.text = if (filtered.isEmpty()) {
            getString(R.string.no_matching_orders)
        } else {
            getString(
                R.string.admin_orders,
                filtered.joinToString("\n") {
                    getString(
                        R.string.order_summary,
                        it.id,
                        it.status,
                        it.total,
                        orderProgress(it.status),
                    )
                },
            )
        }
        filtered.forEach { addStatusButton(it) }
    }

    private fun renderAnalytics(orders: List<OrderRepository.OrderSummary>) {
        val summary = AnalyticsCalculator.summarise(orders, allFeedback)
        findViewById<TextView>(R.id.adminAnalyticsText).text = getString(
            R.string.admin_analytics,
            summary.orderCount,
            PriceFormatter.format(summary.totalSales),
            summary.collectedCount,
            PriceFormatter.format(summary.averageOrderValue),
            if (summary.averageRating == 0.0) "N/A" else String.format("%.1f", summary.averageRating),
            summary.topItemName,
        )
    }

    private fun addStatusButton(order: OrderRepository.OrderSummary) {
        orderActions.addView(
            Button(this).apply {
                text = getString(R.string.advance_order, order.id)
                setOnClickListener {
                    val nextStatus = when (order.status) {
                        "Pending" -> "Preparing"
                        "Preparing" -> "Ready for Collection"
                        "Ready for Collection" -> "Collected"
                        else -> "Collected"
                    }
                    lifecycleScope.launch {
                        runCatching { orderRepository.updateStatus(order.id, nextStatus) }
                            .onSuccess {
                                statusText.text = getString(R.string.status_updated, nextStatus)
                                showAdminOrders()
                            }
                            .onFailure { error ->
                                statusText.text =
                                    error.message ?: getString(R.string.status_update_failed)
                            }
                    }
                }
            },
        )
    }

    private fun signOut() {
        AlertDialog.Builder(this)
            .setTitle(R.string.sign_out_confirmation_title)
            .setMessage(R.string.sign_out_confirmation_message)
            .setNegativeButton(R.string.cancel_order, null)
            .setPositiveButton(R.string.sign_out) { _, _ ->
                userRepository.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .show()
    }

    private fun orderProgress(status: String): String = when (status) {
        "Pending" -> "25%"
        "Preparing" -> "50%"
        "Ready for Collection" -> "75%"
        "Collected" -> "100%"
        else -> "0%"
    }
}
