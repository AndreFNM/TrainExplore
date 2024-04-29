package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import com.example.trainexplore.entities.Favorito

import com.example.trainexplore.entities.Ponto_interesse

@Dao
interface Ponto_interesseDao {

    @Query("SELECT * FROM Ponto_interesse WHERE nome= :nome AND latitude = :latitude AND longitude = :longitude LIMIT 1")
    suspend fun getPontoInteresseByNomeECoordenadas(nome: String, latitude: Double, longitude: Double): Ponto_interesse?


    @Insert
    suspend fun insertPontoInteresse(pontoInteresse: Ponto_interesse): Long // Da retorno do ID dos dados inseridos

    @Insert
    suspend fun insertFavorito(favorito: Favorito)

}