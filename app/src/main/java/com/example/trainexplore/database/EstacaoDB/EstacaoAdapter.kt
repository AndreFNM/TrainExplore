package com.example.trainexplore.database.EstacaoDB

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trainexplore.R
import com.example.trainexplore.entities.Estacao

class EstacaoAdapter(private val estacoes:List<Estacao>, private val clickListener: (Estacao) -> Unit) : RecyclerView.Adapter<EstacaoAdapter.EstacaoViewHolder>() {
    private val estacoesList: MutableList<Estacao> = estacoes.toMutableList()
    private var estacoesFull: List<Estacao> = estacoes.toList()

    class EstacaoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagemViewEstacao: ImageView = view.findViewById(R.id.imagemViewEstacao)
        val nomeViewEstacao: TextView = view.findViewById(R.id.nomeViewEstacao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstacaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_estacao, parent, false)
        return EstacaoViewHolder(view)
    }

     fun updateList(estacoes: List<Estacao>) {
         this.estacoesList.clear()
         this.estacoesList.addAll(estacoes)
         notifyDataSetChanged()
     }

    fun filter(query: String) {
            val filteredList = if (query.isEmpty()) {
                estacoesFull
            } else {
                estacoesFull.filter {
                    it.nome.contains(query, ignoreCase = true)
                }
            }
            updateList(filteredList)
        }

     fun setFullEstacoes(estacoes: List<Estacao>) {
         this.estacoesFull = estacoes
     }


    override fun onBindViewHolder(holder: EstacaoViewHolder, position: Int) {
        val estacao = estacoesList[position]
        //Para poder criar uma "seccao" para a estacao quando o utilizador clica na mesma
        holder.itemView.setOnClickListener { clickListener(estacao) }
        //set imagem e texto aqui
        holder.nomeViewEstacao.text = estacao.nome

        //utilizar Glide para dar load á imagem a partir de um url
        Glide.with(holder.itemView.context)
            .load(estacao.foto)
            .into(holder.imagemViewEstacao)
    }

    override fun getItemCount() = estacoesList.size


}