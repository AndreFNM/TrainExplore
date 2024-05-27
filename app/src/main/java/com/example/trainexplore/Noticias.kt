package com.example.trainexplore

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.trainexplore.noticias.NewsAdapter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.logging.HttpLoggingInterceptor

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
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_noticias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(view)
        setupSwipeRefreshLayout(view)
        fetchNews()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.newsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = NewsAdapter(emptyList())
        recyclerView.adapter = adapter
    }

    private fun setupSwipeRefreshLayout(view: View) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            if (isNetworkAvailable()) {
                fetchNews()
            } else {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Verifique sua conexão com a internet", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return connectivityManager?.activeNetworkInfo?.isConnected == true
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("Noticias", "Error in coroutine", throwable)
        lifecycleScope.launch(Dispatchers.Main) {
            updateUI(emptyList(), "Erro ao processar a requisição.")
        }
    }

    private fun getApiKey(): String {
        val context = requireContext()
        val packageManager = context.packageManager
        val packageName = context.packageName
        return packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData.getString("noticias.API_KEY")
            ?: throw IllegalStateException("API key não encontrada no manifest")
    }

    private fun fetchNews() {
        val retrofit = setupRetrofit()
        val newsService = retrofit.create(NoticiasServico::class.java)

        lifecycleScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                val response = newsService.getNews(
                    query = "\"Comboios Portugal\" OR \"CP\"",
                    qInTitle = null,
                    sources = null,
                    domains = null,
                    language = "pt",
                    apiKey = getApiKey()
                )

                if (response.status == "ok") {
                    val validArticles = response.articles.filter {
                        it.title != "[Removed]" && it.description != "[Removed]"
                    }
                    lifecycleScope.launch(Dispatchers.Main) {
                        updateUI(validArticles, null)
                    }
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        updateUI(emptyList(), "No new articles found.")
                    }
                }
            } catch (e: HttpException) {
                lifecycleScope.launch(Dispatchers.Main) {
                    updateUI(emptyList(), "Erro a conectar ao serviço.")
                }
            } catch (e: Exception) {
                lifecycleScope.launch(Dispatchers.Main) {
                    updateUI(emptyList(), "Erro ao processar a requisição.")
                }
            }
        }
    }

    private fun setupRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Cache-Control", "no-cache")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun updateUI(articles: List<NewsArticle>, message: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            adapter.updateData(articles)
            swipeRefreshLayout.isRefreshing = false
            message?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }
    }
}
