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
import com.subdue.thesteamyspoon.data.DatabaseBackupManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillViewModel(private val invoiceRepository: InvoiceRepository? = null) : ViewModel() {
    private val _billItems = MutableStateFlow<List<BillItem>>(emptyList())
    val billItems: StateFlow<List<BillItem>> = _billItems.asStateFlow()
    
    private val _billNumber = MutableStateFlow(1)
    val billNumber: StateFlow<Int> = _billNumber.asStateFlow()

    private val _editingInvoiceId = MutableStateFlow<Long?>(null)
    val editingInvoiceId: StateFlow<Long?> = _editingInvoiceId.asStateFlow()

    private val _editingInvoiceDateTime = MutableStateFlow<String?>(null)
    val editingInvoiceDateTime: StateFlow<String?> = _editingInvoiceDateTime.asStateFlow()
    
    init {
        refreshBillNumber()
    }
    
    private val _deliveryCharges = MutableStateFlow(0.0) // No delivery charges by default
    val deliveryCharges: StateFlow<Double> = _deliveryCharges.asStateFlow()
    
    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()
    
    private val _houseNumber = MutableStateFlow<String?>(null)
    val houseNumber: StateFlow<String?> = _houseNumber.asStateFlow()
    
    private val _block = MutableStateFlow<String?>(null)
    val block: StateFlow<String?> = _block.asStateFlow()
    
    private val _phoneNumber = MutableStateFlow<String?>(null)
    val phoneNumber: StateFlow<String?> = _phoneNumber.asStateFlow()
    
    // Derived StateFlows that automatically update when billItems, deliveryCharges, or discount change
    val subtotal: StateFlow<Double> = _billItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    val grandTotal: StateFlow<Double> = combine(subtotal, _deliveryCharges, _discount) { sub, delivery, disc ->
        sub + delivery - disc
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    fun setDeliveryCharges(charges: Double) {
        _deliveryCharges.value = charges
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
        _deliveryCharges.value = 0.0
        _discount.value = 0.0
        _houseNumber.value = null
        _block.value = null
        _phoneNumber.value = null
        _editingInvoiceId.value = null
        _editingInvoiceDateTime.value = null
    }
    
    private fun incrementBillNumber() {
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
        deliveryCharges: Double,
        discount: Double,
        grandTotal: Double,
        houseNumber: String? = null,
        block: String? = null,
        phoneNumber: String? = null,
        editingInvoiceId: Long? = null,
        context: android.content.Context? = null,
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
                    id = editingInvoiceId ?: 0L,
                    billNumber = billNumber,
                    dateTime = dateTime,
                    subtotal = subtotal,
                    taxRate = 0.0, // Not used, kept for compatibility
                    taxAmount = 0.0, // Not used, kept for compatibility
                    deliveryCharges = deliveryCharges,
                    discount = discount,
                    grandTotal = grandTotal,
                    billItems = billItemData,
                    houseNumber = houseNumber,
                    block = block,
                    phoneNumber = phoneNumber
                )
                
                repository.insertInvoice(invoice)
                if (editingInvoiceId == null) {
                    incrementBillNumber()
                } else {
                    _editingInvoiceId.value = null
                    _editingInvoiceDateTime.value = null
                    refreshBillNumber()
                }
                // Create backup after successful save
                context?.let {
                    DatabaseBackupManager.backupDatabase(it)
                }
                onSuccess()
            }
        } ?: run {
            // If no repository, still increment for consistency
            onSuccess()
        }
    }

    fun loadInvoiceForEditing(invoiceId: Long) {
        invoiceRepository?.let { repository ->
            viewModelScope.launch {
                val invoice = repository.getInvoiceById(invoiceId) ?: return@launch
                _editingInvoiceId.value = invoice.id
                _editingInvoiceDateTime.value = invoice.dateTime
                _billNumber.value = invoice.billNumber
                _deliveryCharges.value = invoice.deliveryCharges
                _discount.value = invoice.discount
                _houseNumber.value = invoice.houseNumber
                _block.value = invoice.block
                _phoneNumber.value = invoice.phoneNumber
                _billItems.value = invoice.billItems.map { convertToBillItem(it) }
            }
        }
    }

    private fun refreshBillNumber() {
        invoiceRepository?.let { repository ->
            viewModelScope.launch {
                val maxBillNumber = repository.getMaxBillNumber()
                _billNumber.value = maxBillNumber + 1
            }
        }
    }

    private fun convertToBillItem(data: BillItemData): BillItem {
        val product = Product(
            id = data.productId,
            name = data.productName,
            pricePerServing = data.pricePerServing,
            defaultServing = data.defaultServing,
            defaultPieces = data.defaultPieces,
            description = data.productDescription,
            category = ""
        )
        return BillItem(
            product = product,
            quantity = data.quantity,
            addOns = data.addOns
        )
    }
}

