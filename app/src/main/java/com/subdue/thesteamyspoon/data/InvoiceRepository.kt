package com.subdue.thesteamyspoon.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailySalesSummary(
    val date: String,
    val totalSales: Double,
    val orderCount: Int
)

class InvoiceRepository(private val invoiceDao: InvoiceDao) {
    fun getRecentInvoices(limit: Int = 50): Flow<List<Invoice>> = invoiceDao.getRecentInvoices(limit)
    
    fun getAllInvoices(): Flow<List<Invoice>> = invoiceDao.getAllInvoices()
    
    fun getDailySalesSummary(): Flow<List<DailySalesSummary>> {
        return getAllInvoices().map { invoices ->
            // Group invoices by date
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            
            invoices.groupBy { invoice ->
                try {
                    // Parse the dateTime string and extract just the date part
                    val dateTime = dateTimeFormat.parse(invoice.dateTime)
                    dateFormat.format(dateTime ?: Date())
                } catch (e: Exception) {
                    // Fallback: try to extract date part from string
                    invoice.dateTime.split(",").firstOrNull()?.trim() ?: "Unknown"
                }
            }.map { (date, dayInvoices) ->
                DailySalesSummary(
                    date = date,
                    totalSales = dayInvoices.sumOf { it.grandTotal },
                    orderCount = dayInvoices.size
                )
            }.sortedByDescending { summary ->
                try {
                    dateFormat.parse(summary.date)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
        }
    }
    
    suspend fun getInvoiceById(id: Long): Invoice? = invoiceDao.getInvoiceById(id)
    
    suspend fun getMaxBillNumber(): Int = invoiceDao.getMaxBillNumber() ?: 0
    
    suspend fun insertInvoice(invoice: Invoice): Long = invoiceDao.insertInvoice(invoice)
    
    suspend fun deleteInvoice(invoice: Invoice) = invoiceDao.deleteInvoice(invoice)
}

