package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy
import com.example.trainexplore.entities.Acessibilidade

@Dao
interface AcessibilidadeDao {


        //Inserir favorito
        @Insert(onConflict = OnConflictStrategy.ABORT)
        suspend fun insertAcessibilidade(acessibilidade: Acessibilidade): Long

        @Update
        suspend fun updateAcessibilidade(acessibilidade: Acessibilidade)

        //apagar um elemento de acessibilidade especifica
        @Query("DELETE FROM Acessibilidade WHERE id= :id")
        suspend fun deleteAcessibilidade(id: Int)




}