package com.snapmechanic.app.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.snapmechanic.app.utils.Result

/**
 * AuthRepository — handles all Firebase Authentication operations.
 *
 * Firebase Auth is Google's service that manages:
 *   - Creating accounts (email + password)
 *   - Signing in
 *   - Signing out
 *   - Password reset emails
 *   - Re-authentication (needed before deleting account)
 *
 * We wrap every Firebase call in our Result class so Activities
 * get a consistent Success/Error/Loading response.
 */
object AuthRepository {

    // FirebaseAuth instance — the main Firebase Auth object
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Get the currently logged-in user (null if not logged in)
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Check if any user is logged in
    fun isLoggedIn(): Boolean = auth.currentUser != null

    /**
     * Sign up a new user with email and password
     * Firebase creates the account and signs them in automatically
     */
    fun signUp(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        onResult(Result.Loading)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    onResult(Result.Success(user))
                } else {
                    onResult(Result.Error("Signup failed. Please try again."))
                }
            }
            .addOnFailureListener { exception ->
                onResult(Result.Error(exception.message ?: "Signup failed."))
            }
    }

    /**
     * Sign in an existing user with email and password
     */
    fun login(email: String, password: String, onResult: (Result<FirebaseUser>) -> Unit) {
        onResult(Result.Loading)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    onResult(Result.Success(user))
                } else {
                    onResult(Result.Error("Login failed. Please try again."))
                }
            }
            .addOnFailureListener { exception ->
                onResult(Result.Error(exception.message ?: "Login failed."))
            }
    }

    /**
     * Send a password reset email to the given address
     */
    fun sendPasswordReset(email: String, onResult: (Result<Boolean>) -> Unit) {
        onResult(Result.Loading)
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onResult(Result.Success(true))
            }
            .addOnFailureListener { exception ->
                onResult(Result.Error(exception.message ?: "Failed to send reset email."))
            }
    }

    /**
     * Change password — requires re-authentication first for security
     * Firebase requires the user to prove they know their current password
     * before allowing a password change.
     */
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Result<Boolean>) -> Unit
    ) {
        val user = auth.currentUser
        val email = user?.email

        if (user == null || email == null) {
            onResult(Result.Error("No user logged in."))
            return
        }

        onResult(Result.Loading)

        // Step 1: Re-authenticate with current credentials
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Step 2: Update to new password
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        onResult(Result.Success(true))
                    }
                    .addOnFailureListener { exception ->
                        onResult(Result.Error(exception.message ?: "Failed to update password."))
                    }
            }
            .addOnFailureListener { exception ->
                onResult(Result.Error("Current password is incorrect."))
            }
    }

    /**
     * Delete the user's Firebase account permanently
     * Also requires re-authentication first
     *
     * Note: Call UserRepository.deleteUserData() BEFORE calling this
     * to delete the Firestore documents first.
     */
    fun deleteAccount(currentPassword: String, onResult: (Result<Boolean>) -> Unit) {
        val user = auth.currentUser
        val email = user?.email

        if (user == null || email == null) {
            onResult(Result.Error("No user logged in."))
            return
        }

        onResult(Result.Loading)

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.delete()
                    .addOnSuccessListener {
                        onResult(Result.Success(true))
                    }
                    .addOnFailureListener { exception ->
                        onResult(Result.Error(exception.message ?: "Failed to delete account."))
                    }
            }
            .addOnFailureListener {
                onResult(Result.Error("Email does not match. Please try again."))
            }
    }

    /**
     * Sign out the current user
     */
    fun signOut() {
        auth.signOut()
    }
}
