package com.example.trainexplore.database

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainexplore.entities.Estacao
import androidx.lifecycle.asLiveData


class Estacao_ViewModel(private val repository: Estacao_Repository): ViewModel() {

    val readAllEstacoes: LiveData<List<Estacao>> = repository.readAllEstacoes.asLiveData()
}



