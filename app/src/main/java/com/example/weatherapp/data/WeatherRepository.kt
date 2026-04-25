package com.example.weatherapp.data

import com.example.weatherapp.data.remote.CityLocation
import com.example.weatherapp.data.remote.RetrofitClient
import com.example.weatherapp.data.remote.WeatherResponse

class WeatherRepository {
    private val weatherApi = RetrofitClient.weatherApi
    private val geocodingApi = RetrofitClient.geocodingApi
    
    private val apiKey = "FtIk47ZPladzZY9OmKiKMuZE9DtAYJI9mtZ16n8M"

    suspend fun getCoordinates(city: String): CityLocation? {
        val response = geocodingApi.getCoordinates(city, apiKey)
        return response.firstOrNull()
    }

    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return weatherApi.getForecast(lat, lon)
    }
}
