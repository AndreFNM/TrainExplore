package com.example.trainexplore.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.trainexplore.dao.*
import com.example.trainexplore.dao.Acess
import com.example.trainexplore.entities.*

@Database(entities = [Estacao::class, Acess::class, Acessibilidade::class, Fav::class, Favorito::class, Noticia::class,
                     Noticias::class, Ponto_interesse::class, Utilizador::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun Estacao(): EstacaoDao
    // adicionar outras estações aqui

    abstract fun Utilizador(): UtilizadorDao
    abstract fun Ponto_interesse(): Ponto_interesseDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "TrainExploreDB"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}

