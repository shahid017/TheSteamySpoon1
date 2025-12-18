package com.subdue.thesteamyspoon.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY id DESC LIMIT :limit")
    fun getRecentInvoices(limit: Int = 50): Flow<List<Invoice>>
    
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<Invoice>>
    
    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): Invoice?
    
    @Query("SELECT MAX(billNumber) FROM invoices")
    suspend fun getMaxBillNumber(): Int?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long
    
    @Delete
    suspend fun deleteInvoice(invoice: Invoice)
}

