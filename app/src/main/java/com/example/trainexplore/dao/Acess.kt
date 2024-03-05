package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import com.example.trainexplore.entities.Acess


@Dao
interface Acess {


    @Insert
    suspend fun insertAces(acess: Acess)

    @Delete
    suspend fun deleteAces(acess: Acess)


}