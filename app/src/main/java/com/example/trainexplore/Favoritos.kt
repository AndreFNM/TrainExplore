package com.example.trainexplore

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.database.FavoritosDB.FavoritoAdapter
import com.example.trainexplore.entities.Estacao
import com.example.trainexplore.entities.Ponto_interesse
import com.example.trainexplore.loginSystem.SessionManager


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Favoritos : Fragment() {
    private lateinit var adapter: FavoritoAdapter
    private var param1: String? = null
    private var param2: String? = null
    private var allItems = listOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favoritos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.favoritosRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val spinner: Spinner = view.findViewById(R.id.filtro_favoritos)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filtro_favoritos,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val filterType = parent.getItemAtPosition(position) as String
                Log.d("Favoritos", "Filtering as: $filterType")
                adapter.filter(filterType)
            }


            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        adapter = FavoritoAdapter(
            items = emptyList(),
            onRemoverClicked = this::removerFavorito,
            onItemClicked = { latitude, longitude ->
                startActivity(Intent(activity, MapDirecoesActivity::class.java).apply {
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                })
            },
            context = requireContext()
        )
        recyclerView.adapter = adapter

        val userId = SessionManager.userId?.toIntOrNull()
        userId?.let { uid ->
            val db = AppDatabase.getDatabase(requireContext())
            val estacoesLiveData = db.favoritoDao().getFavoritosEstacaoByUtilizador(uid)
            val pontosInteresseLiveData = db.favoritoDao().getFavoritosPontoInteresseByUtilizador(uid)
            estacoesLiveData.observe(viewLifecycleOwner) { estacoes ->
                pontosInteresseLiveData.observe(viewLifecycleOwner) { pontos ->
                    allItems = estacoes + pontos
                    adapter.updateData(allItems as List<Any>)
                }
            }
        }
    }






    private fun removerFavorito(item: Any) {
        val userId = SessionManager.userId?.toIntOrNull()
        userId?.let {
            if (item is Estacao || item is Ponto_interesse) {
                Thread {
                    val dao = AppDatabase.getDatabase(requireContext()).favoritoDao()
                    val itemId = if (item is Estacao) item.id else (item as Ponto_interesse).id

                    dao.removerFavoritoByEstacaoId(itemId) // Ensure this method is generic or correct method is called

                    activity?.runOnUiThread {
                        adapter.removerItem(item)
                    }
                }.start()
            }
        }
    }


    private fun removerEstacaoFavoritos(estacao: Estacao) {
        val userId = SessionManager.userId?.toIntOrNull()
        if (userId != null) {

            Thread{
                val dao = AppDatabase.getDatabase(requireContext()).favoritoDao()
                dao.removerFavoritoByEstacaoId(estacao.id)

                activity?.runOnUiThread {
                    adapter.removerItem(estacao)
                }
            }.start()
        }
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Favoritos().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}