package com.example.trainexplore.database


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trainexplore.R
import com.example.trainexplore.entities.Estacao

class EstacaoAdapter(private val estacoes: List<Estacao>) : RecyclerView.Adapter<EstacaoAdapter.EstacaoViewHolder>() {

    class EstacaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize UI components from itemView here, if needed
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstacaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_comboios, parent, false)
        return EstacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EstacaoViewHolder, position: Int) {
        val estacao = estacoes[position]
        holder.itemView.findViewById<TextView>(R.id.recyclerView).text = estacao.nome
        //holder.itemView.findViewById<TextView>(R.id.recyclerView).text = estacao.descricao
        // Bind the data to UI elements in the holder here
    }

    override fun getItemCount() = estacoes.size
}
