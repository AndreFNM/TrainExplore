package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ItemFavorito")
data class ItemFavorito(
    @PrimaryKey(autoGenerate = true) val itemId: Int
)
