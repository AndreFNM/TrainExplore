package com.example.trainexplore.loginSystem

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.trainexplore.R
import com.example.trainexplore.database.AppDatabase
import kotlinx.coroutines.launch

class RegistarActivity : AppCompatActivity() {

    private lateinit var repository : UtilizadorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registo)

        val db = AppDatabase.getDatabase(this)
        repository = UtilizadorRepository(db)

        val nomeInput = findViewById<EditText>(R.id.inserir_nome)
        val emailInput = findViewById<EditText>(R.id.inserir_email)
        val passInput = findViewById<EditText>(R.id.inserir_pass)
        val registoButton = findViewById<Button>(R.id.register_button)

        registoButton.setOnClickListener {
            val nome = nomeInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val pass = passInput.text.toString().trim()

            if (nome.isNotEmpty() && isValidEmail(email) && isValidPassword(pass)) {
                lifecycleScope.launch {
                    val result = repository.registoUtilizador(nome, email, pass)
                    if (result > 0) {
                        val intent = Intent(this@RegistarActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@RegistarActivity, "Ocorreu um erro no registo. Por favor tente outra vez.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this@RegistarActivity, "Por favor insira um email válido.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[\\W_])(?=\\S+$).{5,}$"
        return password.matches(passwordPattern.toRegex())
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
        return email.matches(emailPattern.toRegex())
    }
}
