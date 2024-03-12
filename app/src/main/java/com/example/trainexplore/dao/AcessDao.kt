package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Delete
import com.example.trainexplore.entities.Acess


@Dao
interface AcessDao {


    @Insert
    suspend fun insertAces(acess: Acess)

    @Delete
    suspend fun deleteAces(acess: Acess)


}