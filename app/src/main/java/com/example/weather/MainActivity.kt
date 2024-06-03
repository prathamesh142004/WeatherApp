package com.example.weather

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.example.weather.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData("Mumbai")
        setupSearchCity()
    }

    private fun setupSearchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, "8882f557df088d139212a0d4ac0b036f", "metric")
        response.enqueue(object : Callback<weather> {
            override fun onResponse(call: Call<weather>, response: Response<weather>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        updateUI(responseBody, cityName)
                    } else {
                        Log.e("WeatherApp", "Empty response body")
                    }
                } else {
                    Log.e("WeatherApp", "Response unsuccessful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<weather>, t: Throwable) {
                Log.e("WeatherApp", "API call failed: ${t.message}", t)
            }
        })
    }


    private fun updateUI(weatherData: weather, cityName: String) {
        val temperature = weatherData.main?.temp?.toString() ?: "N/A"
        val humidity = weatherData.main?.humidity ?: 0
        val windspeed = weatherData.wind?.speed ?: 0.0
        val sunrise = weatherData.sys?.sunrise?.toLong() ?: 0L
        val sunset = weatherData.sys?.sunset?.toLong() ?: 0L
        val seaLevel = weatherData.main?.pressure ?: 0
        val condition = weatherData.weather.firstOrNull()?.main ?: "Unknown"
        val maxTemp = weatherData.main?.temp_max ?: 0.0
        val minTemp = weatherData.main?.temp_min ?: 0.0

        binding.temp.text = "$temperature °C"
        binding.weather.text = condition
        binding.maxTemp.text = "Max Temp: $maxTemp °C"
        binding.minTemp.text = "Min Temp: $minTemp °C"
        binding.humidity.text = "$humidity %"
        binding.windspeed.text = "$windspeed m/s"
        binding.sunrise.text = formatTime(sunrise)
        binding.sunset.text = formatTime(sunset)
        binding.sea.text = "$seaLevel"
        binding.condition.text = condition
        binding.day.text = getDayName(System.currentTimeMillis())
        binding.date.text = getCurrentDate()
        binding.cityName.text = cityName

        changeImageAccordingToCondition(condition)
    }

    private fun changeImageAccordingToCondition(condition: String) {
        Log.d("WeatherApp", "Changing image according to condition: $condition")

        // Define default resources
        var backgroundRes = R.drawable.sunny
        var animationRes = R.raw.sunnyanimation

        // Determine resources based on the weather condition
        when (condition) {
            "Clear", "Sunny", "Haze" -> {
                backgroundRes = R.drawable.sunny
                animationRes = R.raw.sunnyanimation
            }
            "Clouds", "Partly Cloudy", "Overcast", "Mist", "Fog" -> {
                backgroundRes = R.drawable.cloud_bg
                animationRes = R.raw.cloud
            }
            "Rain", "Drizzle", "Showers" -> {
                backgroundRes = R.drawable.rain_bg
                animationRes = R.raw.rain
            }
            "Snow", "Blizzard" -> {
                backgroundRes = R.drawable.snow_bg
                animationRes = R.raw.snow
            }
            else -> {
                Log.w("WeatherApp", "Unexpected condition: $condition")
            }
        }

        try {
            // Set background resource
            binding.root.setBackgroundResource(backgroundRes)
            Log.d("WeatherApp", "Setting background resource: $backgroundRes")

            // Set animation resource
            binding.lottieAnimationView.setAnimation(animationRes)
            Log.d("WeatherApp", "Setting animation resource: $animationRes")

            // Play animation
            binding.lottieAnimationView.playAnimation()
            Log.d("WeatherApp", "Playing animation")
        } catch (e: Exception) {
            Log.e("WeatherApp", "Error setting background or animation: ${e.message}", e)
        }
    }



    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun getDayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
