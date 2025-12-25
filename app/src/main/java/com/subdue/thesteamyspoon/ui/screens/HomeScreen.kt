package com.subdue.thesteamyspoon.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.subdue.thesteamyspoon.data.Invoice
import com.subdue.thesteamyspoon.di.AppContainer
import com.subdue.thesteamyspoon.util.InvoiceImageGenerator
import com.subdue.thesteamyspoon.util.InvoicePdfGenerator
import com.subdue.thesteamyspoon.viewmodel.InvoiceViewModel
import com.subdue.thesteamyspoon.util.CurrencyFormatter
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    invoiceViewModel: InvoiceViewModel = viewModel(factory = AppContainer.viewModelFactory),
    onNavigateToCreateInvoice: () -> Unit,
    onNavigateToManageProducts: () -> Unit,
    onNavigateToSalesSummary: () -> Unit,
    onNavigateToInvoiceDetail: (Long) -> Unit = {}
) {
    val invoices by invoiceViewModel.invoices.collectAsState()
    val context = LocalContext.current
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    var invoiceToDelete by remember { mutableStateOf<Invoice?>(null) }
    
    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Recent Invoices",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onNavigateToCreateInvoice,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Generate New Invoice",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onNavigateToSalesSummary,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "View Sales Summary",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            // Invoice List
            if (invoices.isEmpty()) {
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
                            text = "No invoices yet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap the + button to create your first invoice",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(invoices) { invoice ->
                        InvoiceCard(
                            invoice = invoice,
                            currencyFormat = currencyFormat,
                            onClick = {
                                onNavigateToInvoiceDetail(invoice.id)
                            },
                            onShare = {
                                val imageGenerator = InvoiceImageGenerator(context)
                                val billItems = invoice.billItems.map { itemData ->
                                    // Convert BillItemData back to BillItem for image generation
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
                                    billNumber = invoice.billNumber,
                                    dateTime = invoice.dateTime,
                                    grandTotal = invoice.grandTotal,
                                    subtotal = invoice.subtotal,
                                    deliveryCharges = invoice.deliveryCharges,
                                    discount = invoice.discount,
                                    houseNumber = invoice.houseNumber,
                                    block = invoice.block,
                                    phoneNumber = invoice.phoneNumber
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
                            onDelete = {
                                invoiceToDelete = invoice
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    invoiceToDelete?.let { invoice ->
        AlertDialog(
            onDismissRequest = { invoiceToDelete = null },
            title = { Text("Delete Invoice") },
            text = { 
                Text("Are you sure you want to delete Invoice #${invoice.billNumber}? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        invoiceViewModel.deleteInvoice(invoice)
                        invoiceToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { invoiceToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InvoiceCard(
    invoice: Invoice,
    currencyFormat: NumberFormat,
    onClick: () -> Unit = {},
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Invoice #${invoice.billNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = invoice.dateTime,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "${invoice.billItems.size} items",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = currencyFormat.format(invoice.grandTotal),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = onShare,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text("Share")
                        }
                        TextButton(
                            onClick = onDelete,
                            modifier = Modifier.padding(top = 4.dp),
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
    }
}
