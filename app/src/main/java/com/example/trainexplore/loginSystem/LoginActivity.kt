package com.example.trainexplore.loginSystem

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.entities.Utilizador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

class LoginActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = AppDatabase.getDatabase(applicationContext)
    }

    suspend fun registar(nome: String, email: String, password: String, imageUrl: String?) {
        withContext(Dispatchers.IO) {
            val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
            val novoUtilizador = Utilizador(0, nome, email, passwordHash, imageUrl)
            database.utilizadorDao().registerUser(novoUtilizador)
        }
    }

    suspend fun validacaoLogin(email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val utilizador = database.utilizadorDao().getUserByEmail(email)
        utilizador?.let { BCrypt.checkpw(password, it.pass) } ?: false
    }
}
