package com.example.trainexplore.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trainexplore.entities.Estacao

@Database(entities = [Estacao::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun Estacao(): Estacao
    // adicionar outras estações aqui

   
}