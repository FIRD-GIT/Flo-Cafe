package com.firdgit.flocafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CustomerActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var menuContainer: LinearLayout
    private lateinit var basketText: TextView
    private lateinit var ordersText: TextView
    private lateinit var userRepository: UserRepository
    private lateinit var orderRepository: OrderRepository
    private val basket = mutableMapOf<String, BasketLine>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)
        statusText = findViewById(R.id.customerStatusText)
        menuContainer = findViewById(R.id.menuContainer)
        basketText = findViewById(R.id.basketText)
        ordersText = findViewById(R.id.ordersText)
        userRepository = UserRepository(this)
        orderRepository = OrderRepository(userRepository)

        findViewById<Button>(R.id.logoutButton).setOnClickListener { signOut() }
        loadCustomerGreeting()
        showCustomerMenu()
        loadCustomerOrders()
    }

    private fun loadCustomerGreeting() {
        lifecycleScope.launch {
            val displayName = runCatching { userRepository.currentUserDisplayName() }
                .getOrNull()
                ?: getString(R.string.default_customer_name)
            statusText.text = getString(R.string.customer_welcome_message, displayName)
        }
    }

    private fun showCustomerMenu() {
        findViewById<Button>(R.id.checkoutButton).setOnClickListener {
            if (basket.isEmpty()) {
                statusText.text = getString(R.string.empty_basket)
                return@setOnClickListener
            }
            statusText.text = getString(R.string.order_submitting)
            lifecycleScope.launch {
                runCatching { orderRepository.submitOrder(basket.values.toList()) }
                    .onSuccess { orderId ->
                        basket.clear()
                        updateBasketText()
                        statusText.text = getString(R.string.order_submitted, orderId)
                        loadCustomerOrders()
                    }
                    .onFailure { error ->
                        statusText.text = error.message ?: getString(R.string.order_failed)
                    }
            }
        }

        MenuRepository().availableItems().forEach { item ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 12, 0, 12)
            }
            val itemText = TextView(this).apply {
                text = getString(R.string.menu_item_format, item.name, item.description, item.price)
                textSize = 16f
            }
            val addButton = Button(this).apply {
                text = getString(R.string.add_to_basket)
                setOnClickListener {
                    val line = basket[item.id]
                    if (line == null) {
                        basket[item.id] = BasketLine(item, 1)
                    } else {
                        line.quantity += 1
                    }
                    updateBasketText()
                }
            }
            row.addView(itemText)
            row.addView(addButton)
            menuContainer.addView(row)
        }
        updateBasketText()
    }

    private fun loadCustomerOrders() {
        lifecycleScope.launch {
            runCatching { orderRepository.currentCustomerOrders() }
                .onSuccess { orders ->
                    ordersText.text = if (orders.isEmpty()) {
                        getString(R.string.no_orders)
                    } else {
                        getString(
                            R.string.current_orders,
                            orders.joinToString("\n") {
                                getString(R.string.order_summary, it.id, it.status, it.total)
                            },
                        )
                    }
                }
                .onFailure { error ->
                    ordersText.text = error.message ?: getString(R.string.orders_failed)
                }
        }
    }

    private fun updateBasketText() {
        basketText.text = if (basket.isEmpty()) {
            getString(R.string.basket_empty)
        } else {
            getString(R.string.basket_summary, basket.values.sumOf { it.quantity }, basketTotal())
        }
    }

    private fun basketTotal(): String =
        "%.2f".format(basket.values.sumOf { it.item.price * it.quantity })

    private fun signOut() {
        userRepository.signOut()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
