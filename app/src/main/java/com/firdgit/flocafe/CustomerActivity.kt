package com.firdgit.flocafe

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomerActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_DEMO_MODE = "demo_mode"
    }

    private lateinit var contentFrame: FrameLayout
    private lateinit var statusText: TextView
    private lateinit var userRepository: UserRepository
    private lateinit var orderRepository: OrderRepository
    private val basket = mutableListOf<BasketLine>()
    private val favourites = mutableSetOf<String>()
    private var orders = emptyList<OrderRepository.OrderSummary>()
    private var displayName = "Customer"
    private var selectedTab = Tab.HOME
    private var isSubmitting = false
    private val isDemoMode by lazy { intent.getBooleanExtra(EXTRA_DEMO_MODE, false) }

    private enum class Tab { HOME, MENU, BASKET, PROFILE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)
        contentFrame = findViewById(R.id.customerContentFrame)
        statusText = findViewById(R.id.customerStatusText)
        userRepository = UserRepository(this)
        orderRepository = OrderRepository(userRepository)

        findViewById<Button>(R.id.homeTabButton).setOnClickListener { showTab(Tab.HOME) }
        findViewById<Button>(R.id.menuTabButton).setOnClickListener { showTab(Tab.MENU) }
        findViewById<Button>(R.id.basketTabButton).setOnClickListener { showTab(Tab.BASKET) }
        findViewById<Button>(R.id.profileTabButton).setOnClickListener { showTab(Tab.PROFILE) }

        if (isDemoMode) {
            displayName = "Demo customer"
            statusText.text = getString(R.string.demo_mode_message)
        } else {
            loadCustomerDetails()
        }
        showTab(Tab.HOME)
    }

    private fun loadCustomerDetails() {
        lifecycleScope.launch {
            displayName = runCatching { userRepository.currentUserDisplayName() }
                .getOrNull()
                ?.takeIf { it.isNotBlank() }
                ?: getString(R.string.default_customer_name)
            statusText.text = getString(R.string.customer_welcome_message, displayName)
            loadCustomerOrders()
            showTab(selectedTab)
        }
    }

    private fun loadCustomerOrders() {
        if (isDemoMode) return
        lifecycleScope.launch {
            orders = runCatching { orderRepository.currentCustomerOrders() }.getOrElse {
                emptyList()
            }
            if (selectedTab == Tab.HOME || selectedTab == Tab.PROFILE) showTab(selectedTab)
        }
    }

    private fun showTab(tab: Tab) {
        selectedTab = tab
        contentFrame.removeAllViews()
        contentFrame.addView(
            when (tab) {
                Tab.HOME -> homeView()
                Tab.MENU -> menuView()
                Tab.BASKET -> basketView()
                Tab.PROFILE -> profileView()
            },
        )
        updateNavigationButtons()
    }

    private fun updateNavigationButtons() {
        val buttons = mapOf(
            Tab.HOME to findViewById<Button>(R.id.homeTabButton),
            Tab.MENU to findViewById<Button>(R.id.menuTabButton),
            Tab.BASKET to findViewById<Button>(R.id.basketTabButton),
            Tab.PROFILE to findViewById<Button>(R.id.profileTabButton),
        )
        buttons.forEach { (tab, button) ->
            button.setTextColor(Color.parseColor(if (tab == selectedTab) "#6A35C9" else "#6B6673"))
            button.setBackgroundColor(Color.parseColor(if (tab == selectedTab) "#F0E9FF" else "#FFFFFF"))
        }
    }

    private fun homeView(): View = verticalScroll {
        addView(title(getString(R.string.home_greeting, displayName), 28f))
        addView(body(getString(R.string.home_subtitle)))
        addView(sectionTitle(getString(R.string.featured_heading)))
        MenuRepository().availableItems().take(3).forEach { addView(productCard(it, compact = true)) }
        addView(sectionTitle(getString(R.string.categories_heading)))
        listOf(
            getString(R.string.category_hot_drinks),
            getString(R.string.category_cold_drinks),
            getString(R.string.category_sweet_treats),
            getString(R.string.category_sandwiches),
        ).forEach { category ->
            addView(actionButton(category) {
                showTab(Tab.MENU)
            })
        }
        addView(sectionTitle(getString(R.string.order_status_heading)))
        if (orders.isEmpty()) {
            addView(body(if (isDemoMode) getString(R.string.demo_orders_disabled) else getString(R.string.no_orders)))
        } else {
            orders.take(2).forEach { addView(orderCard(it)) }
        }
        addView(sectionTitle(getString(R.string.quick_actions_heading)))
        addView(actionButton(getString(R.string.view_menu)) { showTab(Tab.MENU) })
        addView(actionButton(getString(R.string.view_basket)) { showTab(Tab.BASKET) })
    }

    private fun menuView(): View = verticalScroll {
        addView(title(getString(R.string.menu_heading), 28f))
        addView(body(getString(R.string.menu_subtitle)))
        val categories = listOf(
            getString(R.string.category_all),
            getString(R.string.category_hot_drinks),
            getString(R.string.category_cold_drinks),
            getString(R.string.category_sweet_treats),
            getString(R.string.category_sandwiches),
        )
        val categoryRow = horizontalRow()
        categories.forEach { category ->
            categoryRow.addView(actionButton(category) {
                renderMenuItems(category)
            }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(4) })
        }
        addView(categoryRow)
        addView(menuItemsContainer())
    }

    private fun renderMenuItems(category: String) {
        val container = contentFrame.findViewWithTag<LinearLayout>("menu_items")
            ?: return
        container.removeAllViews()
        val items = MenuRepository().availableItems().filter {
            category == getString(R.string.category_all) || it.category == category
        }
        items.groupBy { it.category }.forEach { (group, groupedItems) ->
            container.addView(sectionTitle(group))
            groupedItems.forEach { container.addView(productCard(it, compact = false)) }
        }
    }

    private fun menuItemsContainer(): LinearLayout = LinearLayout(this).apply {
        tag = "menu_items"
        orientation = LinearLayout.VERTICAL
        setPadding(dp(2), dp(12), dp(2), dp(16))
        MenuRepository().availableItems().groupBy { it.category }.forEach { (group, items) ->
            addView(sectionTitle(group))
            items.forEach { addView(productCard(it, compact = false)) }
        }
    }

    private fun productCard(item: MenuItem, compact: Boolean): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedBackground("#FFFFFF", 18)
            elevation = dp(1).toFloat()
        }
        val image = ImageView(this).apply {
            setImageResource(item.imageResId)
            contentDescription = getString(R.string.product_image_description, item.name)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        card.addView(image, LinearLayout.LayoutParams(dp(if (compact) 72 else 88), dp(if (compact) 72 else 88)).apply {
            marginEnd = dp(12)
        })
        val details = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        details.addView(title(item.name, if (compact) 18f else 20f))
        details.addView(body(getString(R.string.product_card_details, item.calories, item.description)))
        details.addView(body(PriceFormatter.format(item.price)))
        val customise = actionButton(
            getString(if (item.isCustomisable) R.string.customise_item else R.string.add_to_basket),
        ) {
            if (item.isCustomisable) {
                showProductDetail(item)
            } else {
                addItemToBasket(item, getString(R.string.standard_customisation))
            }
        }
        details.addView(customise)
        card.addView(details, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        return card
    }

    private fun showProductDetail(item: MenuItem) {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(4), dp(20), 0)
        }
        form.addView(body(getString(R.string.nutrition_estimate_notice, item.nutritionNote)))
        val selections = linkedMapOf(
            getString(R.string.option_size) to item.sizeOptions,
            getString(R.string.option_milk) to item.milkOptions,
            getString(R.string.option_shots) to item.shotOptions,
            getString(R.string.option_temperature) to item.temperatureOptions,
            getString(R.string.option_sweetness) to item.sweetnessOptions,
            getString(R.string.option_syrup) to item.syrupOptions,
            getString(R.string.option_topping) to item.toppingOptions,
        )
        val spinners = selections.map { (label, values) ->
            form.addView(body(label))
            Spinner(this).also { spinner ->
                spinner.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    values,
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                form.addView(spinner)
            }
        }
        val whippedCream = CheckBox(this).apply {
            text = getString(R.string.option_whipped_cream)
        }
        form.addView(whippedCream)
        form.addView(body(getString(R.string.nutrition_summary, item.calories)))

        AlertDialog.Builder(this)
            .setTitle(item.name)
            .setView(form)
            .setNegativeButton(R.string.cancel_order, null)
            .setPositiveButton(R.string.add_to_basket) { _, _ ->
                val summary = selections.keys.zip(spinners.map { it.selectedItem.toString() })
                    .joinToString(", ") { (label, value) -> "$label: $value" } +
                    if (whippedCream.isChecked) ", ${getString(R.string.option_whipped_cream)}" else ""
                addItemToBasket(item, summary)
            }
            .show()
    }

    private fun basketView(): View = verticalScroll {
        addView(title(getString(R.string.basket_heading), 28f))
        if (basket.isEmpty()) {
            addView(body(getString(R.string.basket_empty)))
            addView(actionButton(getString(R.string.add_items)) { showTab(Tab.MENU) })
            return@verticalScroll
        }
        basket.toList().forEachIndexed { index, line ->
            addView(basketLineCard(index, line))
        }
        addView(sectionTitle(getString(R.string.basket_total_heading)))
        addView(title(PriceFormatter.format(basketTotal()), 24f))
        addView(actionButton(getString(R.string.checkout_preview_button)) {
            checkForRecentDuplicate()
        })
        addView(actionButton(getString(R.string.clear_basket)) {
            basket.clear()
            showTab(Tab.BASKET)
        })
    }

    private fun basketLineCard(index: Int, line: BasketLine): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedBackground("#FFFFFF", 18)
            elevation = dp(1).toFloat()
        }
        row.addView(title(line.item.name, 19f))
        row.addView(body(line.customisation))
        row.addView(body(getString(R.string.product_nutrition_short, line.item.calories)))
        val controls = horizontalRow()
        controls.addView(actionButton("-") {
            if (line.quantity > 1) line.quantity-- else basket.removeAt(index)
            showTab(Tab.BASKET)
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
        controls.addView(body(getString(R.string.product_quantity, line.quantity)).apply {
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
        controls.addView(actionButton("+") {
            line.quantity++
            showTab(Tab.BASKET)
        }, LinearLayout.LayoutParams(0, dp(48), 1f))
        row.addView(controls)
        return row
    }

    private fun profileView(): View = verticalScroll {
        addView(title(getString(R.string.profile_heading), 28f))
        addView(body(getString(R.string.profile_email, userRepository.currentUserEmail ?: "Demo account")))
        addView(actionButton(getString(R.string.edit_profile)) { editDisplayName() })
        addView(sectionTitle(getString(R.string.profile_orders_heading)))
        if (orders.isEmpty()) addView(body(getString(R.string.no_orders)))
        else orders.forEach { addView(orderCard(it)) }
        addView(sectionTitle(getString(R.string.favourites_heading)))
        addView(body(if (favourites.isEmpty()) getString(R.string.no_favourites) else favourites.joinToString(", ")))
        addView(sectionTitle(getString(R.string.preferences_heading)))
        listOf(
            getString(R.string.preference_vegetarian),
            getString(R.string.preference_dairy_free),
            getString(R.string.preference_lower_sugar),
        ).forEach { preference ->
            addView(CheckBox(this@CustomerActivity).apply { text = preference })
        }
        addView(actionButton(getString(R.string.sign_out)) { signOut() })
    }

    private fun orderCard(order: OrderRepository.OrderSummary): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = roundedBackground("#FFFFFF", 18)
            elevation = dp(2).toFloat()
            setOnClickListener { showOrderDetails(order) }
            contentDescription = getString(R.string.order_card_description, orderDisplayReference(order))
        }
        val firstItem = order.items.firstOrNull()
        val image = ImageView(this).apply {
            setImageResource(
                MenuRepository().availableItems()
                    .firstOrNull { it.id == firstItem?.menuItemId }
                    ?.imageResId
                    ?: R.drawable.product_latte,
            )
            contentDescription = getString(
                R.string.order_image_description,
                firstItem?.name ?: getString(R.string.order_default_item),
            )
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        card.addView(image, LinearLayout.LayoutParams(dp(72), dp(72)).apply {
            marginEnd = dp(12)
        })
        val details = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        details.addView(title(orderDisplayReference(order), 18f))
        details.addView(
            body(
                getString(
                    R.string.order_card_items,
                    order.items.joinToString(", ") { "${it.name} x${it.quantity}" }
                        .ifBlank { getString(R.string.order_default_item) },
                ),
            ),
        )
        details.addView(body(getString(R.string.order_card_meta, order.status, orderDate(order))))
        details.addView(body(PriceFormatter.format(order.total)))
        card.addView(details, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        return card
    }

    private fun showOrderDetails(order: OrderRepository.OrderSummary) {
        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), 0, dp(20), 0)
        }
        details.addView(body(getString(R.string.order_detail_reference, orderDisplayReference(order))))
        details.addView(body(getString(R.string.order_detail_date, orderDate(order))))
        details.addView(body(getString(R.string.order_detail_status, order.status)))
        details.addView(sectionTitle(getString(R.string.order_detail_items_heading)))
        order.items.forEach { item ->
            details.addView(
                body(
                    getString(
                        R.string.order_detail_item,
                        item.name,
                        item.quantity,
                        PriceFormatter.format(item.unitPrice),
                        item.customisation,
                    ),
                ),
            )
        }
        details.addView(sectionTitle(getString(R.string.order_detail_total_heading)))
        details.addView(title(PriceFormatter.format(order.total), 22f))
        details.addView(body(getString(R.string.order_detail_full_reference, order.id)))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.order_detail_title, orderDisplayReference(order)))
            .setView(details)
            .setPositiveButton(R.string.close, null)
            .show()
    }

    private fun orderDisplayReference(order: OrderRepository.OrderSummary): String =
        getString(R.string.order_short_reference, order.id.takeLast(6).uppercase(Locale.UK))

    private fun orderDate(order: OrderRepository.OrderSummary): String =
        order.createdAtMillis?.let {
            SimpleDateFormat("d MMM yyyy, HH:mm", Locale.UK).format(Date(it))
        } ?: getString(R.string.order_date_pending)

    private fun addItemToBasket(item: MenuItem, customisation: String) {
        val existing = basket.firstOrNull {
            it.item.id == item.id && it.customisation == customisation
        }
        if (existing == null) basket.add(BasketLine(item, 1, customisation))
        else existing.quantity++
        favourites.add(item.name)
        statusText.text = getString(R.string.item_added, item.name)
        showTab(Tab.BASKET)
    }

    private fun checkForRecentDuplicate() {
        if (basket.isEmpty()) {
            statusText.text = getString(R.string.empty_basket)
            return
        }
        if (isDemoMode) {
            statusText.text = getString(R.string.demo_checkout_disabled)
            return
        }
        if (isSubmitting) return
        statusText.text = getString(R.string.checking_order)
        lifecycleScope.launch {
            runCatching { orderRepository.findRecentDuplicate(basket) }
                .onSuccess { duplicate ->
                    if (duplicate == null) submitOrder()
                    else showDuplicateConfirmation(duplicate.id)
                }
                .onFailure { error -> statusText.text = error.message ?: getString(R.string.order_failed) }
        }
    }

    private fun showDuplicateConfirmation(orderId: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.duplicate_order_title)
            .setMessage(getString(R.string.duplicate_order_message, orderId))
            .setNegativeButton(R.string.cancel_order, null)
            .setPositiveButton(R.string.confirm_duplicate_order) { _, _ -> submitOrder() }
            .show()
    }

    private fun submitOrder() {
        if (isSubmitting) return
        isSubmitting = true
        lifecycleScope.launch {
            runCatching { orderRepository.submitOrder(basket) }
                .onSuccess { orderId ->
                    basket.clear()
                    statusText.text = getString(R.string.order_submitted, orderId)
                    loadCustomerOrders()
                    showTab(Tab.HOME)
                }
                .onFailure { error -> statusText.text = error.message ?: getString(R.string.order_failed) }
            isSubmitting = false
        }
    }

    private fun editDisplayName() {
        val input = android.widget.EditText(this).apply { setText(displayName) }
        AlertDialog.Builder(this)
            .setTitle(R.string.edit_profile)
            .setView(input)
            .setNegativeButton(R.string.cancel_order, null)
            .setPositiveButton(R.string.save_profile) { _, _ ->
                displayName = input.text.toString().trim().ifBlank { displayName }
                statusText.text = getString(R.string.profile_saved)
                showTab(Tab.PROFILE)
            }
            .show()
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

    private fun verticalScroll(content: LinearLayout.() -> Unit): ScrollView =
        ScrollView(this).apply {
            isFillViewport = true
            addView(LinearLayout(this@CustomerActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(4), dp(12), dp(4), dp(24))
                content()
            })
        }

    private fun horizontalRow() = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    private fun title(value: String, size: Float) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(Color.parseColor("#24202B"))
        setPadding(0, dp(8), 0, dp(8))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun sectionTitle(value: String) = title(value, 21f)

    private fun body(value: String) = TextView(this).apply {
        text = value
        textSize = 15f
        setTextColor(Color.parseColor("#6B6673"))
        setPadding(0, dp(4), 0, dp(6))
    }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = roundedBackground("#6A35C9", 28)
        minHeight = dp(48)
        stateListAnimator = null
        setOnClickListener { action() }
        contentDescription = label
    }

    private fun roundedBackground(color: String, radius: Int) =
        GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = dp(radius).toFloat()
        }

    private fun basketTotal(): Double =
        basket.sumOf { it.item.price * it.quantity }

    private fun orderProgress(status: String): String = when (status) {
        "Pending" -> "1/4"
        "Preparing" -> "2/4"
        "Ready for Collection" -> "3/4"
        "Collected" -> "4/4"
        else -> "0/4"
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
