package com.example.trainexplore.database

import androidx.lifecycle.LiveData
import com.example.trainexplore.dao.UtilizadorDao
import com.example.trainexplore.entities.Estacao
import com.example.trainexplore.entities.Utilizador

class Utilizador_Repository(private val UtilizadorDao: UtilizadorDao) {

    val readAllData: LiveData<List<Utilizador>> = UtilizadorDao.getAllUtilizadores()

    suspend fun addUtilizador(utilizador: Utilizador){
        UtilizadorDao.insertUtilizador(utilizador)
    }

}