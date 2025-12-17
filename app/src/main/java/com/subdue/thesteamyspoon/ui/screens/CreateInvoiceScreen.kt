package com.subdue.thesteamyspoon.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.subdue.thesteamyspoon.data.Product
import com.subdue.thesteamyspoon.di.AppContainer
import com.subdue.thesteamyspoon.model.BillItem
import com.subdue.thesteamyspoon.util.BillImageGenerator
import com.subdue.thesteamyspoon.util.CurrencyFormatter
import com.subdue.thesteamyspoon.util.InvoiceImageGenerator
import com.subdue.thesteamyspoon.util.InvoicePdfGenerator
import com.subdue.thesteamyspoon.viewmodel.BillViewModel
import com.subdue.thesteamyspoon.viewmodel.ProductViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    productViewModel: ProductViewModel = viewModel(factory = AppContainer.viewModelFactory),
    billViewModel: BillViewModel = viewModel(factory = AppContainer.viewModelFactory),
    onNavigateBack: () -> Unit,
    onInvoiceGenerated: () -> Unit
) {
    val products by productViewModel.products.collectAsState()
    val billItems by billViewModel.billItems.collectAsState()
    val billNumber by billViewModel.billNumber.collectAsState()
    val taxRate by billViewModel.taxRate.collectAsState()
    val discount by billViewModel.discount.collectAsState()
    val context = LocalContext.current
    
    var showAddItemsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    val subtotal by billViewModel.subtotal.collectAsState()
    val taxAmount by billViewModel.taxAmount.collectAsState()
    val grandTotal by billViewModel.grandTotal.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Sales Invoice") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    TextButton(onClick = { showSettingsDialog = true }) {
                        Text("Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Bill Items Section
            items(billItems) { item ->
                BillItemCard(
                    item = item,
                    onQuantityChange = { newQty ->
                        billViewModel.updateQuantity(item.product.id, newQty)
                    },
                    onRemove = {
                        billViewModel.removeProductFromBill(item.product.id)
                    }
                )
            }
            
            // Add Items Section
            item {
                AddItemsCard(
                    onClick = { showAddItemsDialog = true }
                )
            }
            
            // Summary Section
            item {
                SummarySection(
                    billItems = billItems,
                    subtotal = subtotal,
                    taxRate = taxRate,
                    taxAmount = taxAmount,
                    discount = discount,
                    grandTotal = grandTotal,
                    onDiscountChange = { billViewModel.setDiscount(it) },
                    onTaxRateChange = { billViewModel.setTaxRate(it) },
                    currencyFormat = currencyFormat
                )
            }
            
            // Generate Invoice Button
            item {
                    Button(
                        onClick = {
                            val dateTime = billViewModel.getBillDate()
                            
                            // Generate invoice image (PNG) using Bitmap + Canvas
                            val imageGenerator = InvoiceImageGenerator(context)
                            val imageUri = imageGenerator.generateAndShareInvoice(
                                billItems = billItems,
                                billNumber = billNumber,
                                dateTime = dateTime,
                                grandTotal = grandTotal,
                                subtotal = subtotal,
                                taxRate = taxRate,
                                taxAmount = taxAmount,
                                discount = discount
                            )
                            
                            // Save invoice to database
                            billViewModel.saveInvoice(
                                billItems = billItems,
                                billNumber = billNumber,
                                dateTime = dateTime,
                                subtotal = subtotal,
                                taxRate = taxRate,
                                taxAmount = taxAmount,
                                discount = discount,
                                grandTotal = grandTotal
                            )
                            
                            imageUri?.let { uri ->
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    type = "image/png"
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
                            }
                            
                            // Clear bill and navigate back
                            billViewModel.clearBill()
                            onInvoiceGenerated()
                        },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = billItems.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A1B9A) // Purple color
                    )
                ) {
                    Text(
                        "Generate Invoice",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    
    // Add Items Dialog
    if (showAddItemsDialog) {
        AddItemsDialog(
            products = products,
            onDismiss = { showAddItemsDialog = false },
            onProductSelected = { product ->
                billViewModel.addProductToBill(product)
                showAddItemsDialog = false
            }
        )
    }
    
    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            taxRate = taxRate,
            discount = discount,
            onDismiss = { showSettingsDialog = false },
            onTaxRateChange = { billViewModel.setTaxRate(it) },
            onDiscountChange = { billViewModel.setDiscount(it) },
                    onManageProducts = {
                        showSettingsDialog = false
                        // Note: Product management is now in drawer
                    }
        )
    }
}

