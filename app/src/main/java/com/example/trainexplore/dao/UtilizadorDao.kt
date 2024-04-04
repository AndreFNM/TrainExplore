package com.example.trainexplore.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.trainexplore.entities.Utilizador

@Dao
interface UtilizadorDao{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun registerUser(utilizador: Utilizador): Long

    @Query("SELECT * FROM Utilizador WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): Utilizador?
}