package com.example.trainexplore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.entities.Favorito
import com.example.trainexplore.loginSystem.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Estacao : AppCompatActivity() {
    private var estacaoId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.estacao)

        //val estacaoId = intent.getIntExtra("estacao_id", -1)
        estacaoId = intent.getIntExtra("estacao_id",-1)

        if (estacaoId != -1) {
            val estacaoDao = AppDatabase.getDatabase(this).estacaoDao()
            estacaoDao.getEstacaoById(estacaoId).observe(this, Observer { estacao ->
                findViewById<TextView>(R.id.nomeViewEstacao).text = estacao.nome

                Glide.with(this)
                    .load(estacao.foto)
                    .into(findViewById(R.id.imagemViewEstacao))
            })
        } else {
            //dar aqui handle aos erros, por exemplo ID not found ou invalido
        }
        findViewById<Button>(R.id.buttonMostrarHistorico).setOnClickListener {
            val intent = Intent(this, Estacao_historico::class.java).apply {
                putExtra("estacao_id", estacaoId)
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.buttonMostrarAcessibilidade).setOnClickListener {
            val intent = Intent(this, AcessibilidadeActivity::class.java).apply {
                putExtra("estacao_id", estacaoId)
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.addEstacaoFavButton).setOnClickListener {
            addEstacaoFavoritos(estacaoId)
        }
    }

    private fun addEstacaoFavoritos(estacaoId:Int){
        val userId = SessionManager.userId?.toIntOrNull()
        if (userId != null && estacaoId != -1) {
            val novoFavorito = Favorito(estacaoId = estacaoId, pontoInteresseId = null, utilizadorId = userId)

            lifecycleScope.launch(Dispatchers.IO) {
                AppDatabase.getDatabase(applicationContext).favoritoDao().addFavorito(novoFavorito)
                withContext(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Estacao adicionada aos favoritos", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Erro ao adicionar aos favorutos", Toast.LENGTH_SHORT).show()
        }
    }
}