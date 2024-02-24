package com.example.trainexplore.entities

import androidx.room.PrimaryKey

data class Ponto_interesse(
    @PrimaryKey(autoGenerate = true) val id:Int,
    val nome: String,
    val latitude: Double,
    val longitude: Double
)
