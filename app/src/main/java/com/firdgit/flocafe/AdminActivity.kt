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
    private lateinit var userRepository: UserRepository
    private lateinit var orderRepository: OrderRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        statusText = findViewById(R.id.adminStatusText)
        ordersText = findViewById(R.id.adminOrdersText)
        orderActions = findViewById(R.id.orderActions)
        userRepository = UserRepository(this)
        orderRepository = OrderRepository(userRepository)

        findViewById<Button>(R.id.logoutButton).setOnClickListener { signOut() }
        showAdminOrders()
    }

    private fun showAdminOrders() {
        orderActions.removeAllViews()
        lifecycleScope.launch {
            runCatching { orderRepository.allOrders() }
                .onSuccess { orders ->
                    ordersText.text = if (orders.isEmpty()) {
                        getString(R.string.no_orders)
                    } else {
                        getString(
                            R.string.admin_orders,
                            orders.joinToString("\n") {
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
                    orders.forEach { addStatusButton(it) }
                }
                .onFailure { error ->
                    ordersText.text = error.message ?: getString(R.string.orders_failed)
                }
        }
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
        "Pending" -> "1/4"
        "Preparing" -> "2/4"
        "Ready for Collection" -> "3/4"
        "Collected" -> "4/4"
        else -> "0/4"
    }
}
