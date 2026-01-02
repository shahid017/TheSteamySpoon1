package com.subdue.thesteamyspoon.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.subdue.thesteamyspoon.data.Invoice
import com.subdue.thesteamyspoon.di.AppContainer
import com.subdue.thesteamyspoon.util.InvoiceImageGenerator
import com.subdue.thesteamyspoon.util.CurrencyFormatter
import com.subdue.thesteamyspoon.viewmodel.InvoiceViewModel
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    invoiceViewModel: InvoiceViewModel = viewModel(factory = AppContainer.viewModelFactory),
    onNavigateBack: () -> Unit,
    onEditInvoice: (Long) -> Unit
) {
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    val context = LocalContext.current
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    
    LaunchedEffect(invoiceId) {
        invoice = invoiceViewModel.getInvoiceById(invoiceId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    invoice?.let { inv ->
                        TextButton(onClick = { onEditInvoice(inv.id) }) {
                            Text("Edit", fontSize = 16.sp)
                        }
                        IconButton(
                            onClick = {
                                val imageGenerator = InvoiceImageGenerator(context)
                                val billItems = inv.billItems.map { itemData ->
                                    com.subdue.thesteamyspoon.model.BillItem(
                                        product = com.subdue.thesteamyspoon.data.Product(
                                            id = itemData.productId,
                                            name = itemData.productName,
                                            description = itemData.productDescription,
                                            pricePerServing = itemData.pricePerServing,
                                            defaultServing = itemData.defaultServing,
                                            defaultPieces = itemData.defaultPieces
                                        ),
                                        quantity = itemData.quantity,
                                        addOns = itemData.addOns
                                    )
                                }
                                
                                val imageUri = imageGenerator.generateAndShareInvoice(
                                    billItems = billItems,
                                    billNumber = inv.billNumber,
                                    dateTime = inv.dateTime,
                                    grandTotal = inv.grandTotal,
                                    subtotal = inv.subtotal,
                                    deliveryCharges = inv.deliveryCharges,
                                    discount = inv.discount,
                                    houseNumber = inv.houseNumber,
                                    block = inv.block,
                                    phoneNumber = inv.phoneNumber
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
                            }
                        ) {
                            Text("Share", fontSize = 16.sp)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val currentInvoice = invoice
        if (currentInvoice == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Invoice Header
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Invoice #${currentInvoice.billNumber}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = currentInvoice.dateTime,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Customer Information (if available)
                if (currentInvoice.houseNumber != null || currentInvoice.block != null || currentInvoice.phoneNumber != null) {
                    item {
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
                                Text(
                                    text = "Customer Information",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (currentInvoice.houseNumber != null) {
                                    Text(
                                        text = "House: ${currentInvoice.houseNumber}",
                                        fontSize = 14.sp
                                    )
                                }
                                if (currentInvoice.block != null) {
                                    Text(
                                        text = "Block: ${currentInvoice.block}",
                                        fontSize = 14.sp
                                    )
                                }
                                if (currentInvoice.phoneNumber != null) {
                                    Text(
                                        text = "Phone: ${currentInvoice.phoneNumber}",
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Items List
                item {
                    Text(
                        text = "Items",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(currentInvoice.billItems) { item ->
                    InvoiceItemCard(
                        item = item,
                        currencyFormat = currencyFormat
                    )
                }
                
                // Summary Section
                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
                
                item {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subtotal:",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = currencyFormat.format(currentInvoice.subtotal),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            if (currentInvoice.discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Discount:",
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "-${currencyFormat.format(currentInvoice.discount)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            if (currentInvoice.deliveryCharges > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Delivery Charges:",
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = currencyFormat.format(currentInvoice.deliveryCharges),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            Divider()
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Grand Total:",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currencyFormat.format(currentInvoice.grandTotal),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
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
fun InvoiceItemCard(
    item: com.subdue.thesteamyspoon.data.BillItemData,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.productName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (item.productDescription.isNotEmpty()) {
                        Text(
                            text = item.productDescription,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val servings = item.quantity
                    val pieces = item.quantity * item.defaultPieces
                    Text(
                        text = "Qty: $servings serving${if (servings != 1) "s" else ""} ($pieces pieces)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.addOns.isNotEmpty()) {
                        Text(
                            text = "Add-ons: ${item.addOns.joinToString(", ")}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = currencyFormat.format(item.totalPrice),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

