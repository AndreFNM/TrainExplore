package com.example.trainexplore.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(tableName = "Acess",
    primaryKeys = ["id_acessibilidade","id_estacao"],
    foreignKeys = [
        ForeignKey(entity = Acessibilidade::class, parentColumns = ["id"], childColumns = ["id_acessibilidade"]),
        ForeignKey(entity = Estacao::class, parentColumns = ["id"], childColumns = ["id_estacao"])
    ]
)
data class Acess(
    @ColumnInfo(name = "id_acessibilidade") val id_acessibilidade: Int,
    @ColumnInfo(name = "id_estacao") val id_estacao: Int,
)
