package com.subdue.thesteamyspoon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.subdue.thesteamyspoon.data.ItemSalesData
import com.subdue.thesteamyspoon.data.Product
import com.subdue.thesteamyspoon.di.AppContainer
import com.subdue.thesteamyspoon.util.CurrencyFormatter
import com.subdue.thesteamyspoon.viewmodel.InvoiceViewModel
import com.subdue.thesteamyspoon.viewmodel.ProductViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    invoiceViewModel: InvoiceViewModel = viewModel(factory = AppContainer.viewModelFactory),
    productViewModel: ProductViewModel = viewModel(factory = AppContainer.viewModelFactory),
    onNavigateBack: () -> Unit
) {
    val itemSales by invoiceViewModel.itemSales.collectAsState()
    val products by productViewModel.products.collectAsState()
    val selectedProductIds by invoiceViewModel.selectedProductIds.collectAsState()
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    
    var showFilterDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Sales") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Text("🔍", fontSize = 20.sp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary info
            if (selectedProductIds != null && selectedProductIds!!.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Showing ${selectedProductIds!!.size} selected item(s)",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            if (itemSales.isEmpty()) {
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
                            text = "No sales data",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Sales data will appear here once you create invoices",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(itemSales) { itemSalesData ->
                        ItemSalesCard(
                            itemSalesData = itemSalesData,
                            currencyFormat = currencyFormat
                        )
                    }
                }
            }
        }
    }
    
    // Filter Dialog
    if (showFilterDialog) {
        ItemFilterDialog(
            products = products,
            selectedProductIds = selectedProductIds,
            onDismiss = { showFilterDialog = false },
            onConfirm = { selectedIds ->
                invoiceViewModel.setSelectedProducts(selectedIds)
                showFilterDialog = false
            }
        )
    }
}

@Composable
fun ItemSalesCard(
    itemSalesData: ItemSalesData,
    currencyFormat: NumberFormat
) {
    var expanded by remember { mutableStateOf(false) }
    
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = itemSalesData.productName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Servings: ${itemSalesData.totalServings} | Total Pieces: ${itemSalesData.totalPieces}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Total Sales: ${currencyFormat.format(itemSalesData.totalSales)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                IconButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) "▼" else "▶",
                        fontSize = 16.sp
                    )
                }
            }
            
            if (expanded && itemSalesData.dailySales.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    text = "Daily Sales:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                itemSalesData.dailySales.forEach { dailySale ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = dailySale.date,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Servings: ${dailySale.servings} | Pieces: ${dailySale.pieces}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = currencyFormat.format(dailySale.sales),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFilterDialog(
    products: List<Product>,
    selectedProductIds: Set<Long>?,
    onDismiss: () -> Unit,
    onConfirm: (Set<Long>?) -> Unit
) {
    var selectedIds by remember { 
        mutableStateOf(selectedProductIds?.toSet() ?: emptySet<Long>())
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Items to View") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { 
                            selectedIds = products.map { it.id }.toSet()
                        }
                    ) {
                        Text("Select All")
                    }
                    TextButton(
                        onClick = { 
                            selectedIds = emptySet()
                        }
                    ) {
                        Text("Clear All")
                    }
                }
                
                Divider()
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(products) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedIds.contains(product.id),
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) {
                                        selectedIds + product.id
                                    } else {
                                        selectedIds - product.id
                                    }
                                }
                            )
                            Text(
                                text = product.name,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onConfirm(if (selectedIds.isEmpty()) null else selectedIds)
                    }
                ) {
                    Text("Apply")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onConfirm(null)
                }
            ) {
                Text("Show All")
            }
        }
    )
}

