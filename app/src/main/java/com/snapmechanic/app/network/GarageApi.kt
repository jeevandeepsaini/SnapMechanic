package com.snapmechanic.app.network

import com.snapmechanic.app.model.Garage
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * GarageApi — defines what API endpoints our app can call.
 *
 * Each function here maps to one REST API endpoint.
 * Retrofit reads these annotations and auto-generates the HTTP code.
 *
 * @GET("garages") → GET https://6a96f1f70e3240db906192f2.mockapi.io/garages/garages
 */
interface GarageApi {

    /**
     * Get a paginated list of garages
     * URL: GET /garages?page=1&limit=5
     *
     * @param page  — which page of results to fetch (starts at 1)
     * @param limit — how many results per page
     * Returns a Call<List<Garage>> — a network request that returns a list of Garage objects
     */
    @GET("garages")
    fun getGarages(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Call<List<Garage>>

    /**
     * Get details for a single garage by its ID
     * URL: GET /garages/{id}
     *
     * @param id — the garage's unique ID string (e.g. "1", "2")
     */
    @GET("garages/{id}")
    fun getGarageById(
        @Path("id") id: String
    ): Call<Garage>
}
