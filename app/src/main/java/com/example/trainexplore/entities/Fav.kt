package com.example.trainexplore.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(
    tableName = "Fav",
    primaryKeys = ["id_utilizador","id_favorito"],
    foreignKeys = [
        ForeignKey(entity = Utilizador::class, parentColumns = ["id"], childColumns = ["id_utilizador"]),
        ForeignKey(entity = Favorito::class, parentColumns = ["id"], childColumns = ["id_favorito"])
    ]
)
data class Fav(
    @ColumnInfo(name = "id_utilizador") val id_utilizador: Int,
    @ColumnInfo(name = "id_favorito") val id_favorito: Int,
)
