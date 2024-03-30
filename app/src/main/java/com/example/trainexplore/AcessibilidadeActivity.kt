package com.example.trainexplore

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.trainexplore.database.AppDatabase
import org.w3c.dom.Text

class AcessibilidadeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acessibilidade_detalhes)

        val estacaoId = intent.getIntExtra("estacao_id", -1)

        


    }
}