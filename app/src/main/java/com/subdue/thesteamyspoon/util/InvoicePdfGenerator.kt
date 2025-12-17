package com.subdue.thesteamyspoon.util

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.subdue.thesteamyspoon.model.BillItem
import java.io.File
import java.io.FileOutputStream
import com.subdue.thesteamyspoon.util.CurrencyFormatter
import java.text.NumberFormat

class InvoicePdfGenerator(private val context: Context) {
    private val restaurantName = "The Steamy Spoon"
    private val pageWidth = 595 // A4 width in points (8.27 inches * 72)
    private val pageHeight = 842 // A4 height in points (11.69 inches * 72)
    private val margin = 50f
    private val lineHeight = 20f
    
    fun generateInvoicePdf(
        billItems: List<BillItem>,
        billNumber: Int,
        dateTime: String,
        grandTotal: Double,
        subtotal: Double,
        taxRate: Double,
        taxAmount: Double,
        discount: Double
    ): Uri? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
            }
            
            val currencyFormat = CurrencyFormatter.getPKRFormatter()
            var yPos = margin
            
            // Restaurant Name
            paint.apply {
                textSize = 28f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                color = android.graphics.Color.BLACK
            }
            val restaurantNameWidth = paint.measureText(restaurantName)
            canvas.drawText(restaurantName, (pageWidth - restaurantNameWidth) / 2, yPos, paint)
            yPos += lineHeight * 2
            
            // Bill Number and Date
            paint.apply {
                textSize = 12f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                color = android.graphics.Color.GRAY
            }
            val billInfo = "Invoice #$billNumber | $dateTime"
            val billInfoWidth = paint.measureText(billInfo)
            canvas.drawText(billInfo, (pageWidth - billInfoWidth) / 2, yPos, paint)
            yPos += lineHeight * 2
            
            // Divider line
            paint.apply {
                color = android.graphics.Color.BLACK
                strokeWidth = 2f
            }
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paint)
            yPos += lineHeight * 1.5f
            
            // Headers
            paint.apply {
                textSize = 14f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                color = android.graphics.Color.BLACK
            }
            canvas.drawText("Item", margin, yPos, paint)
            canvas.drawText("Servings", margin + 180, yPos, paint)
            canvas.drawText("Pieces", margin + 250, yPos, paint)
            canvas.drawText("Price", margin + 320, yPos, paint)
            canvas.drawText("Total", pageWidth - margin - 100, yPos, paint)
            yPos += lineHeight * 1.5f
            
            // Divider line
            paint.apply {
                strokeWidth = 1f
            }
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paint)
            yPos += lineHeight * 1.5f
            
            // Bill Items
            paint.apply {
                textSize = 12f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                color = android.graphics.Color.BLACK
            }
            
            billItems.forEach { item ->
                val product = item.product
                
                // Item name (may wrap to multiple lines)
                val itemName = product.name
                val itemNameWidth = paint.measureText(itemName)
                if (itemNameWidth > 180) {
                    // Split long names
                    val words = itemName.split(" ")
                    var currentLine = ""
                    words.forEach { word ->
                        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (paint.measureText(testLine) > 180) {
                            if (currentLine.isNotEmpty()) {
                                canvas.drawText(currentLine, margin, yPos, paint)
                                yPos += lineHeight
                            }
                            currentLine = word
                        } else {
                            currentLine = testLine
                        }
                    }
                    if (currentLine.isNotEmpty()) {
                        canvas.drawText(currentLine, margin, yPos, paint)
                    }
                } else {
                    canvas.drawText(itemName, margin, yPos, paint)
                }
                
                // Servings
                val servingsText = item.quantity.toString()
                canvas.drawText(servingsText, margin + 180, yPos, paint)
                
                // Pieces (quantity * defaultPieces)
                val pieces = item.quantity * product.defaultPieces
                val piecesText = pieces.toString()
                canvas.drawText(piecesText, margin + 250, yPos, paint)
                
                // Price per serving
                canvas.drawText(currencyFormat.format(product.pricePerServing), margin + 320, yPos, paint)
                
                // Total
                val totalText = currencyFormat.format(item.totalPrice)
                val totalWidth = paint.measureText(totalText)
                canvas.drawText(totalText, pageWidth - margin - totalWidth, yPos, paint)
                
                yPos += lineHeight * 1.5f
            }
            
            yPos += lineHeight
            
            // Divider line
            paint.apply {
                strokeWidth = 2f
            }
            canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paint)
            yPos += lineHeight * 1.5f
            
            // Summary Section
            paint.apply {
                textSize = 12f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                color = android.graphics.Color.BLACK
            }
            
            // Subtotal
            if (subtotal > 0) {
                val subtotalText = "Subtotal: ${currencyFormat.format(subtotal)}"
                val subtotalWidth = paint.measureText(subtotalText)
                canvas.drawText(subtotalText, pageWidth - margin - subtotalWidth, yPos, paint)
                yPos += lineHeight * 1.5f
            }
            
            // Tax
            if (taxRate > 0 && taxAmount > 0) {
                val taxText = "Tax ${taxRate.toInt()}%: ${currencyFormat.format(taxAmount)}"
                val taxWidth = paint.measureText(taxText)
                canvas.drawText(taxText, pageWidth - margin - taxWidth, yPos, paint)
                yPos += lineHeight * 1.5f
            }
            
            // Discount
            if (discount > 0) {
                val discountText = "Discount: ${currencyFormat.format(discount)}"
                val discountWidth = paint.measureText(discountText)
                canvas.drawText(discountText, pageWidth - margin - discountWidth, yPos, paint)
                yPos += lineHeight * 1.5f
            }
            
            // Calculate and show total servings and pieces
            val totalServings = billItems.sumOf { it.quantity }
            val totalPieces = billItems.sumOf { it.quantity * it.product.defaultPieces }
            
            if (totalServings > 0) {
                val servingsText = "Total Servings: $totalServings"
                val servingsWidth = paint.measureText(servingsText)
                canvas.drawText(servingsText, pageWidth - margin - servingsWidth, yPos, paint)
                yPos += lineHeight * 1.5f
            }
            
            if (totalPieces > 0) {
                val piecesText = "Total Pieces: $totalPieces"
                val piecesWidth = paint.measureText(piecesText)
                canvas.drawText(piecesText, pageWidth - margin - piecesWidth, yPos, paint)
                yPos += lineHeight * 1.5f
            }
            
            // Divider line before total
            if (subtotal > 0 && (taxRate > 0 || discount > 0 || totalServings > 0 || totalPieces > 0)) {
                paint.strokeWidth = 1f
                canvas.drawLine(margin, yPos, pageWidth - margin, yPos, paint)
                yPos += lineHeight
            }
            
            // Grand Total
            paint.apply {
                textSize = 16f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                color = android.graphics.Color.BLACK
            }
            val grandTotalText = "Grand Total: ${currencyFormat.format(grandTotal)}"
            val grandTotalWidth = paint.measureText(grandTotalText)
            canvas.drawText(grandTotalText, pageWidth - margin - grandTotalWidth, yPos, paint)
            yPos += lineHeight * 2
            
            // Thank you message
            paint.apply {
                textSize = 14f
                typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                color = android.graphics.Color.GRAY
            }
            val thankYouText = "Thank you for your order!"
            val thankYouWidth = paint.measureText(thankYouText)
            canvas.drawText(thankYouText, (pageWidth - thankYouWidth) / 2, yPos, paint)
            
            pdfDocument.finishPage(page)
            
            // Save PDF to file
            val pdfFile = savePdfToFile(pdfDocument, billNumber)
            pdfDocument.close()
            
            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun savePdfToFile(pdfDocument: PdfDocument, billNumber: Int): Uri? {
        return try {
            val pdfsDir = File(context.cacheDir, "invoices")
            if (!pdfsDir.exists()) {
                pdfsDir.mkdirs()
            }
            
            val pdfFile = File(pdfsDir, "invoice_$billNumber.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

