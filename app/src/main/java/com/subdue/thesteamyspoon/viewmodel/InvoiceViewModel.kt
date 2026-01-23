package com.subdue.thesteamyspoon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subdue.thesteamyspoon.data.DailySalesSummary
import com.subdue.thesteamyspoon.data.InvoiceRepository
import com.subdue.thesteamyspoon.data.WeekdaySalesPoint
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

    enum class AnalyticsPeriod(val label: String, val days: Int?) {
        ALL("All time", null),
        LAST_30("Last 30 days", 30),
        LAST_7("Last 7 days", 7)
    }

    private val _analyticsPeriod = MutableStateFlow(AnalyticsPeriod.ALL)
    val analyticsPeriod: StateFlow<AnalyticsPeriod> = _analyticsPeriod.asStateFlow()

    val weekdaySales: StateFlow<List<WeekdaySalesPoint>> =
        _analyticsPeriod.flatMapLatest { period ->
            repository.getCumulativeSalesByWeekday(period.days)
        }.stateIn(
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

    fun setAnalyticsPeriod(period: AnalyticsPeriod) {
        _analyticsPeriod.value = period
    }
    
    suspend fun getInvoiceById(id: Long) = repository.getInvoiceById(id)
    
    fun deleteInvoice(invoice: com.subdue.thesteamyspoon.data.Invoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }
}

