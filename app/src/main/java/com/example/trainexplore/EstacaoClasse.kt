package com.example.trainexplore

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.trainexplore.database.AppDatabase

class EstacaoClasse : Fragment(R.layout.fragment_comboios) { // Ensure you have a layout resource here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textViewEstacaoNome: TextView = view.findViewById(R.id.textViewEstacaoNome)

        // Get the instance of the database and DAO
        val estacaoDao = AppDatabase.getDatabase(requireContext()).estacaoDao()

        // Observe the LiveData
        estacaoDao.getAllEstacoes().observe(viewLifecycleOwner, Observer { estacoes ->
            if (estacoes.isNotEmpty()) {
                val firstEstacao = estacoes.first()
                textViewEstacaoNome.text = firstEstacao.nome
            } else {
                textViewEstacaoNome.text = "No data available"
            }
        })
    }
}
