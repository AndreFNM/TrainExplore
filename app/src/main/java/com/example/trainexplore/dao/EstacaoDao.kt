package com.example.trainexplore.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import com.example.trainexplore.entities.Estacao

@Dao
interface EstacaoDao{

    @Query("SELECT * FROM Estacao")
    fun getAllEstacoes(): LiveData<List<Estacao>>

    //update a uma estacao
    @Update
    suspend fun updateEstacao(estacao: Estacao)


}