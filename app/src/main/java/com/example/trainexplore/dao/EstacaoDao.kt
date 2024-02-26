package com.example.trainexplore.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import com.example.trainexplore.entities.Estacao

@Dao
interface EstacaoDao{
    // inserir estação
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEstacao(estacao: Estacao): Long

    //update a uma estacao
    @Update
    suspend fun updateEstacao(estacao: Estacao)

    //apagar uma estacao especifica
    @Query("DELETE FROM Estacao WHERE id= :id")
    suspend fun deleteEstacao(id: Int)

}