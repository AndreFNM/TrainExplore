package com.example.trainexplore.loginSystem

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.trainexplore.R
import com.example.trainexplore.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RegistarActivity : AppCompatActivity() {

    private lateinit var repository: UtilizadorRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registo)

        val db = AppDatabase.getDatabase(this)
        repository = UtilizadorRepository(db)

        val nomeInput = findViewById<EditText>(R.id.inserir_nome)
        val emailInput = findViewById<EditText>(R.id.inserir_email)
        val passInput = findViewById<EditText>(R.id.inserir_pass)
        val registoButton = findViewById<Button>(R.id.register_button)



        passInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                val passwordErrorTextView = findViewById<TextView>(R.id.passwordErrorText)
                if (s != null && !isValidPassword(s.toString())) {
                    passwordErrorTextView.text = "Password tem de conter pelo menos uma letra minúscula, uma maiúscula, um caractere especial, e tem de ter pelo menos 5 caracteres."
                    passwordErrorTextView.visibility = View.VISIBLE
                } else {
                    passwordErrorTextView.visibility = View.GONE
                }
            }
        })


        registoButton.setOnClickListener {
            val nome = nomeInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val pass = passInput.text.toString().trim()

            if (nome.isNotEmpty() && isValidEmail(email)) {
                if (!isValidPassword(pass)) {
                    Toast.makeText(
                        this@RegistarActivity,
                        "Password tem de conter pelo menos uma letra minúscula, uma maiúscula, um caractere especial, e tem de ter pelo menos 5 caracteres.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        if (repository.nomeExists(nome)) {
                            runOnUiThread {
                                Toast.makeText(this@RegistarActivity, "Nome já utilizado.", Toast.LENGTH_LONG).show()
                            }
                        } else if (repository.emailExists(email)) {
                            runOnUiThread {
                                Toast.makeText(this@RegistarActivity, "Email já utilizado.", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val result = repository.registoUtilizador(nome, email, pass)
                            if (result > 0) {
                                runOnUiThread {
                                    val intent = Intent(this@RegistarActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@RegistarActivity, "Ocorreu um erro no registo. Por favor tente outra vez.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this@RegistarActivity,
                    "Verifique se todos os campos estão preenchidos corretamente e tente novamente.",
                    Toast.LENGTH_LONG
                ).show()
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
