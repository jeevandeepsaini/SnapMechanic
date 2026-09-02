package com.snapmechanic.app.utils

object Constants {
    // mockapi.io Base URL — all API calls start with this
    const val BASE_URL = "https://6a96f1f70e3240db906192f2.mockapi.io/garages/"

    // How many garages to load per page (pagination)
    const val PAGE_SIZE = 5

    // Firestore collection names
    const val COLLECTION_USERS = "users"
    const val COLLECTION_BOOKINGS = "bookings"

    // Intent keys — used when passing data between Activities
    const val EXTRA_GARAGE_ID = "extra_garage_id"
    const val EXTRA_GARAGE = "extra_garage"

    // Developer support email
    const val SUPPORT_EMAIL = "support@snapmechanic.app"

    // Indian car brands and their models
    val CAR_DATA = mapOf(
        "Maruti Suzuki" to listOf("Swift", "Baleno", "Alto", "Dzire", "Brezza", "Ertiga", "WagonR", "Grand Vitara"),
        "Hyundai" to listOf("Creta", "Venue", "i20", "Grand i10 Nios", "Verna", "Tucson", "Aura", "Alcazar"),
        "Tata" to listOf("Nexon", "Punch", "Harrier", "Safari", "Tiago", "Altroz", "Tigor"),
        "Mahindra" to listOf("Scorpio", "Scorpio-N", "XUV700", "Thar", "Bolero", "XUV300", "XUV400"),
        "Kia" to listOf("Seltos", "Sonet", "Carens", "EV6"),
        "Toyota" to listOf("Innova Crysta", "Innova Hycross", "Fortuner", "Glanza", "Urban Cruiser Hyryder"),
        "Honda" to listOf("City", "Amaze", "Elevate"),
        "MG" to listOf("Hector", "Hector Plus", "Astor", "Comet EV", "Gloster", "ZSEV"),
        "Skoda" to listOf("Slavia", "Kushaq", "Kodiaq"),
        "Volkswagen" to listOf("Virtus", "Taigun", "Tiguan")
    )
}
