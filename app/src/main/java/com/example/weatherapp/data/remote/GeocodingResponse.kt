package com.example.weatherapp.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CityLocation(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val state: String? = null
)
