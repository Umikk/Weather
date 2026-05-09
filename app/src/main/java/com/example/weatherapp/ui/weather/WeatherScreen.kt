package com.example.weatherapp.ui.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.weatherapp.R
import com.example.weatherapp.data.getWeatherDescriptionRes
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

val SoftBlue = Color(0xFFE1F5FE)
val CardBlue = Color(0xFFB3E5FC)

@Composable
fun WeatherAppNavigation(viewModel: WeatherViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "city_list") {
        composable("city_list") {
            CityListScreen(viewModel) { cityName ->
                navController.navigate("weather_detail/$cityName")
            }
        }
        composable(
            "weather_detail/{cityName}",
            arguments = listOf(navArgument("cityName") { type = NavType.StringType })
        ) { backStackEntry ->
            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""
            WeatherDetailScreen(cityName, viewModel) {
                navController.popBackStack()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityListScreen(viewModel: WeatherViewModel, onCityClick: (String) -> Unit) {
    var cityName by remember { mutableStateOf("") }
    val citiesWeather by viewModel.citiesWeather.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val majorCities = context.resources.getStringArray(R.array.major_cities).toList()
        viewModel.refreshAll(majorCities)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.app_name), fontWeight = FontWeight.ExtraBold, color = Color(0xFF01579B)) },
                actions = {
                    IconButton(onClick = { 
                        val majorCities = context.resources.getStringArray(R.array.major_cities).toList()
                        viewModel.refreshAll(majorCities) 
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh), tint = Color(0xFF01579B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = SoftBlue
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = cityName,
                onValueChange = { cityName = it },
                placeholder = { Text(stringResource(R.string.search_city)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.addCity(cityName)
                        cityName = ""
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_city))
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF0288D1),
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFF01579B),
                    unfocusedTextColor = Color(0xFF01579B)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(citiesWeather) { cityWeather ->
                    CityCard(
                        cityWeather = cityWeather,
                        onClick = { onCityClick(cityWeather.cityName) },
                        onRemove = { viewModel.removeCity(cityWeather.cityName) }
                    )
                }
            }
        }
    }
}

@Composable
fun CityCard(cityWeather: CityWeather, onClick: () -> Unit, onRemove: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(CardBlue)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = cityWeather.cityName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF01579B),
                        fontWeight = FontWeight.Bold
                    )
                    if (cityWeather.isLoading) {
                        CircularProgressIndicator(color = Color(0xFF01579B), modifier = Modifier.size(20.dp))
                    } else if (cityWeather.errorRes != null) {
                        Text(text = stringResource(cityWeather.errorRes), color = Color.Red, fontSize = 12.sp)
                    } else if (cityWeather.weather != null) {
                        val current = cityWeather.weather.currentWeather
                        Text(
                            text = stringResource(getWeatherDescriptionRes(current?.weathercode ?: 0)),
                            color = Color(0xFF01579B).copy(alpha = 0.7f)
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (cityWeather.weather?.currentWeather != null) {
                        Text(
                            text = "${cityWeather.weather.currentWeather.temperature.toInt()}°",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color(0xFF01579B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    if (!cityWeather.isMajorCity) {
                        IconButton(onClick = onRemove) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFF01579B).copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDetailScreen(cityName: String, viewModel: WeatherViewModel, onBack: () -> Unit) {
    val citiesWeather by viewModel.citiesWeather.collectAsState()
    val cityWeather = citiesWeather.find { it.cityName == cityName }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cityName, color = Color(0xFF01579B)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color(0xFF01579B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftBlue)
                .padding(padding)
        ) {
            if (cityWeather?.isLoading == true) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF01579B))
            } else if (cityWeather?.errorRes != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(cityWeather.errorRes), color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.addCity(cityName) }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            } else if (cityWeather?.weather != null) {
                val weather = cityWeather.weather
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Current Weather
                    Text(
                        text = "${weather.currentWeather?.temperature?.toInt()}°",
                        fontSize = 80.sp,
                        color = Color(0xFF01579B),
                        fontWeight = FontWeight.Thin
                    )
                    Text(
                        text = stringResource(getWeatherDescriptionRes(weather.currentWeather?.weathercode ?: 0)),
                        fontSize = 20.sp,
                        color = Color(0xFF01579B).copy(alpha = 0.8f)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    // Hourly Forecast
                    Text(
                        text = stringResource(R.string.hourly_forecast),
                        color = Color(0xFF01579B),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val hourly = weather.hourly
                        if (hourly != null) {
                            items(12) { index ->
                                HourlyItem(
                                    time = hourly.time[index].substring(11),
                                    temp = hourly.temperature2m[index],
                                    code = hourly.weathercode[index]
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Daily Forecast
                    Text(
                        text = stringResource(R.string.next_7_days),
                        color = Color(0xFF01579B),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val daily = weather.daily
                            if (daily != null) {
                                repeat(7) { index ->
                                    val date = LocalDate.parse(daily.time[index])
                                    val dayName = if (index == 0) stringResource(R.string.today) else date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
                                    
                                    DailyItem(
                                        date = dayName,
                                        maxTemp = daily.temperature2mMax[index],
                                        minTemp = daily.temperature2mMin[index],
                                        code = daily.weathercode[index]
                                    )
                                    if (index < 6) HorizontalDivider(color = Color(0xFF01579B).copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HourlyItem(time: String, temp: Double, code: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(text = time, color = Color(0xFF01579B), fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "${temp.toInt()}°", color = Color(0xFF01579B), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DailyItem(date: String, maxTemp: Double, minTemp: Double, code: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = date, color = Color(0xFF01579B), modifier = Modifier.weight(1f))
        Text(
            text = stringResource(getWeatherDescriptionRes(code)),
            color = Color(0xFF01579B).copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.weight(1.5f)
        )
        Text(
            text = "${maxTemp.toInt()}° / ${minTemp.toInt()}°",
            color = Color(0xFF01579B),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
    }
}
