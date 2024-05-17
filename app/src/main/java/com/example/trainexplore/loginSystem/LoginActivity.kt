package com.example.trainexplore.loginSystem

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.trainexplore.MainActivity
import com.example.trainexplore.R
import com.example.trainexplore.database.AppDatabase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var repository: UtilizadorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val registerButton = findViewById<Button>(R.id.registerButton)
        val emailInput = findViewById<EditText>(R.id.email_login)
        val passInput = findViewById<EditText>(R.id.pass_login)
        val loginButton = findViewById<Button>(R.id.loginButton)

        val db = AppDatabase.getDatabase(this)
        repository = UtilizadorRepository(db)

        SessionManager.loadSession(this)

        // Verifica se o id da sessão do utilizador existe na base de dados
        lifecycleScope.launch {
            val sessionUserId = SessionManager.userId
            if (sessionUserId != null) {
                val userExists = repository.checkUserExists(sessionUserId.toInt())
                if (userExists) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    SessionManager.clearSession(this@LoginActivity)
                }
            }
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passInput.text.toString().trim()
            loginUtilizador(email, password)
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegistarActivity::class.java))
        }
    }

    private fun loginUtilizador(loginField: String, password: String) {
        lifecycleScope.launch {
            val userId = repository.validarUtilizador(loginField, password)
            if (userId != null) {
                SessionManager.saveSessionData(userId.toString(), this@LoginActivity)
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Credenciais de Login inválidas", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
