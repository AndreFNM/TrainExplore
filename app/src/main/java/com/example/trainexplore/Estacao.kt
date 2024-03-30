package com.example.trainexplore

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.trainexplore.database.AppDatabase

class Estacao : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.estacao)

        val estacaoId = intent.getIntExtra("estacao_id", -1)

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
    }
}