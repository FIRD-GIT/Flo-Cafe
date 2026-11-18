package com.firdgit.flocafe

import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
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
            getString(R.string.admin_incoming_count, filtered.size)
        }
        filtered.forEach { addStatusButton(it) }
    }

    private fun renderAnalytics(orders: List<OrderRepository.OrderSummary>) {
        val summary = AnalyticsCalculator.summarise(orders, allFeedback)
        findViewById<TextView>(R.id.adminMetricOrders).text =
            getString(R.string.admin_metric_orders, summary.orderCount)
        findViewById<TextView>(R.id.adminMetricSales).text =
            getString(R.string.admin_metric_sales, PriceFormatter.format(summary.totalSales))
        findViewById<TextView>(R.id.adminMetricCollected).text =
            getString(R.string.admin_metric_collected, summary.collectedCount)
        findViewById<TextView>(R.id.adminMetricAverage).text =
            getString(R.string.admin_metric_average, PriceFormatter.format(summary.averageOrderValue))
        findViewById<TextView>(R.id.adminMetricRating).text =
            getString(
                R.string.admin_metric_rating,
                if (summary.averageRating == 0.0) "N/A" else String.format("%.1f", summary.averageRating),
            )
        findViewById<TextView>(R.id.adminMetricTopItem).text =
            getString(R.string.admin_metric_top, summary.topItemName)
    }

    private fun addStatusButton(order: OrderRepository.OrderSummary) {
        val nextStatus = when (order.status) {
            "Pending" -> "Preparing"
            "Preparing" -> "Ready for Collection"
            "Ready for Collection" -> "Collected"
            else -> null
        }
        val card = MaterialCardView(this).apply {
            radius = dp(16).toFloat()
            cardElevation = dp(2).toFloat()
            setContentPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                topMargin = dp(6)
                bottomMargin = dp(6)
            }
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        content.addView(TextView(this).apply {
            text = getString(R.string.order_short_reference, order.id.takeLast(6).uppercase())
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#24202B"))
        })
        content.addView(TextView(this).apply {
            text = getString(R.string.admin_order_card_status, order.status, orderProgress(order.status))
            textSize = 14f
            setTextColor(Color.parseColor("#6B6673"))
            setPadding(0, dp(4), 0, dp(4))
        })
        content.addView(TextView(this).apply {
            text = getString(R.string.admin_order_card_total, PriceFormatter.format(order.total))
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#6A35C9"))
        })
        if (nextStatus != null) {
            content.addView(Button(this).apply {
                text = getString(R.string.advance_to_status, nextStatus)
                isAllCaps = false
                minHeight = dp(44)
                setPadding(dp(12), 0, dp(12), 0)
                setOnClickListener {
                    lifecycleScope.launch {
                        runCatching { orderRepository.updateStatus(order.id, nextStatus) }
                            .onSuccess {
                                statusText.text = getString(R.string.status_updated, nextStatus)
                                showAdminOrders()
                            }
                            .onFailure { error ->
                                statusText.text = error.message ?: getString(R.string.status_update_failed)
                            }
                    }
                }
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44),
            ).apply {
                topMargin = dp(8)
            })
        } else {
            content.addView(TextView(this).apply {
                text = getString(R.string.order_collected_label)
                setTextColor(Color.parseColor("#4F7A5A"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(0, dp(8), 0, 0)
            })
        }
        card.addView(content)
        orderActions.addView(card)
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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
