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
    onNavigateToManageProducts: () -> Unit
) {
    val invoices by invoiceViewModel.invoices.collectAsState()
    val context = LocalContext.current
    val currencyFormat = CurrencyFormatter.getPKRFormatter()
    
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
                                        quantity = itemData.quantity
                                    )
                                }
                                
                                val imageUri = imageGenerator.generateAndShareInvoice(
                                    billItems = billItems,
                                    billNumber = invoice.billNumber,
                                    dateTime = invoice.dateTime,
                                    grandTotal = invoice.grandTotal,
                                    subtotal = invoice.subtotal,
                                    taxRate = invoice.taxRate,
                                    taxAmount = invoice.taxAmount,
                                    discount = invoice.discount
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
                                invoiceViewModel.deleteInvoice(invoice)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(
    invoice: Invoice,
    currencyFormat: NumberFormat,
    onShare: () -> Unit,
    onDelete: () -> Unit
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
