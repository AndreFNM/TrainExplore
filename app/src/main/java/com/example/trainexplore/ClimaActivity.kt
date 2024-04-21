package com.example.trainexplore

import android.annotation.SuppressLint
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clima)

        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        if (latitude != 0.0 && longitude != 0.0) {
            getWeather(latitude, longitude)
        } else {
            Toast.makeText(this, "Coordenadas inválidas.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getWeather(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherService::class.java)
        val weatherCall = weatherService.getWeather(latitude, longitude, "b813dceb30c23ec40fd0b61990888903", "metric", "pt")

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

    @SuppressLint("SetTextI18n")
    private fun updateUI(weather: WeatherResponse) {
        findViewById<TextView>(R.id.textViewWeatherDescription).text =
            "O clima em ${weather.name}: ${weather.weather[0].description} \n Temperatura: ${weather.main.temp}°C"
    }
}
