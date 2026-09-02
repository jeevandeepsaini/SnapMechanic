package com.snapmechanic.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.snapmechanic.app.databinding.ActivityChangePasswordBinding
import com.snapmechanic.app.repository.AuthRepository
import com.snapmechanic.app.utils.Result
import com.snapmechanic.app.utils.gone
import com.snapmechanic.app.utils.hideKeyboard
import com.snapmechanic.app.utils.toast
import com.snapmechanic.app.utils.visible

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnUpdatePassword.setOnClickListener {
            hideKeyboard()
            validateAndUpdate()
        }
    }

    private fun validateAndUpdate() {
        val old = binding.etOldPassword.text.toString().trim()
        val new = binding.etNewPassword.text.toString().trim()
        val confirm = binding.etConfirmPassword.text.toString().trim()

        if (old.isEmpty()) { binding.tilOldPassword.error = "Enter current password"; return }
        if (new.length < 6) { binding.tilNewPassword.error = "Min 6 characters"; return }
        if (new != confirm) { binding.tilConfirmPassword.error = "Passwords don't match"; return }

        binding.tilOldPassword.error = null
        binding.tilNewPassword.error = null
        binding.tilConfirmPassword.error = null

        AuthRepository.changePassword(old, new) { result ->
            when (result) {
                is Result.Loading -> { binding.progressBar.visible(); binding.btnUpdatePassword.isEnabled = false }
                is Result.Success -> {
                    binding.progressBar.gone(); binding.btnUpdatePassword.isEnabled = true
                    toast("Password updated successfully!")
                    finish()
                }
                is Result.Error -> {
                    binding.progressBar.gone(); binding.btnUpdatePassword.isEnabled = true
                    toast(result.message)
                }
            }
        }
    }
}
