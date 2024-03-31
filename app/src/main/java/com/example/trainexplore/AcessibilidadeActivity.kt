package com.example.trainexplore

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trainexplore.dao.EstacaoComAcessibilidade
import com.example.trainexplore.database.AppDatabase
import org.w3c.dom.Text
import com.example.trainexplore.entities.Acessibilidade

class AcessibilidadeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acessibilidade_detalhes)

        val estacaoId = intent.getIntExtra("estacao_id", -1)

        if (estacaoId != -1){
            val dao = AppDatabase.getDatabase(this).acessibilidadeDao()

            dao.getAcessibilidadeByEstacao(estacaoId).observe(this) {infoAcessibilidadeList ->
                val textViews = listOf(
                    findViewById<TextView>(R.id.acessibilidadeInfo1),
                    findViewById<TextView>(R.id.acessibilidadeInfo2),
                    findViewById<TextView>(R.id.acessibilidadeInfo3),
                    findViewById<TextView>(R.id.acessibilidadeInfo4)
                )

                infoAcessibilidadeList.forEachIndexed { index, info ->
                    textViews[index].text = info
                    textViews[index].visibility = View.VISIBLE
                }
            }
        } else {
            findViewById<TextView>(R.id.mensagemErroTextView).apply {
                text = getString(R.string.acessibilidade_invalida)
                visibility = View.VISIBLE
            }
        }
    }
}