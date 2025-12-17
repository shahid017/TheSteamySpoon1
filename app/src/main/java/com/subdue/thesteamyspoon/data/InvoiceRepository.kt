package com.subdue.thesteamyspoon.data

import kotlinx.coroutines.flow.Flow

class InvoiceRepository(private val invoiceDao: InvoiceDao) {
    fun getRecentInvoices(limit: Int = 50): Flow<List<Invoice>> = invoiceDao.getRecentInvoices(limit)
    
    suspend fun getInvoiceById(id: Long): Invoice? = invoiceDao.getInvoiceById(id)
    
    suspend fun insertInvoice(invoice: Invoice): Long = invoiceDao.insertInvoice(invoice)
    
    suspend fun deleteInvoice(invoice: Invoice) = invoiceDao.deleteInvoice(invoice)
}

