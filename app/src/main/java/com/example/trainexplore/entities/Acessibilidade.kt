package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "Acessibilidade")
data class Acessibilidade(
    @PrimaryKey val id:Int,
    val informacao: String,
    val descricai: String
)
