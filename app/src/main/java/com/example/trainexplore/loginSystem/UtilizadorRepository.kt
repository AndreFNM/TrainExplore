package com.example.trainexplore.loginSystem

import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.entities.Utilizador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt

 class UtilizadorRepository(private val db: AppDatabase) {
        suspend fun registoUtilizador(nome: String, email: String, pass: String): Long {
            val hashedPass = BCrypt.hashpw(pass, BCrypt.gensalt())
            val novoUtilizador = Utilizador(id = 0, nome = nome, email = email, pass = hashedPass)
            if (nomeExists(nome) || emailExists(email)) {
                return -1 // Indica falha devido à já existência de um nome de utilizador ou email
            }
            return withContext(Dispatchers.IO) {
                db.utilizadorDao().registerUser(novoUtilizador)
            }
        }

        suspend fun nomeExists(nome: String): Boolean = withContext(Dispatchers.IO) {
            db.utilizadorDao().getUserByNome(nome) != null
        }

        suspend fun validarUtilizador(loginField: String, pass: String): Int? = withContext(Dispatchers.IO) {
            val utilizador = db.utilizadorDao().getUserByEmail(loginField) ?: db.utilizadorDao().getUserByNome(loginField)
            if (utilizador != null && BCrypt.checkpw(pass, utilizador.pass)) {
                return@withContext utilizador.id // return ID on successful login
            } else {
                return@withContext null // login failed
            }
        }


    //Fragment Perfil
    suspend fun getUtilizadorById(id: Int): Utilizador? = withContext(Dispatchers.IO) {
        db.utilizadorDao().getUserById(id)
    }

    suspend fun updateUtilizador(utilizador: Utilizador): Int = withContext(Dispatchers.IO) {
        db.utilizadorDao().updateUser(utilizador)
    }

    suspend fun emailExists(email: String): Boolean {
        return db.utilizadorDao().getUserByEmail(email) != null
    }

}