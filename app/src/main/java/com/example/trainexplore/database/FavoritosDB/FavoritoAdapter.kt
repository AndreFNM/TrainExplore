package com.example.trainexplore.database.FavoritosDB

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trainexplore.MapDirecoesActivity
import com.example.trainexplore.entities.Estacao
import com.example.trainexplore.entities.Ponto_interesse
import com.example.trainexplore.R

class FavoritoAdapter(
    private var items: List<Any>,
    private val onRemoverClicked: (Any) -> Unit,
    private val onItemClicked: (Double, Double) -> Unit,
    private val context: Context
) : RecyclerView.Adapter<FavoritoAdapter.ViewHolder>() {

    private var allItems: List<Any> = items // Internal state to keep all items

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: View = view.findViewById(R.id.item_layout)
        val textView: TextView = view.findViewById(R.id.nomeEstacaoFavorito)
        val removerButton: ImageView = view.findViewById(R.id.removerFavoritoButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.favorito_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        when (item) {
            is Estacao -> {
                holder.textView.text = item.nome
                holder.layout.setOnClickListener { onItemClicked(item.latitude, item.longitude) }
            }
            is Ponto_interesse -> {
                holder.textView.text = item.nome
                holder.layout.setOnClickListener { onItemClicked(item.latitude, item.longitude) }
            }
        }
        holder.removerButton.setOnClickListener { onRemoverClicked(item) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<Any>) {
        this.allItems = newItems
        filter("")
    }

    fun filter(filterType: String) {
        Log.d("Favoritos", "Filter type: $filterType")
        items = when (filterType) {
            "Estações" -> allItems.filterIsInstance<Estacao>().also { Log.d("Favoritos", "Filtrar estações: ${it.size}") }
            "Pontos de interesse" -> allItems.filterIsInstance<Ponto_interesse>().also { Log.d("Favoritos", "filtrar pontos de interesse: ${it.size}") }
            else -> allItems
        }
        notifyDataSetChanged()
    }



    fun removerItem(item: Any) {
        val index = items.indexOfFirst { it == item }
        if (index != -1) {
            items = items.toMutableList().apply {
                removeAt(index)
            }
            notifyItemRemoved(index)
        }
    }


}