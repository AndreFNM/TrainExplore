package com.example.trainexplore.loginSystem

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.example.trainexplore.loginSystem.LoginActivity
import com.example.trainexplore.database.AppDatabase
import com.example.trainexplore.entities.Utilizador
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(LoginActivity::class.java)

    @Test
    fun testLoginSuccess() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getDatabase(context)
        val repository = UtilizadorRepository(db)

        val testEmail = "test@test.com"
        val testPassword = "Test@123"

        val user = Utilizador(0, "Test User", testEmail, testPassword)
        db.utilizadorDao().registerUser(user)

        val userId = repository.validarUtilizador(testEmail, testPassword)
        assertNotNull(userId)
    }

    @Test
    fun testLoginFailure() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getDatabase(context)
        val repository = UtilizadorRepository(db)

        val testEmail = "nonexistent@test.com"
        val testPassword = "WrongPass"

        val userId = repository.validarUtilizador(testEmail, testPassword)
        assert(userId == null)
    }
}