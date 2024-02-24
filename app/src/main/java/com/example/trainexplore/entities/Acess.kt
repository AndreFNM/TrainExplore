package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(tableName = "Acess",
    primaryKeys = ["id","id"],
    foreignKeys = [
        ForeignKey(entity = Acessibilidade::class, parentColumns = ["id"], childColumns = ["id_acessibilidade"]),
        ForeignKey(entity = Estacao::class, parentColumns = ["id"], childColumns = ["id_estacao"])
    ]
)
data class Acess(
    @PrimaryKey val id_acessibilidade: Int,
    @PrimaryKey val id_estacao: Int,
)
