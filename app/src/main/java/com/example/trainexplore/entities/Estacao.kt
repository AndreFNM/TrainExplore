package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(tableName = "Estacao")
data class Estacao(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val ano_construcao: String,
    val foto: String?,
    val descricao: String,
    val estado_atual: String
)
