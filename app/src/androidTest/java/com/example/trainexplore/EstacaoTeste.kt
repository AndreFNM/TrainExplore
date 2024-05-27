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
        // Ver se a informação sobre a estação é mostrada
        onView(withId(R.id.nomeViewEstacao)).check(matches(isDisplayed()))
        onView(withId(R.id.imagemViewEstacao)).check(matches(isDisplayed()))
    }

    @Test
    fun testMostrarHistoricoButton() {
        // verificar o intent quande se clica no botão mostrar buttonMostrarHistorico
        onView(withId(R.id.buttonMostrarHistorico)).perform(click())
        intended(hasComponent(Estacao_historico::class.java.name))
    }

    @Test
    fun testMostrarAcessibilidadeButton() {
        // realizar um click no botão buttonMostrarAcessibilidade e verificar o intent
        onView(withId(R.id.buttonMostrarAcessibilidade)).perform(click())
        intended(hasComponent(AcessibilidadeActivity::class.java.name))
    }

    @Test
    fun testAddEstacaoFavButton() {
        // realizar um click no botão addEstacaoFavButton e verificar se apareceu um Toas
        onView(withId(R.id.addEstacaoFavButton)).perform(click())
    }
}