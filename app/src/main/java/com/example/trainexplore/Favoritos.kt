package com.example.trainexplore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.database.FavoritosDB.FavoritoAdapter
import com.example.trainexplore.entities.Estacao
import com.example.trainexplore.entities.Ponto_interesse
import com.example.trainexplore.loginSystem.SessionManager

class Favoritos : Fragment() {
    private lateinit var adapter: FavoritoAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favoritos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupSpinner(view)
        loadFavorites()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.favoritosRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = FavoritoAdapter(
            items = emptyList(),
            onRemoverClicked = this::removerFavorito,
            onItemClicked = this::navigateToMap,
            context = requireContext()
        )
        recyclerView.adapter = adapter
    }

    private fun setupSpinner(view: View) {
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
                adapter.filter(parent.getItemAtPosition(position) as String)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadFavorites() {
        val userId = SessionManager.userId?.toIntOrNull()
        userId?.let { uid ->
            val db = AppDatabase.getDatabase(requireContext())
            db.favoritoDao().getFavoritosEstacaoByUtilizador(uid).observe(viewLifecycleOwner, Observer { estacoes ->
                db.favoritoDao().getFavoritosPontoInteresseByUtilizador(uid).observe(viewLifecycleOwner, Observer { pontos ->
                    adapter.updateData(estacoes + pontos)
                })
            })
        }
    }

    private fun removerFavorito(item: Any) {
        val userId = SessionManager.userId?.toIntOrNull() ?: return  // Ensure you handle a null user ID appropriately

        Thread {
            val db = AppDatabase.getDatabase(requireContext()).favoritoDao()
            if (item is Estacao) {
                db.removerFavoritoByEstacaoId(item.id, userId)
            } else if (item is Ponto_interesse) {
                db.removerFavoritoByPontoInteresseId(item.id, userId)
            }

            activity?.runOnUiThread {
                adapter.removerItem(item)
            }
        }.start()
    }



    private fun refreshFavorites() {
        activity?.runOnUiThread {
            loadFavorites()
        }
    }

    private fun navigateToMap(latitude: Double, longitude: Double) {
        startActivity(Intent(activity, MapDirecoesActivity::class.java).apply {
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
        })
    }
}
