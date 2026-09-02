package com.snapmechanic.app.model

import com.google.gson.annotations.SerializedName

/**
 * Garage data class — maps directly to JSON from mockapi.io
 * Each field uses @SerializedName to match the exact JSON key
 */
data class Garage(
    @SerializedName("id")           val id: String = "",
    @SerializedName("name")         val name: String = "",
    @SerializedName("imageUrl")     val imageUrl: String = "",
    @SerializedName("rating")       val rating: Double = 0.0,
    @SerializedName("distanceKm")   val distanceKm: Double = 0.0,
    @SerializedName("address")      val address: String = "",
    @SerializedName("phone")        val phone: String = "",
    @SerializedName("isOpen")       val isOpen: Boolean = false,
    @SerializedName("openTime")     val openTime: String = "",
    @SerializedName("closeTime")    val closeTime: String = "",
    @SerializedName("services")     val services: List<String> = emptyList(),
    @SerializedName("latitude")     val latitude: Double = 0.0,
    @SerializedName("longitude")    val longitude: Double = 0.0
)
