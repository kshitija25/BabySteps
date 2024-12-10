package com.example.babysteps.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BabyNamesApi {
    @GET("babynames")
    suspend fun getBabyNames(
        @Header("X-Api-Key") apiKey: String, // API Key Header
        @Query("gender") gender: String? = null,  // Optional: boy, girl, neutral
        @Query("popular_only") popularOnly: Boolean? = true // Default is true
    ): Response<List<String>>
}
