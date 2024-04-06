package com.example.trainexplore.loginSystem

import android.app.Application

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SessionManager.loadSession(this)
    }
}