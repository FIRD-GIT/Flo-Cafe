package com.firdgit.flocafe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var roleText: TextView
    private lateinit var statusText: TextView
    private lateinit var firebaseWarningText: TextView
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var displayNameInput: EditText
    private lateinit var userRepository: UserRepository
    private var registerMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        roleText = findViewById(R.id.roleText)
        statusText = findViewById(R.id.statusText)
        firebaseWarningText = findViewById(R.id.firebaseWarningText)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        displayNameInput = findViewById(R.id.displayNameInput)
        userRepository = UserRepository(this)

        if (!userRepository.isFirebaseConfigured) {
            roleText.text = getString(R.string.firebase_configuration_required)
            firebaseWarningText.text = getString(R.string.firebase_setup_hint)
            firebaseWarningText.visibility = android.view.View.VISIBLE
            setAuthenticationEnabled(false)
            findViewById<Button>(R.id.demoMenuButton).apply {
                visibility = android.view.View.VISIBLE
                setOnClickListener {
                    startActivity(
                        Intent(this@MainActivity, CustomerActivity::class.java)
                            .putExtra(CustomerActivity.EXTRA_DEMO_MODE, true),
                    )
                }
            }
            return
        }
        firebaseWarningText.visibility = android.view.View.GONE

        val primaryButton = findViewById<Button>(R.id.primaryAuthButton)
        findViewById<Button>(R.id.signInToggle).setOnClickListener {
            registerMode = false
            updateAuthMode(primaryButton)
        }
        findViewById<Button>(R.id.registerToggle).setOnClickListener {
            registerMode = true
            updateAuthMode(primaryButton)
        }
        primaryButton.setOnClickListener {
            authenticate {
                if (registerMode) {
                    userRepository.registerCustomer(
                        emailInput.text.toString(),
                        passwordInput.text.toString(),
                        displayNameInput.text.toString(),
                    )
                } else {
                    userRepository.signIn(emailInput.text.toString(), passwordInput.text.toString())
                }
            }
        }
        findViewById<TextView>(R.id.forgotPasswordButton).setOnClickListener {
            lifecycleScope.launch {
                runCatching { userRepository.sendPasswordReset(emailInput.text.toString()) }
                    .onSuccess { statusText.text = getString(R.string.password_reset_sent) }
                    .onFailure { error -> statusText.text = error.message ?: getString(R.string.authentication_failed) }
            }
        }

        roleText.text = getString(R.string.role_loading)
        lifecycleScope.launch {
            routeToRole(userRepository.currentUserRole())
        }
    }

    private fun authenticate(action: suspend () -> UserRole) {
        statusText.text = getString(R.string.authentication_in_progress)
        lifecycleScope.launch {
            runCatching { action() }
                .onSuccess { role -> routeToRole(role) }
                .onFailure { error ->
                    statusText.text = error.message ?: getString(R.string.authentication_failed)
                }
        }
    }

    private fun routeToRole(role: UserRole?) {
        when (role) {
            UserRole.ADMIN -> openRoleActivity(AdminActivity::class.java)
            UserRole.CUSTOMER -> openRoleActivity(CustomerActivity::class.java)
            null -> {
                roleText.text = getString(R.string.authentication_required)
                statusText.text = getString(R.string.welcome_message)
            }
        }
    }

    private fun openRoleActivity(activityClass: Class<out AppCompatActivity>) {
        startActivity(Intent(this, activityClass))
        finish()
    }

    private fun setAuthenticationEnabled(enabled: Boolean) {
        emailInput.isEnabled = enabled
        passwordInput.isEnabled = enabled
        displayNameInput.isEnabled = enabled
        findViewById<Button>(R.id.primaryAuthButton).isEnabled = enabled
        findViewById<Button>(R.id.signInToggle).isEnabled = enabled
        findViewById<Button>(R.id.registerToggle).isEnabled = enabled
    }

    private fun updateAuthMode(primaryButton: Button) {
        displayNameInput.visibility = if (registerMode) android.view.View.VISIBLE else android.view.View.GONE
        primaryButton.text = getString(if (registerMode) R.string.register else R.string.sign_in)
        primaryButton.background = getDrawable(
            if (registerMode) R.drawable.auth_outline_button else R.drawable.auth_primary_button,
        )
        primaryButton.setTextColor(
            getColor(if (registerMode) R.color.auth_purple else android.R.color.white),
        )
    }
}
