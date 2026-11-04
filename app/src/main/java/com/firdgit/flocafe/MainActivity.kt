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

        if (!userRepository.isFirebaseConfigured) {
            roleText.text = getString(R.string.firebase_configuration_required)
            statusText.text = getString(R.string.firebase_setup_hint)
            setAuthenticationEnabled(false)
            return
        }

        findViewById<Button>(R.id.signInButton).setOnClickListener {
            authenticate {
                userRepository.signIn(emailInput.text.toString(), passwordInput.text.toString())
            }
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
                statusText.text = getString(R.string.signed_out_message)
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
        findViewById<Button>(R.id.signInButton).isEnabled = enabled
        findViewById<Button>(R.id.registerButton).isEnabled = enabled
    }
}
