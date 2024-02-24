package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(
    tableName = "Fav",
    primaryKeys = ["id","id"],
    foreignKeys = [
        ForeignKey(entity = Utilizador::class, parentColumns = ["id"], childColumns = ["id_utilizdor"]),
        ForeignKey(entity = Favorito::class, parentColumns = ["id"], childColumns = ["id_favorito"])
    ]
)
data class Fav(
    @PrimaryKey val id_favorito: Int,
    @PrimaryKey val id_utilizador: Int
)
