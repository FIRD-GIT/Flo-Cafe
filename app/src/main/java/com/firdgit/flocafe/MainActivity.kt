package com.firdgit.flocafe

import android.os.Bundle
import android.view.View
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
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        roleText = findViewById(R.id.roleText)
        statusText = findViewById(R.id.statusText)
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        displayNameInput = findViewById(R.id.displayNameInput)
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
            roleText.text = when (userRepository.currentUserRole()) {
                UserRole.ADMIN -> getString(R.string.role_admin)
                UserRole.CUSTOMER -> getString(R.string.role_customer)
                null -> getString(R.string.authentication_required)
            }
        }
    }

    private fun authenticate(action: suspend () -> UserRole) {
        statusText.text = getString(R.string.authentication_in_progress)
        lifecycleScope.launch {
            runCatching { action() }
                .onSuccess { role ->
                    roleText.text = if (role == UserRole.ADMIN) {
                        getString(R.string.role_admin)
                    } else {
                        getString(R.string.role_customer)
                    }
                    statusText.text = getString(R.string.authentication_success)
                }
                .onFailure { error ->
                    statusText.text = error.message ?: getString(R.string.authentication_failed)
                }
        }
    }

    private fun setAuthenticationEnabled(enabled: Boolean) {
        emailInput.isEnabled = enabled
        passwordInput.isEnabled = enabled
        displayNameInput.isEnabled = enabled
        findViewById<Button>(R.id.signInButton).isEnabled = enabled
        findViewById<Button>(R.id.registerButton).isEnabled = enabled
    }
}
