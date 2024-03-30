package com.example.trainexplore.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.lifecycle.LiveData
import com.example.trainexplore.entities.Estacao

@Dao
interface EstacaoDao{

    @Query("SELECT * FROM Estacao")
    fun getAllEstacoes(): LiveData<List<Estacao>>


    @Query("SELECT * FROM Estacao LIMIT 1")
    fun getFirstEstacao(): LiveData<Estacao>

    @Query("SELECT * FROM Estacao WHERE id = :id")
    fun getEstacaoById(id: Int): LiveData<Estacao>


}