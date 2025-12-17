package com.subdue.thesteamyspoon

import android.app.Application
import com.subdue.thesteamyspoon.di.AppContainer

class TheSteamySpoonApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
    }
}

