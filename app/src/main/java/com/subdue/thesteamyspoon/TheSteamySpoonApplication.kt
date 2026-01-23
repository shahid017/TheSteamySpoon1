package com.subdue.thesteamyspoon

import android.app.Application
import com.subdue.thesteamyspoon.data.DatabaseBackupManager
import com.subdue.thesteamyspoon.data.WalRecoveryUtil
import com.subdue.thesteamyspoon.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TheSteamySpoonApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        AppContainer.initialize(this)
        
        // Create backup on app startup if database was modified
        applicationScope.launch {
            if (DatabaseBackupManager.shouldBackup(this@TheSteamySpoonApplication)) {
                DatabaseBackupManager.backupDatabase(this@TheSteamySpoonApplication)
            }
        }
        
        // Attempt WAL recovery from old location (experimental)
        applicationScope.launch {
            WalRecoveryUtil.attemptRecoveryFromOldLocation(this@TheSteamySpoonApplication)
        }
    }
}

