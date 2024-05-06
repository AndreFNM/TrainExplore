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

class UtilizadorPerfilViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UtilizadorRepository(AppDatabase.getDatabase(application))

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
                    _errorMessages.value = "User not found."
                }
            } catch (e: Exception) {
                _errorMessages.value = "Failed to load user data."
            }
        }
    }

    fun updateUserData(userId: Int, name: String, password: String?) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null) {
                val userToUpdate = if (password.isNullOrEmpty()) {
                    user.copy(nome = name)
                } else {
                    val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                    user.copy(nome = name, pass = hashedPassword)
                }
                val result = userRepository.updateUtilizador(userToUpdate)
                if (result > 0) {
                    _currentUser.value = userToUpdate
                } else {
                    _errorMessages.value = "Failed to update user data."
                }
            } else {
                _errorMessages.value = "No user data available to update."
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
                        _passwordUpdateResult.postValue("Password updated successfully")
                    } else {
                        _passwordUpdateResult.postValue("Failed to update password")
                    }
                } else {
                    _passwordUpdateResult.postValue("New password cannot be empty")
                }
            } else {
                _passwordUpdateResult.postValue("Current password is incorrect")
            }
        }
    }


}
