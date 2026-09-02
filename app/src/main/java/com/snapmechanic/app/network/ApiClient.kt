package com.snapmechanic.app.network

import com.snapmechanic.app.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * ApiClient — creates and holds a single Retrofit instance (Singleton pattern).
 *
 * Why singleton? We only need ONE Retrofit instance for the whole app.
 * Creating multiple instances wastes memory.
 *
 * The `by lazy` means the instance is created the first time it's needed,
 * and reused every time after that.
 */
object ApiClient {

    // OkHttpClient — handles the actual HTTP connection
    // We add a LoggingInterceptor so we can see API requests in Logcat (debug only)
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Shows full request + response
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)  // Max time to connect
            .readTimeout(30, TimeUnit.SECONDS)     // Max time to read response
            .build()
    }

    // Retrofit instance — built with our base URL and Gson converter
    // Gson converter automatically converts JSON → Kotlin data classes
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // The actual API interface we'll call in our repository
    val garageApi: GarageApi by lazy {
        retrofit.create(GarageApi::class.java)
    }
}
