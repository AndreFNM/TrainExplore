package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "Acessibilidade")
data class Acessibilidade(
    @PrimaryKey(autoGenerate = true) val id:Int,
    val informacao: String
)
