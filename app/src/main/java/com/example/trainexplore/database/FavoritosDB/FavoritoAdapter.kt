package com.example.trainexplore.database.FavoritosDB

import android.health.connect.datatypes.ExerciseSessionRecord
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.trainexplore.entities.Estacao
import com.example.trainexplore.R

class FavoritoAdapter (private var estacoes: List<Estacao>): RecyclerView.Adapter<FavoritoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.nomeEstacaoFavorito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.favorito_item, parent, false)
        return ViewHolder(view)
    }

    fun updateData(novaEstacao: List<Estacao>) {
        this.estacoes = novaEstacao
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val estacao = estacoes[position]
        holder.textView.text = estacao.nome

        //depois colocar aqui os onClickListener
    }

    override fun getItemCount() = estacoes.size
}