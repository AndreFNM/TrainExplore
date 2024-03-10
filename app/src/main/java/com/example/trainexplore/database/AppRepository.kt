package com.example.trainexplore.database

import androidx.lifecycle.LiveData
import com.example.trainexplore.dao.EstacaoDao
import com.example.trainexplore.entities.Estacao

class AppRepository(private val EstacaoDao: EstacaoDao) {

    val readAllData: LiveData<List<Estacao>> = EstacaoDao.selectEstacoes()

    suspend fun addEstacao(estacao: Estacao){
        EstacaoDao.insertEstacao(estacao)
    }
}