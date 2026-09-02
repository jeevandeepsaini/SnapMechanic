package com.snapmechanic.app.model

/**
 * User data class — stores user profile info in Firestore
 * Document path: users/{firebaseUid}
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val carMake: String = "",
    val carModel: String = "",
    val carRegNumber: String = ""
)
