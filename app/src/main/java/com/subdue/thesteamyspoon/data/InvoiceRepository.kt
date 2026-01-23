package com.subdue.thesteamyspoon.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailySalesSummary(
    val date: String,
    val totalSales: Double,
    val orderCount: Int
)

data class ItemSalesData(
    val productName: String,
    val productId: Long,
    val totalServings: Int,
    val totalPieces: Int,
    val totalSales: Double,
    val dailySales: List<DailyItemSales>
)

data class DailyItemSales(
    val date: String,
    val servings: Int,
    val pieces: Int,
    val sales: Double
)

data class WeekdaySalesPoint(
    val weekday: String, // Sunday ... Saturday
    val totalSales: Double
)

class InvoiceRepository(private val invoiceDao: InvoiceDao) {
    fun getRecentInvoices(limit: Int = 50): Flow<List<Invoice>> = invoiceDao.getRecentInvoices(limit)
    
    fun getAllInvoices(): Flow<List<Invoice>> = invoiceDao.getAllInvoices()

    /**
     * Cumulative (summed) sales grouped by weekday across a selected time period.
     *
     * @param periodDays null = all time, otherwise only include invoices whose date is within the last [periodDays].
     */
    fun getCumulativeSalesByWeekday(periodDays: Int? = null): Flow<List<WeekdaySalesPoint>> {
        return getAllInvoices().map { invoices ->
            val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val cal = Calendar.getInstance()

            val cutoffMillis = periodDays?.let {
                Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -it) }.timeInMillis
            }

            val totals = DoubleArray(7) { 0.0 } // 0=Sun ... 6=Sat

            invoices.forEach { invoice ->
                val millis = try {
                    dateTimeFormat.parse(invoice.dateTime)?.time
                } catch (_: Exception) {
                    null
                } ?: return@forEach

                if (cutoffMillis != null && millis < cutoffMillis) return@forEach

                cal.timeInMillis = millis
                val dow = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun ... 7=Sat
                val idx = (dow - Calendar.SUNDAY).coerceIn(0, 6)
                totals[idx] += invoice.grandTotal
            }

            val labels = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
            labels.mapIndexed { i, label -> WeekdaySalesPoint(weekday = label, totalSales = totals[i]) }
        }
    }
    
    fun getDailySalesSummary(): Flow<List<DailySalesSummary>> {
        return getAllInvoices().map { invoices ->
            // Group invoices by date
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            
            invoices.groupBy { invoice ->
                try {
                    // Parse the dateTime string and extract just the date part
                    val dateTime = dateTimeFormat.parse(invoice.dateTime)
                    dateFormat.format(dateTime ?: Date())
                } catch (e: Exception) {
                    // Fallback: try to extract date part from string
                    invoice.dateTime.split(",").firstOrNull()?.trim() ?: "Unknown"
                }
            }.map { (date, dayInvoices) ->
                DailySalesSummary(
                    date = date,
                    totalSales = dayInvoices.sumOf { it.grandTotal },
                    orderCount = dayInvoices.size
                )
            }.sortedByDescending { summary ->
                try {
                    dateFormat.parse(summary.date)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
        }
    }
    
    suspend fun getInvoiceById(id: Long): Invoice? = invoiceDao.getInvoiceById(id)
    
    suspend fun getMaxBillNumber(): Int = invoiceDao.getMaxBillNumber() ?: 0
    
    fun getItemSales(selectedProductIds: Set<Long>? = null): Flow<List<ItemSalesData>> {
        return getAllInvoices().map { invoices ->
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            
            // Map to store item sales: productName -> (productId, dailySales map, totalSales)
            // dailySales map: date -> (servings, pieces, sales)
            data class DailySalesInfo(val servings: Int, val pieces: Int, val sales: Double)
            val itemSalesMap = mutableMapOf<String, Triple<Long, MutableMap<String, DailySalesInfo>, Double>>()
            
            invoices.forEach { invoice ->
                val invoiceDate = try {
                    val dateTime = dateTimeFormat.parse(invoice.dateTime)
                    dateFormat.format(dateTime ?: Date())
                } catch (e: Exception) {
                    invoice.dateTime.split(",").firstOrNull()?.trim() ?: "Unknown"
                }
                
                invoice.billItems.forEach { item ->
                    // Filter by selected products if provided
                    if (selectedProductIds == null || selectedProductIds.contains(item.productId)) {
                        val key = item.productName
                        val current = itemSalesMap[key] ?: Triple(item.productId, mutableMapOf(), 0.0)
                        
                        // Calculate servings and pieces
                        val servings = item.quantity
                        val pieces = item.quantity * item.defaultPieces
                        
                        // Update daily sales
                        val dailySales = current.second
                        val existingDaily = dailySales[invoiceDate] ?: DailySalesInfo(0, 0, 0.0)
                        dailySales[invoiceDate] = DailySalesInfo(
                            servings = existingDaily.servings + servings,
                            pieces = existingDaily.pieces + pieces,
                            sales = existingDaily.sales + item.totalPrice
                        )
                        
                        // Update total
                        itemSalesMap[key] = Triple(
                            current.first,
                            dailySales,
                            current.third + item.totalPrice
                        )
                    }
                }
            }
            
            // Convert to ItemSalesData list and sort by total sales (descending)
            itemSalesMap.map { (productName, data) ->
                val dailySalesList = data.second.map { (date, salesInfo) ->
                    DailyItemSales(
                        date = date,
                        servings = salesInfo.servings,
                        pieces = salesInfo.pieces,
                        sales = salesInfo.sales
                    )
                }.sortedByDescending { 
                    try {
                        dateFormat.parse(it.date)?.time ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                }
                
                ItemSalesData(
                    productName = productName,
                    productId = data.first,
                    totalServings = dailySalesList.sumOf { it.servings },
                    totalPieces = dailySalesList.sumOf { it.pieces },
                    totalSales = data.third,
                    dailySales = dailySalesList
                )
            }.sortedByDescending { it.totalSales } // Sort descending by total sales
        }
    }
    
    suspend fun insertInvoice(invoice: Invoice): Long = invoiceDao.insertInvoice(invoice)
    
    suspend fun deleteInvoice(invoice: Invoice) = invoiceDao.deleteInvoice(invoice)
}

