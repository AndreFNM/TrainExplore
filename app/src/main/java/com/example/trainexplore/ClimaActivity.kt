package com.example.trainexplore

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trainexplore.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.math.round

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val name: String
)

data class Main(
    val temp: Double,
    val humidity: Int
)

data class Weather(
    val main: String,
    val description: String
)

interface WeatherService {
    @GET("data/2.5/weather")
    fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "pt"
    ): Call<WeatherResponse>
}

class ClimaActivity : AppCompatActivity() {

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clima)

        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)

        if (latitude != 0.0 && longitude != 0.0) {
            getWeather(latitude, longitude)
        } else {
            Toast.makeText(this, "Coordenadas inválidas.", Toast.LENGTH_LONG).show()
        }

        val refreshButton = findViewById<Button>(R.id.buttonRefreshWeather)
        refreshButton.setOnClickListener {
            getWeather(latitude, longitude)
        }
    }

    private fun getWeather(latitude: Double, longitude: Double) {
        val apiKey = getApiKey()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherService::class.java)
        val weatherCall = weatherService.getWeather(latitude, longitude, apiKey, "metric", "pt")

        weatherCall.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        updateUI(it)
                    }
                } else {
                    Toast.makeText(this@ClimaActivity, "Falha ao obter dados do clima", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@ClimaActivity, "Erro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getApiKey(): String {
        return packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData.getString("clima.API_KEY")
            ?: throw IllegalStateException("API key não encontrada no manifest")
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(weather: WeatherResponse) {
        val weatherDescription = findViewById<TextView>(R.id.textViewWeatherDescription)
        val temperature = findViewById<TextView>(R.id.textViewTemperature)
        val weatherIcon = findViewById<ImageView>(R.id.imageViewWeatherIcon)

        weatherDescription.text = "O clima em ${weather.name}: ${weather.weather[0].description}"
        val roundedTemp = round(weather.main.temp).toInt()
        temperature.text = "Temperatura: ${roundedTemp}°C"

        // ícon do clima dependendo do tempo
        val weatherCondition = weather.weather[0].main
        val weatherIconResId = when (weatherCondition.toLowerCase()) {
            "clear" -> R.drawable.ceu_limp
            "clouds" -> R.drawable.algumas_nuvens
            "rain" -> R.drawable.chuva
            "drizzle" -> R.drawable.muita_chuva
            "thunderstorm" -> R.drawable.trovoada
            "snow" -> R.drawable.neve
            "mist", "smoke", "haze", "dust", "fog", "sand", "ash", "squall", "tornado" -> R.drawable.mist
            else -> R.drawable.ceu_limp
        }
        weatherIcon.setImageResource(weatherIconResId)
    }
}
