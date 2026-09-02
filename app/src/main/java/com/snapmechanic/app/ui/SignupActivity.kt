package com.snapmechanic.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.snapmechanic.app.databinding.ActivitySignupBinding
import com.snapmechanic.app.model.User
import com.snapmechanic.app.repository.AuthRepository
import com.snapmechanic.app.repository.UserRepository
import com.snapmechanic.app.utils.Result
import com.snapmechanic.app.utils.gone
import com.snapmechanic.app.utils.hideKeyboard
import com.snapmechanic.app.utils.toast
import com.snapmechanic.app.utils.visible

/**
 * SignupActivity — creates a new Firebase user + saves their profile to Firestore.
 *
 * Two-step process:
 * Step 1: AuthRepository.signUp() → creates Firebase Auth account
 * Step 2: UserRepository.createUserProfile() → saves extra data (car info, phone, etc.) to Firestore
 *
 * If Step 2 fails, the Firebase account was still created (user can still log in),
 * but they'll need to update their profile later.
 */
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.tvLogin.setOnClickListener { finish() }

        binding.btnSignup.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener
            hideKeyboard()
            performSignup()
        }

        setupDropdowns()
    }

    private fun setupDropdowns() {
        val makes = com.snapmechanic.app.utils.Constants.CAR_DATA.keys.toList()
        val makeAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, makes)
        binding.etCarMake.setAdapter(makeAdapter)

        binding.etCarMake.setOnItemClickListener { _, _, position, _ ->
            val selectedMake = makes[position]
            val models = com.snapmechanic.app.utils.Constants.CAR_DATA[selectedMake] ?: emptyList()
            val modelAdapter = android.widget.ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, models)
            binding.etCarModel.setAdapter(modelAdapter)
            binding.etCarModel.text.clear()
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val carMake = binding.etCarMake.text.toString().trim()
        val carModel = binding.etCarModel.text.toString().trim()
        val carReg = binding.etCarReg.text.toString().trim()

        if (name.isEmpty()) { binding.tilName.error = "Name required"; return false }
        if (phone.isEmpty()) { binding.tilPhone.error = "Phone required"; return false }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Valid email required"; return false
        }
        if (password.length < 6) { binding.tilPassword.error = "Min 6 characters"; return false }
        if (password != confirmPassword) { binding.tilConfirmPassword.error = "Passwords don't match"; return false }
        if (carMake.isEmpty()) { binding.tilCarMake.error = "Car make required"; return false }
        if (carModel.isEmpty()) { binding.tilCarModel.error = "Car model required"; return false }
        if (carReg.isEmpty()) { binding.tilCarReg.error = "Registration required"; return false }

        // Clear all errors
        listOf(binding.tilName, binding.tilPhone, binding.tilEmail, binding.tilPassword,
            binding.tilConfirmPassword, binding.tilCarMake, binding.tilCarModel, binding.tilCarReg
        ).forEach { it.error = null }

        return true
    }

    private fun performSignup() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Step 1: Create Firebase Auth account
        AuthRepository.signUp(email, password) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visible()
                    binding.btnSignup.isEnabled = false
                }
                is Result.Success -> {
                    // Firebase account created! Now save the extra profile data to Firestore
                    saveUserProfile(email)
                }
                is Result.Error -> {
                    binding.progressBar.gone()
                    binding.btnSignup.isEnabled = true
                    toast(result.message)
                }
            }
        }
    }

    private fun saveUserProfile(email: String) {
        val user = User(
            name = binding.etName.text.toString().trim(),
            phone = binding.etPhone.text.toString().trim(),
            email = email,
            carMake = binding.etCarMake.text.toString().trim(),
            carModel = binding.etCarModel.text.toString().trim(),
            carRegNumber = binding.etCarReg.text.toString().trim()
        )

        // Step 2: Save profile to Firestore
        UserRepository.createUserProfile(user) { result ->
            binding.progressBar.gone()
            binding.btnSignup.isEnabled = true
            when (result) {
                is Result.Success -> {
                    toast("Account created successfully! 🎉")
                    // Go to Home
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                }
                is Result.Error -> {
                    // Account was created but profile save failed — still let them in
                    toast("Account created. Profile setup pending.")
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                }
                is Result.Loading -> {}
            }
        }
    }
}
