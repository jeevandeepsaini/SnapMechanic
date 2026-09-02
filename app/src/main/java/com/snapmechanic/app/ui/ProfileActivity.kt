package com.snapmechanic.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.snapmechanic.app.databinding.ActivityProfileBinding
import com.snapmechanic.app.repository.AuthRepository
import com.snapmechanic.app.repository.UserRepository
import com.snapmechanic.app.utils.Result
import com.snapmechanic.app.utils.gone
import com.snapmechanic.app.utils.toast
import com.snapmechanic.app.utils.visible

/**
 * ProfileActivity — shows user info and provides navigation to sub-screens.
 *
 * Menu items:
 *   Update Profile → UpdateProfileActivity
 *   My Bookings → MyBookingsActivity
 *   Change Password → ChangePasswordActivity
 *   Help & Support → HelpSupportActivity
 *   Logout → confirms → clears session → LoginActivity
 *   Delete Account → confirms → deletes Firestore data + Firebase account → LoginActivity
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMenuItems()
        loadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        // Reload profile when returning from UpdateProfileActivity
        loadUserProfile()
    }

    private fun loadUserProfile() {
        UserRepository.getUserProfile { result ->
            when (result) {
                is Result.Success -> {
                    binding.tvUserName.text = result.data.name.ifEmpty { "User" }
                    binding.tvUserEmail.text = FirebaseAuth.getInstance().currentUser?.email ?: ""
                }
                is Result.Error -> {
                    binding.tvUserName.text = FirebaseAuth.getInstance().currentUser?.email ?: "User"
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun setupMenuItems() {
        binding.menuUpdateProfile.setOnClickListener {
            startActivity(Intent(this, UpdateProfileActivity::class.java))
        }

        binding.menuMyBookings.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        binding.menuChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        binding.menuHelpSupport.setOnClickListener {
            startActivity(Intent(this, HelpSupportActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        binding.btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        val dialogView = layoutInflater.inflate(com.snapmechanic.app.R.layout.dialog_logout, null)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .show()
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<android.widget.Button>(com.snapmechanic.app.R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.widget.Button>(com.snapmechanic.app.R.id.btnLogout).setOnClickListener {
            AuthRepository.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            dialog.dismiss()
        }
    }

    private fun showDeleteAccountConfirmation() {
        val dialogView = layoutInflater.inflate(com.snapmechanic.app.R.layout.dialog_delete_account, null)
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .show()
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val etPassword = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(com.snapmechanic.app.R.id.etPassword)

        dialogView.findViewById<android.widget.Button>(com.snapmechanic.app.R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<android.widget.Button>(com.snapmechanic.app.R.id.btnDelete).setOnClickListener {
            val password = etPassword.text.toString().trim()
            if (password.isEmpty()) { 
                toast("Password required")
                return@setOnClickListener 
            }
            performDelete(password)
            dialog.dismiss()
        }
    }

    private fun performDelete(password: String) {
        // Step 1: Delete Firestore data (profile + bookings)
        UserRepository.deleteUserData { result ->
            when (result) {
                is Result.Loading -> { /* ongoing */ }
                is Result.Success -> {
                    // Step 2: Delete Firebase Auth account (requires re-auth)
                    AuthRepository.deleteAccount(password) { authResult ->
                        when (authResult) {
                            is Result.Success -> {
                                toast("Account deleted successfully.")
                                startActivity(Intent(this, LoginActivity::class.java))
                                finishAffinity()
                            }
                            is Result.Error -> toast("Failed to delete account: ${authResult.message}")
                            is Result.Loading -> {}
                        }
                    }
                }
                is Result.Error -> toast("Failed to delete data: ${result.message}")
            }
        }
    }
}
