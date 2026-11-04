package com.firdgit.flocafe

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var roleText: TextView
    private lateinit var statusText: TextView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var displayNameInput: EditText
    private lateinit var landingText: TextView
    private lateinit var userRepository: UserRepository
    private lateinit var orderRepository: OrderRepository
    private lateinit var menuContainer: LinearLayout
    private lateinit var basketText: TextView
    private val basket = mutableMapOf<String, BasketLine>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        roleText = findViewById(R.id.roleText)
        statusText = findViewById(R.id.statusText)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        displayNameInput = findViewById(R.id.displayNameInput)
        landingText = findViewById(R.id.landingText)
        menuContainer = findViewById(R.id.menuContainer)
        basketText = findViewById(R.id.basketText)
        userRepository = UserRepository(this)
        orderRepository = OrderRepository(userRepository)

        val firebaseConfigured = userRepository.isFirebaseConfigured
        if (!firebaseConfigured) {
            roleText.text = getString(R.string.firebase_configuration_required)
            statusText.text = getString(R.string.firebase_setup_hint)
            setAuthenticationEnabled(false)
            return
        }

        findViewById<Button>(R.id.signInButton).setOnClickListener {
            authenticate { userRepository.signIn(emailInput.text.toString(), passwordInput.text.toString()) }
        }
        findViewById<Button>(R.id.registerButton).setOnClickListener {
            authenticate {
                userRepository.registerCustomer(
                    emailInput.text.toString(),
                    passwordInput.text.toString(),
                    displayNameInput.text.toString(),
                )
            }
        }
        roleText.text = getString(R.string.role_loading)
        lifecycleScope.launch {
            when (userRepository.currentUserRole()) {
                UserRole.ADMIN -> showAuthenticatedState(UserRole.ADMIN)
                UserRole.CUSTOMER -> showAuthenticatedState(UserRole.CUSTOMER)
                null -> showSignedOutState()
            }
        }
    }

    private fun authenticate(action: suspend () -> UserRole) {
        statusText.text = getString(R.string.authentication_in_progress)
        lifecycleScope.launch {
            runCatching { action() }
                .onSuccess { role ->
                    showAuthenticatedState(role)
                }
                .onFailure { error ->
                    statusText.text = error.message ?: getString(R.string.authentication_failed)
                }
        }
    }

    private fun showAuthenticatedState(role: UserRole) {
        roleText.text = if (role == UserRole.ADMIN) {
            getString(R.string.role_admin)
        } else {
            getString(R.string.role_customer)
        }
        landingText.text = if (role == UserRole.ADMIN) {
            getString(R.string.admin_landing_message)
        } else {
            getString(R.string.customer_landing_message)
        }
        statusText.text = getString(
            R.string.authentication_success_for_user,
            userRepository.currentUserEmail.orEmpty(),
        )
        setAuthenticationEnabled(false)
        if (role == UserRole.CUSTOMER) {
            showCustomerMenu()
        } else {
            menuContainer.removeAllViews()
            menuContainer.visibility = LinearLayout.GONE
            basketText.visibility = TextView.GONE
            findViewById<Button>(R.id.checkoutButton).visibility = Button.GONE
        }
        findViewById<Button>(R.id.logoutButton).apply {
            isEnabled = true
            setOnClickListener {
                userRepository.signOut()
                showSignedOutState()
            }
        }
    }

    private fun showSignedOutState() {
        roleText.text = getString(R.string.authentication_required)
        landingText.text = getString(R.string.signed_out_landing_message)
        statusText.text = getString(R.string.signed_out_message)
        setAuthenticationEnabled(true)
        findViewById<Button>(R.id.logoutButton).isEnabled = false
        menuContainer.removeAllViews()
        menuContainer.visibility = LinearLayout.GONE
        basketText.visibility = TextView.GONE
        findViewById<Button>(R.id.checkoutButton).visibility = Button.GONE
    }

    private fun showCustomerMenu() {
        menuContainer.visibility = LinearLayout.VISIBLE
        basketText.visibility = TextView.VISIBLE
        findViewById<Button>(R.id.checkoutButton).apply {
            visibility = Button.VISIBLE
            setOnClickListener {
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
                        }
                        .onFailure { error ->
                            statusText.text = error.message ?: getString(R.string.order_failed)
                        }
                }
            }
        }
        menuContainer.removeAllViews()
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

    private fun updateBasketText() {
        basketText.text = if (basket.isEmpty()) {
            getString(R.string.basket_empty)
        } else {
            getString(R.string.basket_summary, basket.values.sumOf { it.quantity }, basketTotal())
        }
    }

    private fun basketTotal(): String = "%.2f".format(basket.values.sumOf { it.item.price * it.quantity })

    private fun setAuthenticationEnabled(enabled: Boolean) {
        emailInput.isEnabled = enabled
        passwordInput.isEnabled = enabled
        displayNameInput.isEnabled = enabled
        findViewById<Button>(R.id.signInButton).isEnabled = enabled
        findViewById<Button>(R.id.registerButton).isEnabled = enabled
    }
}
