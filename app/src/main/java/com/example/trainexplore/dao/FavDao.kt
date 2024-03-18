package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete

@Dao
interface FavDao {

    @Insert
    suspend fun insertFav(fav: Fav)

    @Delete
    suspend fun deleteFav(fav: Fav)
}