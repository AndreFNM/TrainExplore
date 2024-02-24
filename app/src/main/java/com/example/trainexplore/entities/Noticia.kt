package com.example.trainexplore.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "Noticia")
data class Noticia(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val Titulo :Int,
    val Conteudo: String,
    val data: Date
)
