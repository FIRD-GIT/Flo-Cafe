package com.firdgit.flocafe

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.ListenerRegistration
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
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
    private lateinit var feedbackRepository: FeedbackRepository
    private val basket = mutableListOf<BasketLine>()
    private val favourites = mutableSetOf<String>()
    private var orders = emptyList<OrderRepository.OrderSummary>()
    private var displayName = "Customer"
    private var selectedTab = Tab.HOME
    private var selectedMenuCategory: String? = null
    private var menuSearchQuery: String = ""
    private var isSubmitting = false
    private var orderListener: ListenerRegistration? = null
    private val lastKnownStatuses = mutableMapOf<String, String>()
    private val isDemoMode by lazy { intent.getBooleanExtra(EXTRA_DEMO_MODE, false) }

    private enum class Tab { HOME, MENU, BASKET, PROFILE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer)
        contentFrame = findViewById(R.id.customerContentFrame)
        statusText = findViewById(R.id.customerStatusText)
        userRepository = UserRepository(this)
        orderRepository = OrderRepository(userRepository)
        feedbackRepository = FeedbackRepository(userRepository)

        findViewById<BottomNavigationView>(R.id.customerBottomNavigation).setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showTab(Tab.HOME)
                R.id.nav_menu -> showTab(Tab.MENU)
                R.id.nav_basket -> showTab(Tab.BASKET)
                R.id.nav_profile -> showTab(Tab.PROFILE)
            }
            true
        }

        if (isDemoMode) {
            displayName = "Demo customer"
            statusText.text = getString(R.string.demo_mode_message)
        } else {
            loadCustomerDetails()
        }
        showTab(Tab.HOME)
        observeOrders()
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

    private fun observeOrders() {
        if (isDemoMode) return
        orderListener = orderRepository.observeCustomerOrders(
            onUpdate = { updatedOrders ->
                val changedReadyOrder = updatedOrders.firstOrNull { order ->
                    order.status == "Ready for Collection" &&
                        lastKnownStatuses[order.id] != null &&
                        lastKnownStatuses[order.id] != order.status
                }
                orders = updatedOrders.sortedByDescending { it.createdAtMillis ?: 0L }
                updatedOrders.forEach { lastKnownStatuses[it.id] = it.status }
                changedReadyOrder?.let { showStageNotification(it) }
                if (selectedTab == Tab.HOME || selectedTab == Tab.PROFILE) showTab(selectedTab)
            },
            onError = { statusText.text = getString(R.string.orders_failed) },
        )
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
        val navigation = findViewById<BottomNavigationView>(R.id.customerBottomNavigation)
        navigation.setOnItemSelectedListener(null)
        navigation.selectedItemId = when (selectedTab) {
            Tab.HOME -> R.id.nav_home
            Tab.MENU -> R.id.nav_menu
            Tab.BASKET -> R.id.nav_basket
            Tab.PROFILE -> R.id.nav_profile
        }
        navigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showTab(Tab.HOME)
                R.id.nav_menu -> showTab(Tab.MENU)
                R.id.nav_basket -> showTab(Tab.BASKET)
                R.id.nav_profile -> showTab(Tab.PROFILE)
            }
            true
        }
    }

    private fun homeView(): View = verticalScroll {
        addView(title(getString(R.string.home_greeting, displayName), 28f))
        addView(body(getString(R.string.home_subtitle)))
        orders.firstOrNull { it.status != "Collected" }?.let { addView(orderProgressBanner(it)) }
        addView(
            secondaryButton(getString(R.string.ai_builder_button)) { showAiBuilderPrototype() },
            spacedParams(top = 8, bottom = 4),
        )
        addView(sectionTitle(getString(R.string.featured_heading)))
        MenuRepository().availableItems().take(3).forEach { addView(productCard(it, compact = true)) }
        addView(sectionTitle(getString(R.string.categories_heading)))
        addView(categoryGrid())
        addView(sectionTitle(getString(R.string.order_status_heading)))
        if (orders.isEmpty()) {
            addView(body(if (isDemoMode) getString(R.string.demo_orders_disabled) else getString(R.string.no_orders)))
        } else {
            orders.take(2).forEach { addView(orderCard(it)) }
        }
        addView(sectionTitle(getString(R.string.quick_actions_heading)))
        addView(
            actionButton(getString(R.string.view_menu)) { showTab(Tab.MENU) },
            spacedParams(top = 6, bottom = 5),
        )
        addView(
            actionButton(getString(R.string.view_basket)) { showTab(Tab.BASKET) },
            spacedParams(top = 5, bottom = 6),
        )
    }

    private fun orderProgressBanner(order: OrderRepository.OrderSummary): View {
        val progress = OrderProgress.percent(order.status)
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(14))
            background = roundedBackground("#F0E9FF", 20)
            addView(horizontalRow().apply {
                addView(ImageView(this@CustomerActivity).apply {
                    setImageResource(R.drawable.flo_cafe_cup)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    contentDescription = getString(R.string.coffee_progress_description, order.status)
                }, LinearLayout.LayoutParams(dp(54), dp(54)).apply { marginEnd = dp(12) })
                addView(LinearLayout(this@CustomerActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(title(getString(R.string.home_progress_title), 18f))
                    addView(body(getString(R.string.home_progress_status, order.status)))
                }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            })
            addView(android.widget.ProgressBar(this@CustomerActivity, null, android.R.attr.progressBarStyleHorizontal).apply {
                max = 100
                this.progress = progress
                progressDrawable = resources.getDrawable(R.drawable.cup_progress_bar, theme)
                contentDescription = getString(R.string.coffee_progress_description, order.status)
            }, LinearLayout.LayoutParams.MATCH_PARENT, dp(10))
        }.apply { layoutParams = spacedParams(top = 6, bottom = 8) }
    }

    private fun menuView(): View = verticalScroll {
        addView(title(getString(R.string.menu_heading), 28f))
        addView(body(getString(R.string.menu_subtitle)))
        val searchInput = android.widget.EditText(this@CustomerActivity).apply {
            hint = getString(R.string.menu_search_hint)
            background = roundedBackground("#FFFFFF", 18)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    menuSearchQuery = s?.toString().orEmpty()
                    renderMenuItems(selectedMenuCategory ?: getString(R.string.category_all))
                }
                override fun afterTextChanged(s: android.text.Editable?) = Unit
            })
        }
        addView(searchInput, spacedParams(top = 6, bottom = 6))
        addView(
            secondaryButton(getString(R.string.category_all)) {
                renderMenuItems(getString(R.string.category_all))
            },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)).apply {
                setMargins(dp(4), dp(4), dp(4), dp(8))
            },
        )
        addView(categoryGrid())
        addView(menuItemsContainer(selectedMenuCategory))
    }

    private fun renderMenuItems(category: String) {
        selectedMenuCategory = category.takeUnless { it == getString(R.string.category_all) }
        val container = contentFrame.findViewWithTag<LinearLayout>("menu_items")
            ?: return
        container.removeAllViews()
        val items = MenuRepository().searchItems(
            menuSearchQuery,
            selectedMenuCategory,
        )
        if (items.isEmpty()) {
            container.addView(body(getString(R.string.menu_no_matches)))
            return
        }
        items.groupBy { it.category }.forEach { (group, groupedItems) ->
            container.addView(sectionTitle(group))
            groupedItems.forEach { container.addView(productCard(it, compact = false)) }
        }
    }

    private fun categoryGrid(): View {
        val categories = listOf(
            getString(R.string.category_hot_drinks),
            getString(R.string.category_cold_drinks),
            getString(R.string.category_sweet_treats),
            getString(R.string.category_sandwiches),
        )
        return GridLayout(this).apply {
            columnCount = 2
            rowCount = (categories.size + 1) / 2
            useDefaultMargins = false
            setPadding(0, dp(4), 0, dp(10))
            categories.forEachIndexed { index, category ->
                val chip = com.google.android.material.chip.Chip(this@CustomerActivity).apply {
                    text = category
                    isCheckable = true
                    isChecked = selectedMenuCategory == category
                    chipMinHeight = dp(40).toFloat()
                    setEnsureMinTouchTargetSize(true)
                    setOnClickListener {
                        selectedMenuCategory = category
                        showTab(Tab.MENU)
                    }
                }
                addView(chip, GridLayout.LayoutParams().apply {
                    width = 0
                    height = dp(48)
                    columnSpec = GridLayout.spec(index % 2, 1f)
                    rowSpec = GridLayout.spec(index / 2)
                    setMargins(dp(4), dp(4), dp(4), dp(4))
                })
            }
        }
    }

    private fun menuItemsContainer(category: String?): LinearLayout = LinearLayout(this).apply {
        tag = "menu_items"
        orientation = LinearLayout.VERTICAL
        setPadding(dp(2), dp(12), dp(2), dp(16))
        MenuRepository().searchItems(menuSearchQuery, category)
            .groupBy { it.category }
            .forEach { (group, items) ->
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
        val actions = horizontalRow().apply { setPadding(0, dp(4), 0, 0) }
        if (item.isCustomisable) {
            actions.addView(actionButton(getString(R.string.add_to_basket)) {
                addItemToBasket(item, getString(R.string.standard_customisation))
            }, LinearLayout.LayoutParams(0, dp(44), 1f).apply { marginEnd = dp(6) })
            actions.addView(secondaryButton(getString(R.string.customise_item)) {
                showProductDetail(item)
            }, LinearLayout.LayoutParams(0, dp(44), 1f))
        } else {
            actions.addView(actionButton(getString(R.string.add_to_basket)) {
                addItemToBasket(item, getString(R.string.standard_customisation))
            }, LinearLayout.LayoutParams.MATCH_PARENT, dp(44))
        }
        details.addView(actions)
        card.addView(details, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        card.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = dp(6)
            bottomMargin = dp(6)
        }
        return card
    }

    private fun showProductDetail(item: MenuItem) {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(4), dp(20), 0)
        }
        form.addView(body(getString(R.string.nutrition_estimate_notice, item.nutritionNote)))
        val groups = item.customisationGroups.ifEmpty {
            listOf(
                CustomisationGroup(getString(R.string.option_size), item.sizeOptions),
                CustomisationGroup(getString(R.string.option_milk), item.milkOptions),
                CustomisationGroup(getString(R.string.option_shots), item.shotOptions),
                CustomisationGroup(getString(R.string.option_temperature), item.temperatureOptions),
                CustomisationGroup(getString(R.string.option_sweetness), item.sweetnessOptions),
                CustomisationGroup(getString(R.string.option_syrup), item.syrupOptions),
                CustomisationGroup(getString(R.string.option_topping), item.toppingOptions),
            )
        }
        val spinners = groups.map { group ->
            form.addView(body(group.title))
            Spinner(this).also { spinner ->
                spinner.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    group.options,
                ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
                form.addView(spinner)
            }
        }
        form.addView(body(getString(R.string.nutrition_summary, item.calories)))

        AlertDialog.Builder(this)
            .setTitle(item.name)
            .setView(form)
            .setNegativeButton(R.string.cancel_order, null)
            .setPositiveButton(R.string.add_to_basket) { _, _ ->
                val summary = groups.zip(spinners)
                    .joinToString(", ") { (group, spinner) ->
                        "${group.title}: ${spinner.selectedItem}"
                    }
                addItemToBasket(item, summary)
            }
            .show()
    }

    private fun showAiBuilderPrototype() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), dp(24))
        }
        val milk = Spinner(this)
        milk.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Semi-skimmed", "Oat", "Soya", "Almond"))
        val flavour = Spinner(this)
        flavour.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("No flavour", "Vanilla", "Caramel", "Chocolate"))
        val sweetness = Spinner(this)
        sweetness.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Regular", "Less sweet", "Extra sweet", "Unsweetened"))
        form.addView(body(getString(R.string.ai_builder_milk)))
        form.addView(milk)
        form.addView(body(getString(R.string.ai_builder_flavour)))
        form.addView(flavour)
        form.addView(body(getString(R.string.ai_builder_sweetness)))
        form.addView(sweetness)
        val recommendation = TextView(this).apply {
            text = getString(R.string.ai_builder_result_placeholder)
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#24202B"))
            setPadding(0, dp(16), 0, dp(8))
        }
        form.addView(recommendation)
        val sheet = BottomSheetDialog(this)
        fun updatePreview() {
            recommendation.text = DrinkRecommendation.recommend(
                DrinkRecommendation.Preferences(
                    milk = milk.selectedItem?.toString().orEmpty(),
                    flavour = flavour.selectedItem?.toString().orEmpty(),
                    sweetness = sweetness.selectedItem?.toString().orEmpty(),
                ),
            )
        }
        val preferenceSelected = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) =
                updatePreview()
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }
        milk.onItemSelectedListener = preferenceSelected
        flavour.onItemSelectedListener = preferenceSelected
        sweetness.onItemSelectedListener = preferenceSelected
        updatePreview()
        val applyButton = actionButton(getString(R.string.ai_builder_apply)) {
                val recommendedDrink = DrinkRecommendation.recommend(
                    DrinkRecommendation.Preferences(
                        milk = milk.selectedItem.toString(),
                        flavour = flavour.selectedItem.toString(),
                        sweetness = sweetness.selectedItem.toString(),
                    ),
                )
                statusText.text = getString(R.string.ai_builder_saved, recommendedDrink)
                sheet.dismiss()
                showTab(Tab.MENU)
        }
        form.addView(applyButton, spacedParams(top = 10, bottom = 8))
        form.addView(body(getString(R.string.ai_builder_disclosure)))

        sheet.setContentView(form)
        sheet.show()
    }

    private fun basketView(): View = verticalScroll {
        addView(title(getString(R.string.basket_heading), 28f))
        if (basket.isEmpty()) {
            addView(body(getString(R.string.basket_empty)))
            addView(
                actionButton(getString(R.string.add_items)) { showTab(Tab.MENU) },
                spacedParams(top = 12, bottom = 6),
            )
            return@verticalScroll
        }
        basket.toList().forEachIndexed { index, line ->
            addView(basketLineCard(index, line))
        }
        addView(sectionTitle(getString(R.string.basket_total_heading)))
        addView(title(PriceFormatter.format(basketTotal()), 24f))
        addView(
            actionButton(getString(R.string.checkout_preview_button)) {
                checkForRecentDuplicate()
            },
            spacedParams(top = 12, bottom = 5),
        )
        addView(
            secondaryButton(getString(R.string.clear_basket)) {
                basket.clear()
                showTab(Tab.BASKET)
            },
            spacedParams(top = 5, bottom = 6),
        )
    }

    private fun basketLineCard(index: Int, line: BasketLine): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = roundedBackground("#FFFFFF", 18)
            elevation = dp(1).toFloat()
        }
        row.addView(ImageView(this).apply {
            setImageResource(line.item.imageResId)
            contentDescription = getString(R.string.product_image_description, line.item.name)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }, LinearLayout.LayoutParams(dp(64), dp(64)).apply { marginEnd = dp(10) })
        val details = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        details.addView(title(line.item.name, 18f))
        details.addView(body(line.customisation.take(80)))
        details.addView(body(getString(R.string.product_nutrition_short, line.item.calories)))
        val controls = horizontalRow().apply {
            gravity = Gravity.END
            setPadding(0, dp(6), 0, 0)
        }
        controls.addView(compactControlButton("-") {
            if (line.quantity > 1) line.quantity-- else basket.removeAt(index)
            showTab(Tab.BASKET)
        }, LinearLayout.LayoutParams(dp(40), dp(36)).apply { marginEnd = dp(8) })
        controls.addView(body(getString(R.string.product_quantity, line.quantity)).apply {
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 0)
        }, LinearLayout.LayoutParams(dp(28), dp(36)).apply { marginEnd = dp(8) })
        controls.addView(compactControlButton("+") {
            line.quantity++
            showTab(Tab.BASKET)
        }, LinearLayout.LayoutParams(dp(40), dp(36)))
        details.addView(controls)
        row.addView(details, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        row.layoutParams = spacedParams(top = 7, bottom = 7)
        return row
    }


    private fun profileView(): View = verticalScroll {
        addView(title(getString(R.string.profile_heading), 28f))
        addView(body(getString(R.string.profile_email, userRepository.currentUserEmail ?: "Demo account")))
        addView(
            secondaryButton(getString(R.string.edit_profile)) { editDisplayName() },
            LinearLayout.LayoutParams(dp(220), dp(44)).apply {
                gravity = Gravity.START
                topMargin = dp(4)
                bottomMargin = dp(6)
            },
        )
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
        card.layoutParams = spacedParams(top = 8, bottom = 8)
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
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.order_detail_title, orderDisplayReference(order)))
            .setView(details)
            .setPositiveButton(R.string.close, null)
        if (order.status == "Collected" && !isDemoMode) {
            dialog.setNeutralButton(R.string.leave_review) { _, _ -> showReviewDialog(order) }
        }
        dialog.show()
    }

    private fun showReviewDialog(order: OrderRepository.OrderSummary) {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), 0, dp(20), 0)
        }
        val rating = Spinner(this).apply {
            adapter = ArrayAdapter(this@CustomerActivity, android.R.layout.simple_spinner_item, listOf(1, 2, 3, 4, 5))
        }
        val comment = android.widget.EditText(this).apply {
            hint = getString(R.string.review_comment_hint)
            minLines = 3
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        form.addView(body(getString(R.string.review_rating_label)))
        form.addView(rating)
        form.addView(comment)
        AlertDialog.Builder(this)
            .setTitle(R.string.leave_review)
            .setView(form)
            .setNegativeButton(R.string.cancel_order, null)
            .setPositiveButton(R.string.submit_review) { _, _ ->
                lifecycleScope.launch {
                    runCatching {
                        feedbackRepository.submit(order.id, rating.selectedItem as Int, comment.text.toString())
                    }.onSuccess {
                        statusText.text = getString(R.string.review_submitted)
                    }.onFailure { error ->
                        statusText.text = error.message ?: getString(R.string.review_failed)
                    }
                }
            }
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
                val updatedName = input.text.toString().trim()
                lifecycleScope.launch {
                    runCatching {
                        if (!isDemoMode) userRepository.updateDisplayName(updatedName)
                        displayName = updatedName
                    }.onSuccess {
                        statusText.text = getString(R.string.profile_saved)
                        showTab(Tab.PROFILE)
                    }.onFailure { error ->
                        statusText.text = error.message ?: getString(R.string.profile_save_failed)
                    }
                }
            }
            .show()
    }

    private fun showStageNotification(order: OrderRepository.OrderSummary) {
        val message = when (order.status) {
            "Ready for Collection" -> getString(R.string.notification_ready)
            "Preparing" -> getString(R.string.notification_preparing)
            "Collected" -> getString(R.string.notification_collected)
            else -> getString(R.string.notification_pending)
        }
        statusText.text = message
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "order_updates",
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        runCatching {
            NotificationManagerCompat.from(this).notify(
                order.id.hashCode(),
                NotificationCompat.Builder(this, "order_updates")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .build(),
            )
        }
    }

    override fun onDestroy() {
        orderListener?.remove()
        super.onDestroy()
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
                setPadding(dp(16), dp(16), dp(16), dp(32))
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
        setPadding(0, dp(10), 0, dp(8))
        setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    private fun sectionTitle(value: String) = title(value, 21f).apply {
        layoutParams = spacedParams(top = 18, bottom = 5)
    }

    private fun body(value: String) = TextView(this).apply {
        text = value
        textSize = 15f
        setTextColor(Color.parseColor("#6B6673"))
        setPadding(0, dp(4), 0, dp(8))
    }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = roundedBackground("#6A35C9", 28)
        minHeight = dp(44)
        minimumHeight = dp(44)
        setPadding(dp(16), 0, dp(16), 0)
        stateListAnimator = null
        setOnClickListener { action() }
        contentDescription = label
    }

    private fun secondaryButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setTextColor(Color.parseColor("#6A35C9"))
        background = roundedBackground("#F0E9FF", 18)
        minHeight = dp(40)
        minimumHeight = dp(40)
        setPadding(dp(12), 0, dp(12), 0)
        stateListAnimator = null
        setOnClickListener { action() }
        contentDescription = label
    }

    private fun compactControlButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        setTextColor(Color.parseColor("#6A35C9"))
        background = roundedBackground("#F0E9FF", 12)
        minHeight = 0
        minimumHeight = 0
        setPadding(0, 0, 0, 0)
        stateListAnimator = null
        setOnClickListener { action() }
        contentDescription = label
    }

    private fun spacedParams(top: Int = 0, bottom: Int = 0) =
        LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        }

    private fun roundedBackground(color: String, radius: Int) =
        GradientDrawable().apply {
            setColor(Color.parseColor(color))
            cornerRadius = dp(radius).toFloat()
        }

    private fun basketTotal(): Double = BasketCalculator.total(basket)

    private fun orderProgress(status: String): String = when (status) {
        "Pending" -> "1/4"
        "Preparing" -> "2/4"
        "Ready for Collection" -> "3/4"
        "Collected" -> "4/4"
        else -> "0/4"
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
