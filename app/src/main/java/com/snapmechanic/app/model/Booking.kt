package com.snapmechanic.app.model

/**
 * Booking data class — stored in Firestore under "bookings" collection
 * Each booking is linked to a user by userId (Firebase UID)
 */
data class Booking(
    val id: String = "",
    val userId: String = "",
    val garageId: String = "",
    val garageName: String = "",
    val garagePhone: String = "",
    val services: List<String> = emptyList(),
    val date: String = "",
    val time: String = "",
    val issue: String = "",
    val carMake: String = "",
    val carModel: String = "",
    val carRegNumber: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val createdAt: String = ""
)
