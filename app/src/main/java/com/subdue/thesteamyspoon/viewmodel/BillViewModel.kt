package com.subdue.thesteamyspoon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subdue.thesteamyspoon.data.BillItemData
import com.subdue.thesteamyspoon.data.Invoice
import com.subdue.thesteamyspoon.data.InvoiceRepository
import com.subdue.thesteamyspoon.data.Product
import com.subdue.thesteamyspoon.model.BillItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillViewModel(private val invoiceRepository: InvoiceRepository? = null) : ViewModel() {
    private val _billItems = MutableStateFlow<List<BillItem>>(emptyList())
    val billItems: StateFlow<List<BillItem>> = _billItems.asStateFlow()
    
    private val _billNumber = MutableStateFlow(1)
    val billNumber: StateFlow<Int> = _billNumber.asStateFlow()
    
    init {
        // Initialize bill number from database
        invoiceRepository?.let { repository ->
            viewModelScope.launch {
                val maxBillNumber = repository.getMaxBillNumber()
                _billNumber.value = maxBillNumber + 1
            }
        }
    }
    
    private val _taxRate = MutableStateFlow(0.0) // No tax by default
    val taxRate: StateFlow<Double> = _taxRate.asStateFlow()
    
    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()
    
    private val _houseNumber = MutableStateFlow<String?>(null)
    val houseNumber: StateFlow<String?> = _houseNumber.asStateFlow()
    
    private val _block = MutableStateFlow<String?>(null)
    val block: StateFlow<String?> = _block.asStateFlow()
    
    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> = _phoneNumber.asStateFlow()
    
    // Derived StateFlows that automatically update when billItems, taxRate, or discount change
    val subtotal: StateFlow<Double> = _billItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    val taxAmount: StateFlow<Double> = combine(subtotal, _taxRate) { sub, rate ->
        if (rate > 0) sub * (rate / 100.0) else 0.0
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    val grandTotal: StateFlow<Double> = combine(subtotal, taxAmount, _discount) { sub, tax, disc ->
        sub + tax - disc
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    fun setTaxRate(rate: Double) {
        _taxRate.value = rate
    }
    
    fun setDiscount(amount: Double) {
        _discount.value = amount
    }
    
    fun setHouseNumber(houseNumber: String?) {
        _houseNumber.value = houseNumber
    }
    
    fun setBlock(block: String?) {
        _block.value = block
    }
    
    fun setPhoneNumber(phoneNumber: String?) {
        _phoneNumber.value = phoneNumber
    }
    
    fun addProductToBill(product: Product) {
        val currentItems = _billItems.value.toMutableList()
        val existingItemIndex = currentItems.indexOfFirst { it.product.id == product.id }
        
        if (existingItemIndex >= 0) {
            // Increment quantity if product already exists
            val existingItem = currentItems[existingItemIndex]
            currentItems[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + product.defaultServing)
        } else {
            // Add new item with default serving
            currentItems.add(BillItem(product, product.defaultServing))
        }
        
        _billItems.value = currentItems
    }
    
    fun updateQuantity(productId: Long, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeProductFromBill(productId)
            return
        }
        
        val currentItems = _billItems.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.product.id == productId }
        
        if (itemIndex >= 0) {
            // Create a new BillItem with updated quantity to trigger recomposition
            val existingItem = currentItems[itemIndex]
            currentItems[itemIndex] = existingItem.copy(quantity = newQuantity)
            _billItems.value = currentItems
        }
    }
    
    fun removeProductFromBill(productId: Long) {
        val currentItems = _billItems.value.toMutableList()
        currentItems.removeAll { it.product.id == productId }
        _billItems.value = currentItems
    }
    
    fun updateAddOns(productId: Long, addOns: List<String>) {
        val currentItems = _billItems.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.product.id == productId }
        
        if (itemIndex >= 0) {
            val existingItem = currentItems[itemIndex]
            currentItems[itemIndex] = existingItem.copy(addOns = addOns)
            _billItems.value = currentItems
        }
    }
    
    fun clearBill() {
        _billItems.value = emptyList()
        _houseNumber.value = null
        _block.value = null
        _phoneNumber.value = null
    }
    
    fun incrementBillNumber() {
        _billNumber.value = _billNumber.value + 1
    }
    
    fun getBillDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }
    
    fun saveInvoice(
        billItems: List<BillItem>,
        billNumber: Int,
        dateTime: String,
        subtotal: Double,
        taxRate: Double,
        taxAmount: Double,
        discount: Double,
        grandTotal: Double,
        houseNumber: String? = null,
        block: String? = null,
        phoneNumber: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        invoiceRepository?.let { repository ->
            viewModelScope.launch {
                val billItemData = billItems.map { item ->
                    BillItemData(
                        productId = item.product.id,
                        productName = item.product.name,
                        productDescription = item.product.description,
                        pricePerServing = item.product.pricePerServing,
                        quantity = item.quantity,
                        defaultServing = item.product.defaultServing,
                        defaultPieces = item.product.defaultPieces,
                        totalPrice = item.totalPrice,
                        addOns = item.addOns
                    )
                }
                
                val invoice = Invoice(
                    billNumber = billNumber,
                    dateTime = dateTime,
                    subtotal = subtotal,
                    taxRate = taxRate,
                    taxAmount = taxAmount,
                    discount = discount,
                    grandTotal = grandTotal,
                    billItems = billItemData,
                    houseNumber = houseNumber,
                    block = block,
                    phoneNumber = phoneNumber
                )
                
                repository.insertInvoice(invoice)
                // Increment bill number after successful save
                incrementBillNumber()
                onSuccess()
            }
        } ?: run {
            // If no repository, still increment for consistency
            incrementBillNumber()
            onSuccess()
        }
    }
}

