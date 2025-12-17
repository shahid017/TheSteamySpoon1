package com.subdue.thesteamyspoon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subdue.thesteamyspoon.data.InvoiceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {
    val invoices: StateFlow<List<com.subdue.thesteamyspoon.data.Invoice>> = 
        repository.getRecentInvoices(50)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    suspend fun getInvoiceById(id: Long) = repository.getInvoiceById(id)
    
    fun deleteInvoice(invoice: com.subdue.thesteamyspoon.data.Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }
}

