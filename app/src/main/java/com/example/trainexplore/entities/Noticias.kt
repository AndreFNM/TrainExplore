package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(tableName = "Noticias",
        primaryKeys = ["id","id"],
        foreignKeys = [
            ForeignKey(entity = Estacao::class, parentColumns = ["id"], childColumns = ["id_estacao"]),
            ForeignKey(entity = Noticia::class, parentColumns = ["id"], childColumns = ["id_noticias"])
        ]
)
data class Noticias(
    @PrimaryKey val id_noticias: Int,
    @PrimaryKey val id_estacao: Int
)
