package com.snapmechanic.app.repository

import com.snapmechanic.app.model.Garage
import com.snapmechanic.app.network.ApiClient
import com.snapmechanic.app.utils.Constants
import com.snapmechanic.app.utils.Result
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * GarageRepository — the ONLY place in the app that talks to the Garage API.
 *
 * Why a repository? Instead of writing Retrofit code inside each Activity,
 * we put it here. Any Activity that needs garage data calls this class.
 * This keeps Activities clean and makes the code easy to test.
 *
 * Pattern used: Callback-based (no coroutines)
 *   enqueue() → runs the API call on a background thread
 *   onResponse / onFailure → called back on the main thread with the result
 */
object GarageRepository {

    /**
     * Fetch a page of garages from mockapi.io
     *
     * @param page     — page number (1, 2, 3…)
     * @param onResult — called when done: Result.Success(list) or Result.Error(message)
     */
    fun getGarages(page: Int, onResult: (Result<List<Garage>>) -> Unit) {
        // Tell the caller that loading has started
        onResult(Result.Loading)

        ApiClient.garageApi.getGarages(
            page = page,
            limit = Constants.PAGE_SIZE
        ).enqueue(object : Callback<List<Garage>> {

            // Called when API responded (even if response code is 4xx/5xx)
            override fun onResponse(call: Call<List<Garage>>, response: Response<List<Garage>>) {
                if (response.isSuccessful) {
                    val garages = response.body() ?: emptyList()
                    onResult(Result.Success(garages))
                } else {
                    onResult(Result.Error("Failed to load garages (${response.code()})"))
                }
            }

            // Called when network error (no internet, timeout, etc.)
            override fun onFailure(call: Call<List<Garage>>, t: Throwable) {
                onResult(Result.Error(t.message ?: "Network error. Check your connection."))
            }
        })
    }

    /**
     * Fetch a single garage by its ID
     *
     * @param id       — garage ID from mockapi.io
     * @param onResult — called with Result.Success(garage) or Result.Error(message)
     */
    fun getGarageById(id: String, onResult: (Result<Garage>) -> Unit) {
        onResult(Result.Loading)

        ApiClient.garageApi.getGarageById(id).enqueue(object : Callback<Garage> {

            override fun onResponse(call: Call<Garage>, response: Response<Garage>) {
                if (response.isSuccessful) {
                    val garage = response.body()
                    if (garage != null) {
                        onResult(Result.Success(garage))
                    } else {
                        onResult(Result.Error("Garage not found"))
                    }
                } else {
                    onResult(Result.Error("Failed to load garage (${response.code()})"))
                }
            }

            override fun onFailure(call: Call<Garage>, t: Throwable) {
                onResult(Result.Error(t.message ?: "Network error. Check your connection."))
            }
        })
    }
}
