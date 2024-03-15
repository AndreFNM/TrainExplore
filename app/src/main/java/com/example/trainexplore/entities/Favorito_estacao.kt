package com.example.trainexplore.entities

import androidx.room.Embedded
import androidx.room.Relation

data class Favorito_estacao(
    @Embedded val favorito: Favorito,
    @Relation(
        parentColumn = "id",
        entityColumn = "favorito_id"
    )

    val favorito_estacao: Estacao
)
