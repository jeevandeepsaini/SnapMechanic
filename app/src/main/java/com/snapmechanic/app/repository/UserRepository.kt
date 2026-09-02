package com.snapmechanic.app.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.snapmechanic.app.model.Booking
import com.snapmechanic.app.model.User
import com.snapmechanic.app.utils.Constants
import com.snapmechanic.app.utils.Result

/**
 * UserRepository — handles all Firestore database operations for:
 *   - User profile (read, create, update)
 *   - Bookings (create, list)
 *   - Account deletion (delete all user data)
 *
 * Firestore is Google's NoSQL cloud database.
 * Documents are organized in collections (like folders).
 * Each user's profile is a document in the "users" collection,
 * with the document ID equal to their Firebase UID.
 */
object UserRepository {

    // Firestore instance — the main database object
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Quick helper to get current user's UID (their unique ID in Firebase)
    private fun currentUid(): String? = FirebaseAuth.getInstance().currentUser?.uid

    // ─────────────────────────────────────────
    // USER PROFILE
    // ─────────────────────────────────────────

    /**
     * Save a new user profile to Firestore after signup.
     * Document ID = Firebase UID → so we can always find the user's data.
     */
    fun createUserProfile(user: User, onResult: (Result<Boolean>) -> Unit) {
        val uid = currentUid() ?: run {
            onResult(Result.Error("Not logged in.")); return
        }
        onResult(Result.Loading)

        // Convert User object to a Map (Firestore stores maps/dictionaries)
        val userMap = mapOf(
            "uid" to uid,
            "name" to user.name,
            "phone" to user.phone,
            "email" to user.email,
            "carMake" to user.carMake,
            "carModel" to user.carModel,
            "carRegNumber" to user.carRegNumber
        )

        db.collection(Constants.COLLECTION_USERS)
            .document(uid) // Document ID = user's UID
            .set(userMap)
            .addOnSuccessListener { onResult(Result.Success(true)) }
            .addOnFailureListener { e -> onResult(Result.Error(e.message ?: "Failed to save profile.")) }
    }

    /**
     * Fetch the current user's profile from Firestore
     */
    fun getUserProfile(onResult: (Result<User>) -> Unit) {
        val uid = currentUid() ?: run {
            onResult(Result.Error("Not logged in.")); return
        }
        onResult(Result.Loading)

        db.collection(Constants.COLLECTION_USERS)
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Convert Firestore document → User data class
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        onResult(Result.Success(user))
                    } else {
                        onResult(Result.Error("Failed to parse user data."))
                    }
                } else {
                    onResult(Result.Error("User profile not found."))
                }
            }
            .addOnFailureListener { e -> onResult(Result.Error(e.message ?: "Failed to fetch profile.")) }
    }

    /**
     * Update existing user profile fields
     */
    fun updateUserProfile(user: User, onResult: (Result<Boolean>) -> Unit) {
        val uid = currentUid() ?: run {
            onResult(Result.Error("Not logged in.")); return
        }
        onResult(Result.Loading)

        val updates = mapOf(
            "name" to user.name,
            "phone" to user.phone,
            "carMake" to user.carMake,
            "carModel" to user.carModel,
            "carRegNumber" to user.carRegNumber
        )

        db.collection(Constants.COLLECTION_USERS)
            .document(uid)
            .update(updates)
            .addOnSuccessListener { onResult(Result.Success(true)) }
            .addOnFailureListener { e -> onResult(Result.Error(e.message ?: "Failed to update profile.")) }
    }

    // ─────────────────────────────────────────
    // BOOKINGS
    // ─────────────────────────────────────────

    /**
     * Save a new booking to Firestore.
     * Firestore auto-generates a unique ID for each booking document.
     */
    fun createBooking(booking: Booking, onResult: (Result<Boolean>) -> Unit) {
        val uid = currentUid() ?: run {
            onResult(Result.Error("Not logged in.")); return
        }
        onResult(Result.Loading)

        val bookingMap = mapOf(
            "userId" to uid,
            "garageId" to booking.garageId,
            "garageName" to booking.garageName,
            "garagePhone" to booking.garagePhone,
            "services" to booking.services,
            "date" to booking.date,
            "time" to booking.time,
            "issue" to booking.issue,
            "carMake" to booking.carMake,
            "carModel" to booking.carModel,
            "carRegNumber" to booking.carRegNumber,
            "customerName" to booking.customerName,
            "customerPhone" to booking.customerPhone,
            "createdAt" to System.currentTimeMillis().toString()
        )

        // .add() auto-generates the document ID
        db.collection(Constants.COLLECTION_BOOKINGS)
            .add(bookingMap)
            .addOnSuccessListener { onResult(Result.Success(true)) }
            .addOnFailureListener { e -> onResult(Result.Error(e.message ?: "Failed to save booking.")) }
    }

    /**
     * Fetch all bookings for the currently logged-in user.
     * Filters by userId so each user only sees their own bookings.
     */
    fun getMyBookings(onResult: (Result<List<Booking>>) -> Unit) {
        val uid = currentUid() ?: run {
            onResult(Result.Error("Not logged in.")); return
        }
        onResult(Result.Loading)

        db.collection(Constants.COLLECTION_BOOKINGS)
            .whereEqualTo("userId", uid) // Only get documents where userId matches current user
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val bookings = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                }
                onResult(Result.Success(bookings))
            }
            .addOnFailureListener { e -> onResult(Result.Error(e.message ?: "Failed to load bookings.")) }
    }

    // ─────────────────────────────────────────
    // ACCOUNT DELETION
    // ─────────────────────────────────────────

    /**
     * Delete all of the user's data from Firestore before deleting their Firebase account.
     * We delete: user profile document + all booking documents.
     *
     * Called by: ProfileActivity → before AuthRepository.deleteAccount()
     */
    fun deleteUserData(onResult: (Result<Boolean>) -> Unit) {
        val uid = currentUid() ?: run {
            onResult(Result.Error("Not logged in.")); return
        }
        onResult(Result.Loading)

        // Step 1: Delete user profile document
        db.collection(Constants.COLLECTION_USERS).document(uid).delete()
            .addOnSuccessListener {
                // Step 2: Delete all bookings for this user
                db.collection(Constants.COLLECTION_BOOKINGS)
                    .whereEqualTo("userId", uid)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val batch = db.batch()
                        snapshot.documents.forEach { doc -> batch.delete(doc.reference) }
                        batch.commit()
                            .addOnSuccessListener { onResult(Result.Success(true)) }
                            .addOnFailureListener { e -> onResult(Result.Error(e.message ?: "Failed to delete bookings.")) }
                    }
                    .addOnFailureListener { e -> onResult(Result.Error(e.message ?: "Failed to find bookings.")) }
            }
            .addOnFailureListener { e -> onResult(Result.Error(e.message ?: "Failed to delete user profile.")) }
    }
}
