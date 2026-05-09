package com.example.weatherapp.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.data.WeatherRepository
import com.example.weatherapp.data.remote.WeatherResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

import com.example.weatherapp.R

data class CityWeather(
    val cityName: String,
    val weather: WeatherResponse? = null,
    val isLoading: Boolean = false,
    val errorRes: Int? = null,
    val isMajorCity: Boolean = false
)

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    private val _citiesWeather = MutableStateFlow<List<CityWeather>>(emptyList())
    val citiesWeather: StateFlow<List<CityWeather>> = _citiesWeather.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll(initialCities: List<String> = emptyList()) {
        if (_citiesWeather.value.isEmpty() && initialCities.isNotEmpty()) {
            initialCities.forEach { addCity(it, isMajor = true) }
        } else {
            _citiesWeather.value.forEach { loadWeatherForCity(it.cityName) }
        }
    }

    fun addCity(cityName: String, isMajor: Boolean = false) {
        val trimmedName = cityName.trim()
        if (trimmedName.isBlank()) return
        if (_citiesWeather.value.any { it.cityName.equals(trimmedName, ignoreCase = true) }) return
        
        val newCity = CityWeather(cityName = trimmedName, isLoading = true, isMajorCity = isMajor)
        _citiesWeather.value += newCity
        loadWeatherForCity(trimmedName)
    }

    private fun loadWeatherForCity(cityName: String) {
        viewModelScope.launch {
            updateCityStatus(cityName, isLoading = true)
            try {
                val location = repository.getCoordinates(cityName)
                if (location != null) {
                    val weather = repository.getWeather(location.latitude, location.longitude)
                    updateCityWeather(cityName, weather, null)
                } else {
                    updateCityWeather(cityName, null, R.string.city_not_found)
                }
            } catch (e: IOException) {
                updateCityWeather(cityName, null, R.string.network_error)
            } catch (e: Exception) {
                updateCityWeather(cityName, null, R.string.weather_data_error)
            }
        }
    }

    private fun updateCityStatus(cityName: String, isLoading: Boolean) {
        _citiesWeather.value = _citiesWeather.value.map {
            if (it.cityName == cityName) it.copy(isLoading = isLoading) else it
        }
    }

    private fun updateCityWeather(cityName: String, weather: WeatherResponse?, errorRes: Int?) {
        _citiesWeather.value = _citiesWeather.value.map {
            if (it.cityName == cityName) {
                it.copy(weather = weather, isLoading = false, errorRes = errorRes)
            } else {
                it
            }
        }
    }

    fun removeCity(cityName: String) {
        _citiesWeather.value = _citiesWeather.value.filter { it.cityName != cityName }
    }
}
