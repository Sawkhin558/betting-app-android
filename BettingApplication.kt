package com.example.bettingapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BettingApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-level components here
    }
}