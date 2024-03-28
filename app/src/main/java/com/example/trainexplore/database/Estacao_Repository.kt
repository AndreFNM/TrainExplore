package com.example.trainexplore.database

import androidx.lifecycle.LiveData
import com.example.trainexplore.dao.EstacaoDao
import com.example.trainexplore.entities.Estacao
import kotlinx.coroutines.flow.Flow
import androidx.lifecycle.asLiveData

class Estacao_Repository(private val estacaoDao: EstacaoDao) {

    val readAllEstacoes: Flow<List<Estacao>> = estacaoDao.getAllEstacoes()


}