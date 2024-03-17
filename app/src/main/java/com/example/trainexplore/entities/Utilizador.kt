package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "Utilizador")
data class Utilizador(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val nome: String,
    val email: String,
    val pass: String,
    val imageUrl: String?, //Url para a imagem na cloud
)
