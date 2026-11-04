package com.firdgit.flocafe

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var roleText: TextView
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        roleText = findViewById(R.id.roleText)
        userRepository = UserRepository(this)

        roleText.text = if (userRepository.isFirebaseConfigured) {
            getString(R.string.role_customer)
        } else {
            getString(R.string.firebase_configuration_required)
        }
    }
}
