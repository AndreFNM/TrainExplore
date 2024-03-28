package com.example.trainexplore.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import com.example.trainexplore.entities.Estacao
import kotlinx.coroutines.flow.Flow

@Dao
interface EstacaoDao{

    @Query("SELECT * FROM Estacao")
    fun getAllEstacoes(): Flow<List<Estacao>>

    //update a uma estacao
    @Update
    suspend fun updateEstacao(estacao: Estacao)

    //apagar uma estacao especifica
    @Query("SELECT * FROM Estacao")
    fun selectEstacoes(): LiveData<List<Estacao>>

}