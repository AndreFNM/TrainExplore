package com.example.trainexplore.dao


import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.Update
import androidx.lifecycle.LiveData
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.Relation
import androidx.room.Transaction
import com.example.trainexplore.entities.Acess
import com.example.trainexplore.entities.Acessibilidade
import com.example.trainexplore.entities.Estacao

@Dao
interface AcessibilidadeDao {
        @Transaction
        @Query("SELECT * FROM Acessibilidade WHERE id= :estacaoId")
        fun getAcessibilidadeByEstacao(estacaoId: Int): LiveData<List<EstacaoComAcessibilidade>>
}

data class EstacaoComAcessibilidade(
        @Embedded val estacao: Estacao,
        @Relation(
                parentColumn = "id",
                entityColumn = "id",
                associateBy = Junction(
                        value = Acess::class,
                        parentColumn = "id_estacao",
                        entityColumn = "id_acessibilidade"
                )
        )
        val acessibilidadeDao: List<Acessibilidade>
)