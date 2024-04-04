package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import androidx.room.Transaction
import com.example.trainexplore.entities.Estacao
import com.example.trainexplore.entities.Favorito

@Dao
interface FavoritoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addFavorito(favorito: Favorito)

    @Query("SELECT * FROM Estacao WHERE id IN (SELECT estacaoId FROM Favorito WHERE UtilizadorId = :utilizadorId)")
    fun getFavoritosEstacaoByUtilizador(utilizadorId: Int): LiveData<List<Estacao>>

    // Para quando adicionar os pontos de interesse
    //@Query("SELECT * FROM Ponto_interesse WHERE id IN (SELECT pontointeresseId FROM Favorito WHERE UtilizadorId = :userId)")
    // fun getFavoritePointsByUser(userId: Int): LiveData<List<Ponto_de_Interesse>>


}