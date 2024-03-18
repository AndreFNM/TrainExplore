package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
@Entity(tableName = "Favorito",
    foreignKeys = [
        ForeignKey(entity = Utilizador::class, parentColumns = ["id"], childColumns = ["utilizadorId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Estacao::class, parentColumns = ["id"], childColumns = ["estacaoId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = Ponto_interesse::class, parentColumns = ["id"], childColumns = ["pontoInteresseId"], onDelete = ForeignKey.SET_NULL)
    ], indices = [
        Index(value = ["utilizadorId", "estacaoId"], unique = true),
        Index(value = ["utilizadorId", "pontoInteresseId"], unique = true)
    ]
)
data class Favorito(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val estacaoId: Int?,
    val pontoInteresseId: Int?,
    val utilizadorId: Int
)
