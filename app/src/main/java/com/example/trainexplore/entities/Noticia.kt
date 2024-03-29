package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "Noticia")
data class Noticia(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val Titulo :String,
    val Conteudo: String,
    val data: String
)
