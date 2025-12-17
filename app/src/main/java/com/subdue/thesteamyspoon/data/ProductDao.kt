package com.subdue.thesteamyspoon.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>
    
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long
    
    @Update
    suspend fun updateProduct(product: Product)
    
    @Delete
    suspend fun deleteProduct(product: Product)
}

