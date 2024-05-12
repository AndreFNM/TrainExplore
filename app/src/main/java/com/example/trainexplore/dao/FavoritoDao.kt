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
import com.example.trainexplore.entities.Ponto_interesse

@Dao
interface FavoritoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addFavorito(favorito: Favorito)

    @Query("SELECT * FROM Estacao WHERE id IN (SELECT estacaoId FROM Favorito WHERE UtilizadorId = :utilizadorId)")
    fun getFavoritosEstacaoByUtilizador(utilizadorId: Int): LiveData<List<Estacao>>

    @Query("SELECT * FROM Ponto_interesse WHERE id IN (SELECT pontoInteresseId FROM Favorito WHERE utilizadorId = :utilizadorId)")
    fun getFavoritosPontoInteresseByUtilizador(utilizadorId: Int): LiveData<List<Ponto_interesse>>

    @Query("DELETE FROM Favorito WHERE estacaoId = :estacaoId AND utilizadorId = :utilizadorId")
    fun removerFavoritoByEstacaoId(estacaoId: Int, utilizadorId: Int)

    @Query("SELECT * FROM Favorito WHERE utilizadorId =:utilizadorId AND pontoInteresseId =:pontoInteresseId LIMIT 1")
    suspend fun getFavoritoByUtilizadorIdEPontoInteresse(utilizadorId: Int, pontoInteresseId: Int): Favorito?

    @Query("DELETE FROM Favorito WHERE pontoInteresseId = :pontoInteresseId AND utilizadorId = :utilizadorId")
    fun removerFavoritoByPontoInteresseId(pontoInteresseId: Int, utilizadorId: Int)

}