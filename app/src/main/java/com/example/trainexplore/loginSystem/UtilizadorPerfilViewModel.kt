package com.example.trainexplore.loginSystem

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.entities.Utilizador
import com.example.trainexplore.loginSystem.UtilizadorRepository
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

class UtilizadorPerfilViewModel(application: Application, private val db: AppDatabase) : AndroidViewModel(application) {
    private val userRepository = UtilizadorRepository(db)

    private val _currentUser = MutableLiveData<Utilizador?>()
    val currentUser: LiveData<Utilizador?> = _currentUser

    private val _errorMessages = MutableLiveData<String>()
    val errorMessages: LiveData<String> = _errorMessages

    private val _passwordUpdateResult = MutableLiveData<String>()
    val passwordUpdateResult: LiveData<String> = _passwordUpdateResult



    fun loadUserData(userId: Int) {
        viewModelScope.launch {
            try {
                val user = userRepository.getUtilizadorById(userId)
                _currentUser.value = user
                if (user == null) {
                    _errorMessages.value = "Utilizador não encontrado"
                }
            } catch (e: Exception) {
                _errorMessages.value = "Falha ao carregar os dados do utilizador."
            }
        }
    }

    fun updateUserData(userId: Int, name: String, password: String?) {
        viewModelScope.launch {
            val currentUser = userRepository.getUtilizadorById(userId)
            if (currentUser != null) {
                if (userRepository.nomeExists(name) && name != currentUser.nome) {
                    _errorMessages.postValue("Nome já utilizado. Por favor escolha outro.")
                    return@launch
                }

                val userToUpdate = if (password.isNullOrEmpty()) {
                    currentUser.copy(nome = name)
                } else {
                    val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                    currentUser.copy(nome = name, pass = hashedPassword)
                }
                val result = userRepository.updateUtilizador(userToUpdate)
                if (result > 0) {
                    _currentUser.postValue(userToUpdate)
                } else {
                    _errorMessages.postValue("Falha ao atualizar dados do utilizador.")
                }
            } else {
                _errorMessages.postValue("Nenhum dado de utilizador disponível para atualizar.")
            }
        }
    }




    fun changePassword(userId: Int, currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            val user = userRepository.getUtilizadorById(userId)
            if (user != null && BCrypt.checkpw(currentPassword, user.pass)) {
                if (newPassword.isNotEmpty()) {
                    val hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
                    val updatedUser = user.copy(pass = hashedNewPassword)
                    val result = userRepository.updateUtilizador(updatedUser)
                    if (result > 0) {
                        _passwordUpdateResult.postValue("Password atualizada com sucesso.")
                    } else {
                        _passwordUpdateResult.postValue("Falha ao atualizar a password")
                    }
                } else {
                    _passwordUpdateResult.postValue("Nova password não pode estar vazia")
                }
            } else {
                _passwordUpdateResult.postValue("Password atual incorreta")
            }
        }
    }

    suspend fun nomeExists(nome: String): Boolean {
        return db.utilizadorDao().getUserByNome(nome) != null
    }



}
