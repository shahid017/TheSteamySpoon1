package com.subdue.thesteamyspoon.ui.screens
import androidx.compose.ui.text.input.KeyboardType
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.style.TextOverflow
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
    val deliveryCharges by billViewModel.deliveryCharges.collectAsState()
    val discount by billViewModel.discount.collectAsState()
    val houseNumber by billViewModel.houseNumber.collectAsState()
    val block by billViewModel.block.collectAsState()
    val phoneNumber by billViewModel.phoneNumber.collectAsState()
    val context = LocalContext.current
    
    var showAddItemsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    val blocks = listOf(
        "Shaheen Block",
        "Mehran Block",
        "Nishat Block",
        "Khyber Block",
        "Punjab Block",
        "Bolan Block",
        "Jehlum Block",
        "Kashmir Block",
        "Rachna Block"
    )
    
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    val subtotal by billViewModel.subtotal.collectAsState()
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
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
                    },
                    onAddOnsChange = { addOns ->
                        billViewModel.updateAddOns(item.product.id, addOns)
                    }
                )
            }
            
            // Add Items Section
            item {
                AddItemsCard(
                    onClick = { showAddItemsDialog = true }
                )
            }
            
            // Customer Info Section
            item {
                CustomerInfoSection(
                    houseNumber = houseNumber ?: "",
                    block = block ?: "",
                    phoneNumber = phoneNumber ?: "",
                    blocks = blocks,
                    onHouseNumberChange = { billViewModel.setHouseNumber(it.ifBlank { null }) },
                    onBlockChange = { billViewModel.setBlock(it.ifBlank { null }) },
                    onPhoneNumberChange = { billViewModel.setPhoneNumber(it.ifBlank { null }) }
                )
            }
            
            // Summary Section
            item {
                SummarySection(
                    billItems = billItems,
                    subtotal = subtotal,
                    deliveryCharges = deliveryCharges,
                    discount = discount,
                    grandTotal = grandTotal,
                    onDiscountChange = { billViewModel.setDiscount(it) },
                    onDeliveryChargesChange = { billViewModel.setDeliveryCharges(it) },
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
                                deliveryCharges = deliveryCharges,
                                discount = discount,
                                houseNumber = houseNumber,
                                block = block,
                                phoneNumber = phoneNumber
                            )
                            
                            // Save invoice to database
                            billViewModel.saveInvoice(
                                billItems = billItems,
                                billNumber = billNumber,
                                dateTime = dateTime,
                                subtotal = subtotal,
                                deliveryCharges = deliveryCharges,
                                discount = discount,
                                grandTotal = grandTotal,
                                houseNumber = houseNumber,
                                block = block,
                                phoneNumber = phoneNumber,
                                onSuccess = {
                                    // Clear bill and navigate back after successful save
                                    billViewModel.clearBill()
                                    onInvoiceGenerated()
                                }
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
            deliveryCharges = deliveryCharges,
            discount = discount,
            onDismiss = { showSettingsDialog = false },
            onDeliveryChargesChange = { billViewModel.setDeliveryCharges(it) },
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
    onRemove: () -> Unit,
    onAddOnsChange: (List<String>) -> Unit
) {
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    var quantityText by remember { mutableStateOf(item.quantity.toString()) }
    var showAddOnsDialog by remember { mutableStateOf(false) }
    
    // Sync quantityText when item.quantity changes externally
    LaunchedEffect(item.quantity) {
        quantityText = item.quantity.toString()
    }
    
    // Available add-ons
    val availableAddOns = listOf("Cheese", "Extra Sauce", "Extra Spice", "Extra Toppings")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4A148C) // Dark purple
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // First row: Product name, price, quantity controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product name and price
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${currencyFormat.format(item.product.pricePerServing)} × ${item.quantity}",
                            fontSize = 13.sp,
                            color = Color(0xFFE1BEE7)
                        )
                        Text(
                            text = "•",
                            fontSize = 13.sp,
                            color = Color(0xFFE1BEE7)
                        )
                        Text(
                            text = "${item.quantity} serving${if (item.quantity != 1) "s" else ""} (${item.quantity * item.product.defaultPieces} pcs)",
                            fontSize = 13.sp,
                            color = Color(0xFFE1BEE7)
                        )
                    }
                }
                
                // Quantity Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Text(
                        text = quantityText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = {
                            val newQty = (quantityText.toIntOrNull() ?: item.quantity) + 1
                            quantityText = newQty.toString()
                            onQuantityChange(newQty)
                        },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Second row: Add-ons and total (if add-ons exist)
            if (item.addOns.isNotEmpty() || item.addOnPrice > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showAddOnsDialog = true },
                        modifier = Modifier.padding(0.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFE1BEE7)
                        )
                    ) {
                        Text(
                            text = "Add-ons: ${item.addOns.joinToString(", ")}",
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    if (item.addOnPrice > 0) {
                        Text(
                            text = "+${currencyFormat.format(item.addOnPrice)}",
                            fontSize = 12.sp,
                            color = Color(0xFFE1BEE7)
                        )
                    }
                }
            } else {
                TextButton(
                    onClick = { showAddOnsDialog = true },
                    modifier = Modifier.padding(0.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFE1BEE7)
                    )
                ) {
                    Text(
                        text = "Add Add-ons",
                        fontSize = 11.sp
                    )
                }
            }
            
            // Third row: Total price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier)
                Text(
                    text = "Total: ${currencyFormat.format(item.totalPrice)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
    
    // Add-ons Selection Dialog
    if (showAddOnsDialog) {
        AddOnsDialog(
            availableAddOns = availableAddOns,
            selectedAddOns = item.addOns,
            onDismiss = { showAddOnsDialog = false },
            onConfirm = { selected ->
                onAddOnsChange(selected)
                showAddOnsDialog = false
            }
        )
    }
}

