package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(tableName = "Favorito",
        foreignKeys =
        [ForeignKey(entity = Ponto_interesse::class, parentColumns = arrayOf("id"), childColumns = arrayOf("id"), onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Estacao::class, parentColumns = arrayOf("id"), childColumns = arrayOf("id"), onDelete = ForeignKey.CASCADE)
        ])
data class Favorito(
    @PrimaryKey val id:Int
)