@Composable
fun BillItemCard(
    item: BillItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    var quantityText by remember { mutableStateOf(item.quantity.toString()) }
    
    // Sync quantityText when item.quantity changes externally
    LaunchedEffect(item.quantity) {
        quantityText = item.quantity.toString()
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4A148C) // Dark purple
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7B1FA2)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.product.name.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Product Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (item.product.description.isNotEmpty()) {
                    Text(
                        text = item.product.description,
                        fontSize = 14.sp,
                        color = Color(0xFFE1BEE7),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Servings: ${item.quantity}",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Pieces: ${item.quantity * item.product.defaultPieces}",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
                Text(
                    text = currencyFormat.format(item.product.pricePerServing),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Quantity Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newQty = (quantityText.toIntOrNull() ?: item.quantity) - 1
                        if (newQty > 0) {
                            quantityText = newQty.toString()
                            onQuantityChange(newQty)
                        } else {
                            onRemove()
                        }
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                
                Text(
                    text = quantityText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.width(30.dp),
                    textAlign = TextAlign.Center
                )
                
                IconButton(
                    onClick = {
                        val newQty = (quantityText.toIntOrNull() ?: item.quantity) + 1
                        quantityText = newQty.toString()
                        onQuantityChange(newQty)
                    },
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddItemsCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                2.dp,
                Color.Gray.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "+",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Add Items",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SummarySection(
    billItems: List<BillItem>,
    subtotal: Double,
    taxRate: Double,
    taxAmount: Double,
    discount: Double,
    grandTotal: Double,
    onDiscountChange: (Double) -> Unit,
    onTaxRateChange: (Double) -> Unit,
    currencyFormat: NumberFormat
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryRow(
            label = "Subtotal",
            value = currencyFormat.format(subtotal)
        )
        
        // Only show tax if tax rate is greater than 0
        if (taxRate > 0) {
            SummaryRow(
                label = "Tax ${taxRate.toInt()}%",
                value = currencyFormat.format(taxAmount)
            )
        }
        
        // Only show discount if discount is greater than 0
        if (discount > 0) {
            SummaryRow(
                label = "Discount",
                value = currencyFormat.format(discount)
            )
        }
        
        // Calculate and show total servings and pieces
        val totalServings = billItems.sumOf { it.quantity }
        val totalPieces = billItems.sumOf { it.quantity * it.product.defaultPieces }
        

        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.Gray.copy(alpha = 0.3f)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Amount",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = currencyFormat.format(grandTotal),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AddItemsDialog(
    products: List<Product>,
    onDismiss: () -> Unit,
    onProductSelected: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter products based on search query
    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter { product ->
                product.name.contains(searchQuery, ignoreCase = true) ||
                product.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Product",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Text("×", fontSize = 24.sp)
                    }
                }
                
                HorizontalDivider()
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search products...") },
                    leadingIcon = {
                        Text("🔍", fontSize = 20.sp)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Text("×", fontSize = 20.sp)
                            }
                        }
                    },
                    singleLine = true
                )
                
                HorizontalDivider()
                
                // Products List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductSelectionCard(
                            product = product,
                            onClick = { onProductSelected(product) }
                        )
                    }
                    
                    if (filteredProducts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) {
                                        "No products found for \"$searchQuery\""
                                    } else {
                                        "No products available"
                                    },
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductSelectionCard(
    product: Product,
    onClick: () -> Unit
) {
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (product.description.isNotEmpty()) {
                    Text(
                        text = product.description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = currencyFormat.format(product.pricePerServing),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsDialog(
    taxRate: Double,
    discount: Double,
    onDismiss: () -> Unit,
    onTaxRateChange: (Double) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onManageProducts: () -> Unit
) {
    var taxRateText by remember { mutableStateOf(taxRate.toString()) }
    var discountText by remember { mutableStateOf(discount.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = taxRateText,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            taxRateText = it
                            it.toDoubleOrNull()?.let { rate -> onTaxRateChange(rate) }
                        }
                    },
                    label = { Text("Tax Rate (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            discountText = it
                            it.toDoubleOrNull()?.let { disc -> onDiscountChange(disc) }
                        }
                    },
                    label = { Text("Discount Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Button(
                    onClick = {
                        onManageProducts()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage Products")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