@Composable
fun AddOnsDialog(
    availableAddOns: List<String>,
    selectedAddOns: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    var selected by remember { mutableStateOf(selectedAddOns.toSet()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Add-ons",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                availableAddOns.forEach { addOn ->
                    val isSelected = selected.contains(addOn)
                    val price = if (addOn.equals("Cheese", ignoreCase = true)) 100.0 else 50.0
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (isSelected) {
                                    selected - addOn
                                } else {
                                    selected + addOn
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    selected = if (it) {
                                        selected + addOn
                                    } else {
                                        selected - addOn
                                    }
                                }
                            )
                            Text(
                                text = addOn,
                                fontSize = 16.sp
                            )
                        }
                        Text(
                            text = currencyFormat.format(price),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(selected.toList()) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm")
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerInfoSection(
    houseNumber: String,
    block: String,
    phoneNumber: String,
    blocks: List<String>,
    onHouseNumberChange: (String) -> Unit,
    onBlockChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Customer Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // House Number Field
            OutlinedTextField(
                value = houseNumber,
                onValueChange = onHouseNumberChange,
                label = { Text("House Number") },
                placeholder = { Text("Enter house number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Block Dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = block,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Block") },
                    placeholder = { Text("Select block") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    blocks.forEach { blockOption ->
                        DropdownMenuItem(
                            text = { Text(blockOption) },
                            onClick = {
                                onBlockChange(blockOption)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Phone Number Field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = { Text("Phone Number") },
                placeholder = { Text("Enter phone number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                )
            )
        }
    }
}

@Composable
fun SummarySection(
    billItems: List<BillItem>,
    subtotal: Double,
    deliveryCharges: Double,
    discount: Double,
    grandTotal: Double,
    onDiscountChange: (Double) -> Unit,
    onDeliveryChargesChange: (Double) -> Unit,
    currencyFormat: NumberFormat
) {
    var showDeliveryChargesInput by remember { mutableStateOf(false) }
    var deliveryChargesText by remember { mutableStateOf(deliveryCharges.toString()) }
    
    LaunchedEffect(deliveryCharges) {
        deliveryChargesText = deliveryCharges.toString()
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryRow(
            label = "Subtotal",
            value = currencyFormat.format(subtotal)
        )
        
        // Delivery Charges - editable
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Delivery Charges:",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (showDeliveryChargesInput) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = deliveryChargesText,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() || char == '.' }) {
                                deliveryChargesText = it
                                it.toDoubleOrNull()?.let { charges -> onDeliveryChargesChange(charges) }
                            }
                        },
                        modifier = Modifier.width(100.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    TextButton(onClick = { showDeliveryChargesInput = false }) {
                        Text("Done")
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currencyFormat.format(deliveryCharges),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { showDeliveryChargesInput = true }) {
                        Text("Edit", fontSize = 12.sp)
                    }
                }
            }
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
    deliveryCharges: Double,
    discount: Double,
    onDismiss: () -> Unit,
    onDeliveryChargesChange: (Double) -> Unit,
    onDiscountChange: (Double) -> Unit,
    onManageProducts: () -> Unit
) {
    var deliveryChargesText by remember { mutableStateOf(deliveryCharges.toString()) }
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
                    value = deliveryChargesText,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            deliveryChargesText = it
                            it.toDoubleOrNull()?.let { charges -> onDeliveryChargesChange(charges) }
                        }
                    },
                    label = { Text("Delivery Charges") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
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
