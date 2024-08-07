package com.example.trainexplore.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.trainexplore.dao.*
import com.example.trainexplore.entities.*

@Database(entities = [Estacao::class, Acessibilidade::class, Favorito::class, Noticia::class,
                     Noticias::class, Ponto_interesse::class, Utilizador::class, Acess::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun estacaoDao(): EstacaoDao
    abstract fun utilizadorDao(): UtilizadorDao
    abstract fun pontoInteressedao(): Ponto_interesseDao
    abstract fun acessibilidadeDao(): AcessibilidadeDao
    abstract fun favoritoDao(): FavoritoDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "TrainExploreDB"
                )
                    .createFromAsset("TrainExplore.db")
                    //.fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}