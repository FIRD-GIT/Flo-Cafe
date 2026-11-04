package com.firdgit.flocafe

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
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var displayNameInput: EditText
    private lateinit var landingText: TextView
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        roleText = findViewById(R.id.roleText)
        statusText = findViewById(R.id.statusText)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        displayNameInput = findViewById(R.id.displayNameInput)
        landingText = findViewById(R.id.landingText)
        userRepository = UserRepository(this)

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
    }

    private fun setAuthenticationEnabled(enabled: Boolean) {
        emailInput.isEnabled = enabled
        passwordInput.isEnabled = enabled
        displayNameInput.isEnabled = enabled
        findViewById<Button>(R.id.signInButton).isEnabled = enabled
        findViewById<Button>(R.id.registerButton).isEnabled = enabled
    }
}
