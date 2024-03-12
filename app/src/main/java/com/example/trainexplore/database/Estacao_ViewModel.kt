package com.example.trainexplore.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.trainexplore.entities.Estacao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class Estacao_ViewModel(application: Application): AndroidViewModel(application) {

    private val readAllEstacoes: LiveData<List<Estacao>>
    private val repository: Estacao_Repository

    init {
        val estacaoDao = AppDatabase.getDatabase(application).Estacao()
        repository = Estacao_Repository(estacaoDao)
        readAllEstacoes = repository.readAllData
    }

    fun addEstacao(estacao: Estacao){
        viewModelScope.launch(Dispatchers.IO) {
            repository.addEstacao(estacao)
        }
    }
}