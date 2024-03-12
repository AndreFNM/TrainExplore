package com.example.trainexplore.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import com.example.trainexplore.entities.Utilizador

@Dao
interface UtilizadorDao{

    // Inserir novo utilizador
    @Insert
    suspend fun  insertUtilizador(Utilizador: Utilizador): Long

    //update a um utilizador
    @Update
    suspend fun updateUtilizador(Utilizador: Utilizador)

    @Delete
    suspend fun deleteUtilizador(Utilizador: Utilizador)

    //get todos os utilizadores
    @Query("SELECT * FROM Utilizador")
    fun getAllUtilizadores(): LiveData<List<Utilizador>>

    //get utilizadores por id
    @Query("SELECT * FROM utilizador WHERE id= :id")
    suspend fun getUtilizadorById(id: Int): Utilizador?

    //get Utilizadores por email
    @Query("SELECT * FROM utilizador WHERE email= :email")
    suspend fun  getUtilizadorByEmail(email: String): Utilizador?

    //Delete utilizador
    @Query("DELETE FROM Utilizador")
    suspend fun deleteAllUtilizadores()
}