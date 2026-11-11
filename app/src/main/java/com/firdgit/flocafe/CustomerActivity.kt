package com.firdgit.flocafe

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class CustomerActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var menuContainer: LinearLayout
    private lateinit var basketText: TextView
    private lateinit var ordersText: TextView
    private lateinit var ordersContainer: LinearLayout
    private lateinit var checkoutButton: Button
    private lateinit var userRepository: UserRepository
    private lateinit var orderRepository: OrderRepository
    private val basket = mutableMapOf<String, BasketLine>()
    private val quantityLabels = mutableMapOf<String, TextView>()
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)
        statusText = findViewById(R.id.customerStatusText)
        menuContainer = findViewById(R.id.menuContainer)
        basketText = findViewById(R.id.basketText)
        ordersText = findViewById(R.id.ordersText)
        ordersContainer = findViewById(R.id.ordersContainer)
        checkoutButton = findViewById(R.id.checkoutButton)
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
        checkoutButton.setOnClickListener {
            if (basket.isEmpty()) {
                statusText.text = getString(R.string.empty_basket)
                return@setOnClickListener
            }
            checkForRecentDuplicate()
        }

        MenuRepository().availableItems()
            .groupBy { it.category }
            .forEach { (category, items) ->
                addCategoryHeader(category)
                items.forEach(::addMenuItemRow)
            }
        updateBasketText()
    }

    private fun addCategoryHeader(category: String) {
        menuContainer.addView(
            TextView(this).apply {
                text = category
                textSize = 20f
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
                setPadding(0, dp(18), 0, dp(8))
            },
        )
    }

    private fun addMenuItemRow(item: MenuItem) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(14))
        }
        val itemContent = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val image = ImageView(this).apply {
            if (item.imageResId != 0) setImageResource(item.imageResId)
            contentDescription = getString(R.string.product_image_description, item.name)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        itemContent.addView(
            image,
            LinearLayout.LayoutParams(dp(72), dp(72)).apply {
                marginEnd = dp(12)
            },
        )
        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        details.addView(
            TextView(this).apply {
                text = item.name
                textSize = 17f
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleSmall)
            },
        )
        details.addView(
            TextView(this).apply {
                text = getString(R.string.menu_item_description, item.description, item.price)
                textSize = 14f
            },
        )
        itemContent.addView(
            details,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
        )
        row.addView(itemContent)

        val customisationLabel = TextView(this).apply {
            text = getString(R.string.customisation_label)
            setPadding(0, dp(8), 0, 0)
        }
        row.addView(customisationLabel)

        val customisationSpinner = Spinner(this).apply {
            contentDescription = getString(R.string.customisation_description, item.name)
            adapter = android.widget.ArrayAdapter(
                this@CustomerActivity,
                android.R.layout.simple_spinner_item,
                item.customisationOptions,
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        }
        row.addView(customisationSpinner)

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val addButton = Button(this).apply {
            text = getString(R.string.add_to_basket)
            contentDescription = getString(R.string.add_product_description, item.name)
            setOnClickListener {
                val selected = customisationSpinner.selectedItem?.toString() ?: "Standard"
                addItemToBasket(item, selected)
            }
        }
        actions.addView(addButton, LinearLayout.LayoutParams(0, dp(48), 1f))
        val quantityText = TextView(this).apply {
            text = getString(R.string.product_quantity, 0)
            textSize = 16f
            gravity = Gravity.CENTER
            contentDescription = getString(R.string.quantity_description, item.name)
        }
        quantityLabels[item.id] = quantityText
        actions.addView(quantityText, LinearLayout.LayoutParams(dp(88), dp(48)))
        row.addView(actions)
        menuContainer.addView(row)
    }

    private fun addItemToBasket(item: MenuItem, customisation: String) {
        val line = basket[item.id]
        if (line == null) {
            basket[item.id] = BasketLine(item, 1, customisation)
        } else {
            line.quantity += 1
            line.customisation = customisation
        }
        updateBasketText()
        statusText.text = getString(R.string.item_added, item.name)
    }

    private fun checkForRecentDuplicate() {
        if (isSubmitting) return
        statusText.text = getString(R.string.checking_order)
        lifecycleScope.launch {
            runCatching { orderRepository.findRecentDuplicate(basket.values.toList()) }
                .onSuccess { duplicate ->
                    if (duplicate == null) {
                        submitOrder()
                    } else {
                        showDuplicateConfirmation(duplicate.id)
                    }
                }
                .onFailure { error ->
                    statusText.text = error.message ?: getString(R.string.order_failed)
                }
        }
    }

    private fun showDuplicateConfirmation(orderId: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.duplicate_order_title)
            .setMessage(getString(R.string.duplicate_order_message, orderId))
            .setNegativeButton(R.string.cancel_order) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.confirm_duplicate_order) { _, _ -> submitOrder() }
            .show()
    }

    private fun submitOrder() {
        if (isSubmitting) return
        isSubmitting = true
        checkoutButton.isEnabled = false
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
            isSubmitting = false
            checkoutButton.isEnabled = true
        }
    }

    private fun loadCustomerOrders() {
        lifecycleScope.launch {
            runCatching { orderRepository.currentCustomerOrders() }
                .onSuccess { orders ->
                    ordersContainer.removeViews(1, (ordersContainer.childCount - 1).coerceAtLeast(0))
                    ordersText.text = if (orders.isEmpty()) {
                        getString(R.string.no_orders)
                    } else {
                        getString(R.string.current_orders_heading)
                    }
                    orders.forEach(::addOrderProgressCard)
                    if (orders.any { it.status == "Ready for Collection" || it.status == "Collected" }) {
                        statusText.text = getString(R.string.order_ready_notification)
                    }
                }
                .onFailure { error ->
                    ordersText.text = error.message ?: getString(R.string.orders_failed)
                }
        }
    }

    private fun addOrderProgressCard(order: OrderRepository.OrderSummary) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(10))
        }
        card.addView(
            TextView(this).apply {
                text = getString(
                    R.string.order_summary,
                    order.id,
                    order.status,
                    order.total,
                    orderProgress(order.status),
                )
                textSize = 15f
            },
        )
        card.addView(
            TextView(this).apply {
                text = getString(R.string.coffee_progress_label)
                setPadding(0, dp(4), 0, 0)
            },
        )
        card.addView(
            ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 4
                progress = orderProgress(order.status).substringBefore('/').toInt()
                contentDescription = getString(R.string.coffee_progress_description, order.status)
            },
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(12),
            ),
        )
        ordersContainer.addView(card)
    }

    private fun updateBasketText() {
        basketText.text = if (basket.isEmpty()) {
            getString(R.string.basket_empty)
        } else {
            getString(
                R.string.basket_summary,
                basket.values.sumOf { it.quantity },
                basketTotal(),
                basket.values.joinToString("\n") {
                    getString(R.string.basket_line, it.item.name, it.customisation, it.quantity)
                },
            )
        }
        basket.forEach { (id, line) ->
            quantityLabels[id]?.text = getString(R.string.product_quantity, line.quantity)
        }
        quantityLabels.filterKeys { it !in basket.keys }.values.forEach {
            it.text = getString(R.string.product_quantity, 0)
        }
    }

    private fun basketTotal(): String =
        "%.2f".format(basket.values.sumOf { it.item.price * it.quantity })

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

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
