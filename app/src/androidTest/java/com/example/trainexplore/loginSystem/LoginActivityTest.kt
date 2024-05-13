package com.example.trainexplore.loginSystem

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.trainexplore.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {
    @get:Rule
    var activityRule: ActivityScenarioRule<LoginActivity> =
        ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun loginSuccessTest() {
        onView(withId(R.id.email_login)).perform(typeText("test@example.com"))
        onView(withId(R.id.pass_login)).perform(typeText("password"))
        onView(withId(R.id.loginButton)).perform(click())

        onView(withId(R.id.login_activity_root)).check(matches(isDisplayed()))
    }
}
