package com.example.trainexplore

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.database.FavoritosDB.FavoritoAdapter
import com.example.trainexplore.loginSystem.SessionManager


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Favoritos : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

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
        val adapter = FavoritoAdapter(emptyList())
        recyclerView.adapter = adapter

        val userId = SessionManager.userId?.toIntOrNull()
        if (userId != null) {
            AppDatabase.getDatabase(requireContext()).favoritoDao().getFavoritosEstacaoByUtilizador(userId).observe(viewLifecycleOwner) { estacoes ->
                adapter.updateData(estacoes)
            }
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