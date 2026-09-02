package com.snapmechanic.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.snapmechanic.app.databinding.ActivitySplashBinding
import com.snapmechanic.app.repository.AuthRepository

/**
 * SplashActivity — First screen the user sees.
 *
 * What it does:
 * 1. Shows the app logo and name for 2 seconds
 * 2. Checks if a user is already logged in (Firebase remembers login sessions)
 * 3. If logged in → go to HomeActivity (skip login)
 * 4. If not logged in → go to LoginActivity
 *
 * Handler(Looper.getMainLooper()).postDelayed() — runs code after a delay
 * on the main UI thread (safe to update UI from here)
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wait 2 seconds, then decide where to go
        Handler(Looper.getMainLooper()).postDelayed({
            navigateNext()
        }, 2000)
    }

    private fun navigateNext() {
        val intent = if (AuthRepository.isLoggedIn()) {
            // User is already logged in — take them to Home
            Intent(this, HomeActivity::class.java)
        } else {
            // No user logged in — show Login screen
            Intent(this, LoginActivity::class.java)
        }

        // Start the next screen
        startActivity(intent)
        // Close SplashActivity so pressing Back doesn't return to it
        finish()
    }
}
