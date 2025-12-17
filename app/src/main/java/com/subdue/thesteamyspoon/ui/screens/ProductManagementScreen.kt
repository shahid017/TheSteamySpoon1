package com.subdue.thesteamyspoon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.subdue.thesteamyspoon.data.Product
import com.subdue.thesteamyspoon.di.AppContainer
import com.subdue.thesteamyspoon.viewmodel.ProductViewModel
import com.subdue.thesteamyspoon.util.CurrencyFormatter
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    productViewModel: ProductViewModel = viewModel(factory = AppContainer.viewModelFactory),
    onNavigateBack: () -> Unit
) {
    val products by productViewModel.products.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Products") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "No products yet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap the + button to add your first product",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductManagementCard(
                            product = product,
                            onEdit = { editingProduct = product },
                            onDelete = { productViewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddEditProductDialog(
            product = null,
            onDismiss = { showAddDialog = false },
            onSave = { product ->
                productViewModel.insertProduct(product)
                showAddDialog = false
            }
        )
    }
    
    editingProduct?.let { product ->
        AddEditProductDialog(
            product = product,
            onDismiss = { editingProduct = null },
            onSave = { updatedProduct ->
                productViewModel.updateProduct(updatedProduct)
                editingProduct = null
            }
        )
    }
}

@Composable
fun ProductManagementCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    
    Card(
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Price: ${currencyFormat.format(product.pricePerServing)}",
                    fontSize = 14.sp
                )
                Text(
                    text = "Default Serving ${product.defaultServing}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Default Pieces ${product.defaultPieces}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
fun AddEditProductDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var priceText by remember { mutableStateOf(product?.pricePerServing?.toString() ?: "") }
    var defaultServingText by remember { mutableStateOf(product?.defaultServing?.toString() ?: "1") }
    var defaultPiecesText by remember { mutableStateOf(product?.defaultPieces?.toString() ?: "1") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional, e.g., Less Spicy)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) priceText = it },
                    label = { Text("Price per Serving") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = defaultServingText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) defaultServingText = it },
                        label = { Text("Default Serving") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = defaultPiecesText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) defaultPiecesText = it },
                        label = { Text("Default Pieces") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val defaultServing = defaultServingText.toIntOrNull() ?: 1
                    val defaultPieces = defaultPiecesText.toIntOrNull() ?: 1
                    
                    if (name.isNotBlank() && price > 0) {
                        val productToSave = product?.copy(
                            name = name,
                            pricePerServing = price,
                            defaultServing = defaultServing,
                            defaultPieces = defaultPieces,
                            description = description
                        ) ?: Product(
                            name = name,
                            pricePerServing = price,
                            defaultServing = defaultServing,
                            defaultPieces = defaultPieces,
                            description = description
                        )
                        onSave(productToSave)
                    }
                },
                enabled = name.isNotBlank() && 
                         priceText.toDoubleOrNull() != null && 
                         priceText.toDoubleOrNull()!! > 0 &&
                         defaultServingText.toIntOrNull() != null &&
                         defaultPiecesText.toIntOrNull() != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

