package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(tableName = "Favorito",
    foreignKeys = [
        ForeignKey(
            entity = ItemFavorito::class,
            parentColumns = arrayOf("itemId"),
            childColumns = arrayOf("id"),
            onDelete = ForeignKey.CASCADE
        )
    ])
data class Favorito(
    @PrimaryKey(autoGenerate = true) val id:Int
)
