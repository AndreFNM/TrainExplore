package com.example.trainexplore.loginSystem

import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.entities.Utilizador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

class UtilizadorRepository (private val db: AppDatabase) {
    suspend fun registoUtilizador(nome: String, email: String, pass: String): Long {
        val hashedPass = BCrypt.hashpw(pass, BCrypt.gensalt())
        val novoUtilizador = Utilizador(id = 0, nome = nome, email = email, pass = hashedPass, imageUrl = null)
        return withContext(Dispatchers.IO) {
            db.utilizadorDao().registerUser(novoUtilizador)
        }
    }

    suspend fun validarUtilizador(email: String, pass: String): Boolean = withContext(Dispatchers.IO) {
        val utilizador = db.utilizadorDao().getUserByEmail(email)
        utilizador?.let {
            BCrypt.checkpw(pass, it.pass)
        }?: false
    }
}