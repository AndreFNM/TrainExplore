package com.example.trainexplore.entities

import androidx.room.Embedded
import androidx.room.Relation

data class Favorito_ponto(
    @Embedded val favorito: Favorito,
    @Relation(
        parentColumn = "id",
        entityColumn = "favorito_id"
    )

    val favorito_ponto: Ponto_interesse
)
