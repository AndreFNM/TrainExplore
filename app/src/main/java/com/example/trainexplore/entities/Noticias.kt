package com.example.trainexplore.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(tableName = "Noticias",
        primaryKeys = ["id_estacao","id_noticias"],
        foreignKeys = [
            ForeignKey(entity = Estacao::class, parentColumns = ["id"], childColumns = ["id_estacao"]),
            ForeignKey(entity = Noticia::class, parentColumns = ["id"], childColumns = ["id_noticias"])
        ]
)
data class Noticias(
    @ColumnInfo(name = "id_estacao") val id_estacao: Int,
    @ColumnInfo(name = "id_noticias") val id_noticias: Int,

)
