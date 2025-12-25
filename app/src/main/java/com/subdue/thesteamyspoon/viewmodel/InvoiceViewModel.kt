package com.subdue.thesteamyspoon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subdue.thesteamyspoon.data.DailySalesSummary
import com.subdue.thesteamyspoon.data.InvoiceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InvoiceViewModel(private val repository: InvoiceRepository) : ViewModel() {
    val invoices: StateFlow<List<com.subdue.thesteamyspoon.data.Invoice>> = 
        repository.getRecentInvoices(50)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    val dailySalesSummary: StateFlow<List<DailySalesSummary>> =
        repository.getDailySalesSummary()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    
    private val _selectedProductIds = MutableStateFlow<Set<Long>?>(null)
    val selectedProductIds: StateFlow<Set<Long>?> = _selectedProductIds.asStateFlow()
    
    val itemSales: StateFlow<List<com.subdue.thesteamyspoon.data.ItemSalesData>> = 
        _selectedProductIds.flatMapLatest { selectedIds ->
            repository.getItemSales(selectedIds)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    init {
        // Initialize with all products (null = show all)
        _selectedProductIds.value = null
    }
    
    fun setSelectedProducts(productIds: Set<Long>?) {
        _selectedProductIds.value = productIds
    }
    
    suspend fun getInvoiceById(id: Long) = repository.getInvoiceById(id)
    
    fun deleteInvoice(invoice: com.subdue.thesteamyspoon.data.Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }
}

