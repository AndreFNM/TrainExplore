package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import com.example.trainexplore.entities.Fav

interface FavDao {

    @Insert
    suspend fun insertFav(fav: Fav)

    @Delete
    suspend fun deleteFav(fav: Fav)
}