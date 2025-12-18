package com.subdue.thesteamyspoon.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Product::class, Invoice::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE products ADD COLUMN description TEXT NOT NULL DEFAULT ''")
            }
        }
        
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS invoices (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        billNumber INTEGER NOT NULL,
                        dateTime TEXT NOT NULL,
                        subtotal REAL NOT NULL,
                        taxRate REAL NOT NULL,
                        taxAmount REAL NOT NULL,
                        discount REAL NOT NULL,
                        grandTotal REAL NOT NULL,
                        billItems TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new products table with new structure
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS products_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        pricePerServing REAL NOT NULL,
                        defaultServing INTEGER NOT NULL DEFAULT 1,
                        defaultPieces INTEGER NOT NULL DEFAULT 1,
                        description TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
                
                // Migrate data from old table to new table
                database.execSQL("""
                    INSERT INTO products_new (id, name, pricePerServing, defaultServing, defaultPieces, description)
                    SELECT id, name, pricePerServing, 
                           CASE WHEN defaultQuantity > 0 THEN defaultQuantity ELSE 1 END,
                           CASE WHEN unitLabel LIKE '%piece%' OR unitLabel LIKE '%pieces%' 
                                THEN CAST(SUBSTR(unitLabel, 1, LENGTH(unitLabel) - 7) AS INTEGER)
                                ELSE 1 END,
                           description
                    FROM products
                """.trimIndent())
                
                // Drop old table and rename new table
                database.execSQL("DROP TABLE products")
                database.execSQL("ALTER TABLE products_new RENAME TO products")
            }
        }
        
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add houseNumber and block columns to invoices table
                database.execSQL("ALTER TABLE invoices ADD COLUMN houseNumber TEXT")
                database.execSQL("ALTER TABLE invoices ADD COLUMN block TEXT")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "restaurant_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

