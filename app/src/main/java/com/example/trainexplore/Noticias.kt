package com.example.trainexplore

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainexplore.noticias.NewsAdapter
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface NoticiasServico {
    @GET("v2/everything")
    suspend fun getNews(
        @Query("q") query: String?,
        @Query("qInTitle") qInTitle: String? = null,
        @Query("sources") sources: String? = null,
        @Query("domains") domains: String? = null,
        @Query("language") language: String = "pt",
        @Query("apiKey") apiKey: String
    ): NoticiasResposta
}

data class NoticiasResposta(val status: String, val totalResults: Int, val articles: List<NewsArticle>)

data class NewsArticle(
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String
)


class Noticias : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_noticias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.newsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = NewsAdapter(emptyList())
        recyclerView.adapter = adapter
        fetchNews()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchNews() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val newsService = retrofit.create(NoticiasServico::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = newsService.getNews(query = "Comboios de Portugal OR CP", apiKey = "9442fe2ecbeb48f48fc7c7d196937dec")
                if (response.status == "ok" && response.articles.isNotEmpty()) {
                    Log.d("NewsAPI", "Fetched articles: ${response.articles}")
                    val validArticles = response.articles.filter {
                        it.title != "[Removed]" && it.description != "[Removed]"
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        adapter.updateData(validArticles)
                    }
                } else {
                    Log.e("NewsAPI", "Failed to fetch articles: Status: ${response.status}")
                }
            } catch (e: HttpException) {
                Log.e("NewsAPI", "HTTP Error response: ${e.response()?.errorBody()?.string()}")
            } catch (e: Exception) {
                Log.e("NewsAPI", "Error fetching news", e)
            }
        }
    }



}


