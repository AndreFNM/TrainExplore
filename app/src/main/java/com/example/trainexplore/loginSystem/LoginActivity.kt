
package com.example.trainexplore.loginSystem


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.trainexplore.Comboios
import com.example.trainexplore.MainActivity
import com.example.trainexplore.R
import com.example.trainexplore.database.AppDatabase
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private lateinit var repository : UtilizadorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val paraPaginaRegisto = findViewById<Button>(R.id.registerButton)

        val db = AppDatabase.getDatabase(this)
        repository = UtilizadorRepository(db)

        val emailInput = findViewById<EditText>(R.id.email_login)
        val passInput = findViewById<EditText>(R.id.pass_login)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val pass = passInput.text.toString().trim()

            //Realizar o login
            loginUtilizador(email, pass)
        }

        paraPaginaRegisto.setOnClickListener {
            val intent = Intent(this, RegistarActivity::class.java)
            startActivity(intent)
        }
    }
    private fun loginUtilizador(email: String, pass: String) {
        lifecycleScope.launch {
            if (repository.validarUtilizador(email, pass)) {
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "Email ou Password inválidos", Toast.LENGTH_LONG).show()
            }
        }
    }

}