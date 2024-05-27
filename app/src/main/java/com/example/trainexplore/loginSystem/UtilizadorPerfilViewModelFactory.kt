package com.example.trainexplore.loginSystem

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.trainexplore.database.AppDatabase

class UtilizadorPerfilViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UtilizadorPerfilViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UtilizadorPerfilViewModel(application, AppDatabase.getDatabase(application)) as T
        }
        throw IllegalArgumentException("ViewModel class não conhecida")
    }
}
