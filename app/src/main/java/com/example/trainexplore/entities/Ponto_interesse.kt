package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Ponto_interesse",
    foreignKeys = [
        ForeignKey(
            entity = Favorito::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("favorito_id"),
            onDelete = ForeignKey.CASCADE
        )
    ]
    )
data class Ponto_interesse(
    @PrimaryKey(autoGenerate = true) val id:Int,
    val nome: String,
    val latitude: Double,
    val longitude: Double,
    val favorito_id: Int
)
