package com.example.trainexplore.loginSystem

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.trainexplore.database.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class RepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: UtilizadorRepository

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        repository = UtilizadorRepository(database)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertUserAndReadTest() = runBlocking {
        val nome = "Test User"
        val email = "test@example.com"
        val pass = "password123"
        repository.registoUtilizador(nome, email, pass)
        val user = database.utilizadorDao().getUserByEmail(email)
        assertNotNull(user)
        assertEquals(nome, user?.nome)
    }
}
