package com.example.trainexplore.noticias

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.trainexplore.NewsArticle
import com.example.trainexplore.R

class NewsAdapter(private var articles: List<NewsArticle>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.title)
        val description: TextView = view.findViewById(R.id.description)
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_noticia, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]
        holder.title.text = article.title ?: "Nenhum Título disponível"
        holder.description.text = article.description ?: "Descrição não disponível"
        Glide.with(holder.imageView.context).load(article.urlToImage ?: R.drawable.ic_launcher_background).into(holder.imageView)
    }

    override fun getItemCount() = articles.size

    fun updateData(newArticles: List<NewsArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    fun setData(newData: List<NewsArticle>) {
        articles = newData
        notifyDataSetChanged()
    }
}

