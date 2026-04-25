package com.example.weatherapp.data

import com.example.weatherapp.R

fun getWeatherDescriptionRes(code: Int): Int {
    return when (code) {
        0 -> R.string.weather_clear
        1, 2, 3 -> R.string.weather_mainly_clear
        45, 48 -> R.string.weather_fog
        51, 53, 55 -> R.string.weather_drizzle
        61, 63, 65 -> R.string.weather_rain
        71, 73, 75 -> R.string.weather_snow
        77 -> R.string.weather_snow_grains
        80, 81, 82 -> R.string.weather_rain_showers
        85, 86 -> R.string.weather_snow_showers
        95 -> R.string.weather_thunderstorm
        96, 99 -> R.string.weather_thunderstorm
        else -> R.string.weather_unknown
    }
}
