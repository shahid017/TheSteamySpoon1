package com.subdue.thesteamyspoon

import android.app.Application
import com.subdue.thesteamyspoon.di.AppContainer
import com.subdue.thesteamyspoon.util.DefaultProductsSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TheSteamySpoonApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
        
        // Seed default products - ensures all menu products are present
        applicationScope.launch {
            val seeder = DefaultProductsSeeder(AppContainer.productRepository)
            seeder.seedDefaultProductsSync()
        }
    }
}

