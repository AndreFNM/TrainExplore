package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import com.example.trainexplore.entities.Favorito

@Dao
interface FavoritoDao {
    //Inserir favorito
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFavorito(favorito: Favorito): Long

    @Update
    suspend fun updateFavorito(favorito: Favorito)

    //apagar um favorito especifica
    @Query("DELETE FROM Favorito WHERE id= :id")
    suspend fun deleteFavorito(id: Int)

}