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


    @Query("DELETE FROM Favorito WHERE estacaoId = :estacaoId")
    fun removerFavoritoByEstacaoId(estacaoId: Int)

    @Query("SELECT * FROM Favorito WHERE utilizadorId =:utilizadorId AND pontoInteresseId =:pontoInteresseId LIMIT 1")
    suspend fun getFavoritoByUtilizadorIdEPontoInteresse(utilizadorId: Int, pontoInteresseId: Int): Favorito?

}