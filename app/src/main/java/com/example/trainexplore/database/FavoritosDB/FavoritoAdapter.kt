package com.example.trainexplore.database.FavoritosDB

import android.content.Context
import android.health.connect.datatypes.ExerciseSessionRecord
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.trainexplore.entities.Estacao
import com.example.trainexplore.R
import com.example.trainexplore.database.AppDatabase

class FavoritoAdapter (
    private var estacoes: List<Estacao>,
    private val onRemoverClicked: (Estacao) -> Unit
): RecyclerView.Adapter<FavoritoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.nomeEstacaoFavorito)
        val removerButton: ImageView = view.findViewById(R.id.removerFavoritoButton)
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

        holder.removerButton.setOnClickListener{
            onRemoverClicked(estacao)
        }
    }

    fun removerEstacao(estacao: Estacao) {
        val index = estacoes.indexOfFirst { it.id == estacao.id }
        if (index != -1){
            estacoes = estacoes.toMutableList().apply {
                removeAt(index)
            }
            notifyItemRemoved(index)
        }
    }
    override fun getItemCount() = estacoes.size
}