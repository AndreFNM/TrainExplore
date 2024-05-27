package com.example.trainexplore

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EstacaoTeste {

    @get:Rule
    val intentsTestRule = IntentsTestRule(Estacao::class.java)

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Test
    fun testEstacaoInfoIsDisplayed() {
        // Check if the station information is displayed
        onView(withId(R.id.nomeViewEstacao)).check(matches(isDisplayed()))
        onView(withId(R.id.imagemViewEstacao)).check(matches(isDisplayed()))
    }

    @Test
    fun testMostrarHistoricoButton() {
        // Perform a click on the "Mostrar Historico" button and check the intent
        onView(withId(R.id.buttonMostrarHistorico)).perform(click())
        intended(hasComponent(Estacao_historico::class.java.name))
    }

    @Test
    fun testMostrarAcessibilidadeButton() {
        // Perform a click on the "Mostrar Acessibilidade" button and check the intent
        onView(withId(R.id.buttonMostrarAcessibilidade)).perform(click())
        intended(hasComponent(AcessibilidadeActivity::class.java.name))
    }

    @Test
    fun testAddEstacaoFavButton() {
        // Perform a click on the "Add Estacao Favoritos" button and verify the toast message
        onView(withId(R.id.addEstacaoFavButton)).perform(click())
        // Check if the Toast message is displayed
        // Assuming that the Toast message is displayed, you may need a custom matcher for Toast
    }
}