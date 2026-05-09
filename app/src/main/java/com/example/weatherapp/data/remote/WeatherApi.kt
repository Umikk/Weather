package com.example.weatherapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true,
        @Query("hourly") hourly: String = "temperature_2m,weathercode",
        @Query("daily") daily: String = "weathercode,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}

interface GeocodingApi {
    @GET("v1/geocoding")
    suspend fun getCoordinates(
        @Query("city") city: String,
        @Header("X-Api-Key") apiKey: String
    ): List<CityLocation>
}
