package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.example.trainexplore.entities.Favorito

@Dao
interface FavoritoDao {
    @Transaction
    @Query("SELECT * FROM Favorito")
    fun getFavoritos():List<Favorito>

}