package com.snapmechanic.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.snapmechanic.app.databinding.ActivityLoginBinding
import com.snapmechanic.app.repository.AuthRepository
import com.snapmechanic.app.utils.Result
import com.snapmechanic.app.utils.gone
import com.snapmechanic.app.utils.hideKeyboard
import com.snapmechanic.app.utils.toast
import com.snapmechanic.app.utils.visible

/**
 * LoginActivity — handles email/password login and forgot password.
 *
 * Flow:
 *   User enters email + password → tap Login
 *   → AuthRepository.login() calls Firebase signInWithEmailAndPassword()
 *   → Result.Success → go to HomeActivity
 *   → Result.Error → show error message
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // LOGIN button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validate inputs before calling API
            if (!validateInputs(email, password)) return@setOnClickListener

            hideKeyboard()
            performLogin(email, password)
        }

        // FORGOT PASSWORD link
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        // SIGNUP link
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email"
            return false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return false
        }
        // Clear errors if valid
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        return true
    }

    private fun performLogin(email: String, password: String) {
        AuthRepository.login(email, password) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visible()
                    binding.btnLogin.isEnabled = false
                }
                is Result.Success -> {
                    binding.progressBar.gone()
                    binding.btnLogin.isEnabled = true
                    // Login successful → go to Home
                    startActivity(Intent(this, HomeActivity::class.java))
                    // Clear the back stack — user can't go back to Login
                    finishAffinity()
                }
                is Result.Error -> {
                    binding.progressBar.gone()
                    binding.btnLogin.isEnabled = true
                    toast(result.message)
                }
            }
        }
    }

    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(com.snapmechanic.app.R.layout.dialog_forgot_password, null)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .show()
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val etEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.snapmechanic.app.R.id.etEmail)
        
        dialogView.findViewById<android.widget.Button>(com.snapmechanic.app.R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.widget.Button>(com.snapmechanic.app.R.id.btnSend).setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty()) {
                toast("Please enter your email")
                return@setOnClickListener
            }
            sendPasswordReset(email)
            dialog.dismiss()
        }
    }

    private fun sendPasswordReset(email: String) {
        AuthRepository.sendPasswordReset(email) { result ->
            when (result) {
                is Result.Success -> toast("Reset email sent! Check your inbox.")
                is Result.Error -> toast(result.message)
                is Result.Loading -> { /* handled by Firebase internally */ }
            }
        }
    }
}
