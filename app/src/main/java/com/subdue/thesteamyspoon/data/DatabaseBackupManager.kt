package com.subdue.thesteamyspoon.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages automatic database backups to external storage
 */
object DatabaseBackupManager {
    
    private const val DATABASE_NAME = "restaurant_database"
    private const val BACKUP_RETENTION_DAYS = 30
    
    /**
     * Get the backup directory path
     */
    fun getBackupDirectory(context: Context): File {
        val externalDir = context.getExternalFilesDir("TheSteamySpoon/backups")
        val backupDir = externalDir ?: File(context.filesDir, "TheSteamySpoon/backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        return backupDir
    }
    
    /**
     * Get the database directory path
     */
    private fun getDatabaseDirectory(context: Context): File {
        val externalDir = context.getExternalFilesDir("TheSteamySpoon/database")
        val dbDir = externalDir ?: File(context.filesDir, "TheSteamySpoon/database")
        if (!dbDir.exists()) {
            dbDir.mkdirs()
        }
        return dbDir
    }
    
    /**
     * Create a timestamped backup of the database
     */
    suspend fun backupDatabase(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dbDir = getDatabaseDirectory(context)
                val backupDir = getBackupDirectory(context)
                
                val dbFile = File(dbDir, DATABASE_NAME)
                val walFile = File(dbDir, "$DATABASE_NAME-wal")
                val shmFile = File(dbDir, "$DATABASE_NAME-shm")
                
                // Check if database exists
                if (!dbFile.exists()) {
                    return@withContext false
                }
                
                // Create timestamp for backup
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val backupPrefix = "${DATABASE_NAME}_$timestamp"
                
                // Copy database file
                val backupDbFile = File(backupDir, backupPrefix)
                dbFile.copyTo(backupDbFile, overwrite = true)
                
                // Copy WAL file if exists
                if (walFile.exists()) {
                    val backupWalFile = File(backupDir, "$backupPrefix-wal")
                    walFile.copyTo(backupWalFile, overwrite = true)
                }
                
                // Copy SHM file if exists
                if (shmFile.exists()) {
                    val backupShmFile = File(backupDir, "$backupPrefix-shm")
                    shmFile.copyTo(backupShmFile, overwrite = true)
                }
                
                // Clean old backups
                cleanOldBackups(context)
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    /**
     * Get the most recent backup file
     */
    fun getLatestBackup(context: Context): File? {
        val backupDir = getBackupDirectory(context)
        if (!backupDir.exists()) {
            return null
        }
        
        val backupFiles = backupDir.listFiles { file ->
            file.name.startsWith(DATABASE_NAME) && !file.name.contains("-wal") && !file.name.contains("-shm")
        } ?: return null
        
        return backupFiles.maxByOrNull { it.lastModified() }
    }
    
    /**
     * Restore database from a backup file
     * Note: This will close the current database instance. App restart recommended after restore.
     */
    suspend fun restoreFromBackup(context: Context, backupFile: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dbDir = getDatabaseDirectory(context)
                val dbFile = File(dbDir, DATABASE_NAME)
                
                // Note: Closing database here may cause issues if database is in use
                // In production, this should be called when app is not using the database
                // For now, we'll attempt to close it safely
                try {
                    val database = AppDatabase.getDatabase(context)
                    database.close()
                } catch (e: Exception) {
                    // Database might not be initialized yet or already closed, continue
                    e.printStackTrace()
                }
                
                // Copy backup to database location
                backupFile.copyTo(dbFile, overwrite = true)
                
                // Copy WAL file if exists
                val backupWalFile = File(backupFile.parent, "${backupFile.name}-wal")
                if (backupWalFile.exists()) {
                    val walFile = File(dbDir, "$DATABASE_NAME-wal")
                    backupWalFile.copyTo(walFile, overwrite = true)
                }
                
                // Copy SHM file if exists
                val backupShmFile = File(backupFile.parent, "${backupFile.name}-shm")
                if (backupShmFile.exists()) {
                    val shmFile = File(dbDir, "$DATABASE_NAME-shm")
                    backupShmFile.copyTo(shmFile, overwrite = true)
                }
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    /**
     * Remove backups older than specified days
     */
    fun cleanOldBackups(context: Context, keepDays: Int = BACKUP_RETENTION_DAYS) {
        try {
            val backupDir = getBackupDirectory(context)
            if (!backupDir.exists()) {
                return
            }
            
            val cutoffTime = System.currentTimeMillis() - (keepDays * 24 * 60 * 60 * 1000L)
            
            val backupFiles = backupDir.listFiles { file ->
                file.name.startsWith(DATABASE_NAME)
            } ?: return
            
            backupFiles.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    try {
                        file.delete()
                        // Also delete associated WAL and SHM files
                        File(file.parent, "${file.name}-wal").delete()
                        File(file.parent, "${file.name}-shm").delete()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Check if database was modified since last backup
     */
    fun shouldBackup(context: Context): Boolean {
        try {
            val dbDir = getDatabaseDirectory(context)
            val dbFile = File(dbDir, DATABASE_NAME)
            
            if (!dbFile.exists()) {
                return false
            }
            
            val lastBackup = getLatestBackup(context)
            if (lastBackup == null) {
                return true // No backup exists, should backup
            }
            
            // Backup if database is newer than last backup
            return dbFile.lastModified() > lastBackup.lastModified()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}

