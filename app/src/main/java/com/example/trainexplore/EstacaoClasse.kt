package com.example.trainexplore

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.database.EstacaoAdapter
import com.example.trainexplore.database.Estacao_Repository
import com.example.trainexplore.database.Estacao_ViewModel
import com.example.trainexplore.database.ViewModelFactory
import com.example.trainexplore.entities.Estacao

class EstacaoClasse : AppCompatActivity() {
    private lateinit var viewModel: Estacao_ViewModel
    private lateinit var estacaoAdapter: EstacaoAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_comboios)

        val repository = Estacao_Repository(AppDatabase.getDatabase(application).estacaoDao())
        viewModel = ViewModelProvider(this, ViewModelFactory(repository)).get(Estacao_ViewModel::class.java)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewModel.readAllEstacoes.observe(this) { estacoes ->
            //dar aqui os updates com a lista de estacoes

        }
    }

}