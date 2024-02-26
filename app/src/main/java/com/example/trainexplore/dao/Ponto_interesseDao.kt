package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import com.example.trainexplore.entities.Ponto_interesse

@Dao
interface Ponto_interesseDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPonto(pontoInteresse: Ponto_interesse) : Long

    @Update
    suspend fun updatePonto(pontoInteresse: Ponto_interesse)

    @Query("DELETE FROM Ponto_interesse WHERE id = :id")
    suspend fun deletePonto(id: Int)

}