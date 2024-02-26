package com.example.trainexplore.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.trainexplore.dao.*
import com.example.trainexplore.entities.*

@Database(entities = [Estacao::class, Acess::class, Acessibilidade::class, Fav::class, Favorito::class, Noticia::class,
                     Noticias::class, Ponto_interesse::class, Utilizador::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun Estacao(): EstacaoDao
    // adicionar outras estações aqui

    abstract fun Utilizador(): UtilizadorDao
    abstract fun Ponto_interesse(): Ponto_interesseDao

    @Volatile
    private var INSTANCE: AppDatabase? = null


    //Impedir que exista mais de uma instância da base de dados aberta ao mesmo tempo
    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "TrainExploreDB"
            ).build()
            INSTANCE = instance
            instance
        }
    }



}