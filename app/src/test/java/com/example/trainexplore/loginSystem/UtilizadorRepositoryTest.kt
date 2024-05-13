package com.example.trainexplore.loginSystem

import com.example.trainexplore.database.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any

class UtilizadorRepositoryTest {
    @Mock
    private lateinit var database: AppDatabase
    private lateinit var repository: UtilizadorRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = UtilizadorRepository(database)
    }

    @Test
    fun `register user returns correct id`() = runBlocking {
        val nome = "Test User"
        val email = "test@example.com"
        val pass = "Password123."
        whenever(database.utilizadorDao().registerUser(any())).thenReturn(1L)

        val result = repository.registoUtilizador(nome, email, pass)
        assertEquals(1, result)
    }
}
