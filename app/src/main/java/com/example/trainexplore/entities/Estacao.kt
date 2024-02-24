package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "Estacao")
data class Estacao(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val ano_construcao: Date, // perguntar se existe melhor forma de guardar, visto que esta a usar java.sql.Date
    val foto: String?,
    val descricao: String,
    val estado_atual: String
)
