package com.subdue.thesteamyspoon.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.subdue.thesteamyspoon.model.BillItem
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Invoice Image Generator using Bitmap + Canvas
 * Generates PNG images for sharing via WhatsApp and other apps
 * 
 * STRICTLY FORBIDDEN: Any use of PdfDocument or PDF APIs
 */
class InvoiceImageGenerator(private val context: Context) {
    private val restaurantName = "The Steamy Spoon"
    
    // Image dimensions
    private val imageWidth = 1080
    private val margin = 60f
    private val padding = 40f
    
    // Font sizes as specified
    private val titleFontSize = 48f
    private val normalFontSize = 32f
    private val totalFontSize = 40f
    
    // Spacing
    private val lineSpacing = 50f
    private val itemSpacing = 45f
    private val dividerSpacing = 30f
    
    /**
     * Generate invoice bitmap image
     */
    fun generateInvoiceBitmap(
        billItems: List<BillItem>,
        billNumber: Int,
        dateTime: String,
        grandTotal: Double,
        subtotal: Double,
        deliveryCharges: Double,
        discount: Double,
        houseNumber: String? = null,
        block: String? = null,
        phoneNumber: String? = null
    ): Bitmap {
        // Calculate dynamic height
        val height = calculateImageHeight(billItems, subtotal, deliveryCharges, discount, houseNumber, block, phoneNumber)
        
        // Create bitmap
        val bitmap = Bitmap.createBitmap(imageWidth, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // White background
        canvas.drawColor(Color.WHITE)
        
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        var yPos = padding + titleFontSize
        
        // Restaurant Name (Title - Bold, 48f)
        paint.apply {
            textSize = titleFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        val restaurantNameWidth = paint.measureText(restaurantName)
        canvas.drawText(restaurantName, (imageWidth - restaurantNameWidth) / 2, yPos, paint)
        yPos += lineSpacing * 1.5f
        
        // Invoice Number (Normal - 32f)
        paint.apply {
            textSize = normalFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.BLACK
        }
        val invoiceNumberText = "Invoice #$billNumber"
        val invoiceNumberWidth = paint.measureText(invoiceNumberText)
        canvas.drawText(invoiceNumberText, (imageWidth - invoiceNumberWidth) / 2, yPos, paint)
        yPos += lineSpacing
        
        // Date (Normal - 32f)
        val dateWidth = paint.measureText(dateTime)
        canvas.drawText(dateTime, (imageWidth - dateWidth) / 2, yPos, paint)
        yPos += lineSpacing
        
        // Customer Info (if provided)
        if (!houseNumber.isNullOrBlank() || !block.isNullOrBlank() || !phoneNumber.isNullOrBlank()) {
            val customerInfo = buildString {
                if (!block.isNullOrBlank()) {
                    append(block)
                }
                if (!houseNumber.isNullOrBlank()) {
                    if (isNotEmpty()) append(", ")
                    append("House #$houseNumber")
                }
            }
            if (customerInfo.isNotEmpty()) {
                val customerInfoWidth = paint.measureText(customerInfo)
                canvas.drawText(customerInfo, (imageWidth - customerInfoWidth) / 2, yPos, paint)
                yPos += lineSpacing
            }
            
            // Phone Number (separate line)
            if (!phoneNumber.isNullOrBlank()) {
                val phoneText = "Phone: $phoneNumber"
                val phoneWidth = paint.measureText(phoneText)
                canvas.drawText(phoneText, (imageWidth - phoneWidth) / 2, yPos, paint)
                yPos += lineSpacing
            }
        }
        
        yPos += dividerSpacing
        
        // Divider line
        paint.apply {
            color = Color.BLACK
            strokeWidth = 3f
        }
        canvas.drawLine(margin, yPos, imageWidth - margin, yPos, paint)
        yPos += dividerSpacing
        
        // Item list header
        paint.apply {
            textSize = normalFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        
        val currencyFormat = CurrencyFormatter.getPKRFormatter()
        
        // Draw items
        billItems.forEach { item ->
            val product = item.product
            
            // Item name (left aligned)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(product.name, margin, yPos, paint)
            
            // Quantity with unit (center-left)
            val servings = item.quantity
            val pieces = item.quantity * product.defaultPieces
            val qtyText = "$servings serving${if (servings != 1) "s" else ""} ($pieces pieces)"
            val qtyX = margin + 400f
            canvas.drawText(qtyText, qtyX, yPos, paint)
            
            // Price (right aligned)
            val priceText = currencyFormat.format(item.totalPrice)
            val priceWidth = paint.measureText(priceText)
            canvas.drawText(priceText, imageWidth - margin - priceWidth, yPos, paint)
            
            yPos += itemSpacing
            
            // Add-ons (if any)
            if (item.addOns.isNotEmpty()) {
                paint.apply {
                    textSize = normalFontSize * 0.85f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    color = Color.GRAY
                }
                val addOnsText = "  Add-ons: ${item.addOns.joinToString(", ")}"
                canvas.drawText(addOnsText, margin + 20f, yPos, paint)
                
                // Add-on prices
                val addOnPrice = item.addOnPrice
                if (addOnPrice > 0) {
                    val addOnPriceText = currencyFormat.format(addOnPrice)
                    val addOnPriceWidth = paint.measureText(addOnPriceText)
                    canvas.drawText(addOnPriceText, imageWidth - margin - addOnPriceWidth, yPos, paint)
                }
                
                yPos += itemSpacing * 0.7f
                
                // Reset paint for next item
                paint.apply {
                    textSize = normalFontSize
                    color = Color.BLACK
                }
            }
        }
        
        yPos += dividerSpacing
        
        // Divider line
        paint.apply {
            strokeWidth = 3f
        }
        canvas.drawLine(margin, yPos, imageWidth - margin, yPos, paint)
        yPos += dividerSpacing
        
        // Summary section
        paint.apply {
            textSize = normalFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.BLACK
        }
        
        // Subtotal
        if (subtotal > 0) {
            val subtotalText = "Subtotal: ${currencyFormat.format(subtotal)}"
            val subtotalWidth = paint.measureText(subtotalText)
            canvas.drawText(subtotalText, imageWidth - margin - subtotalWidth, yPos, paint)
            yPos += lineSpacing
        }
        
        // Delivery Charges
        if (deliveryCharges > 0) {
            val deliveryText = "Delivery Charges: ${currencyFormat.format(deliveryCharges)}"
            val deliveryWidth = paint.measureText(deliveryText)
            canvas.drawText(deliveryText, imageWidth - margin - deliveryWidth, yPos, paint)
            yPos += lineSpacing
        }
        
        // Discount
        if (discount > 0) {
            val discountText = "Discount: ${currencyFormat.format(discount)}"
            val discountWidth = paint.measureText(discountText)
            canvas.drawText(discountText, imageWidth - margin - discountWidth, yPos, paint)
            yPos += lineSpacing
        }
        
        // Total servings and pieces
        val totalServings = billItems.sumOf { it.quantity }
        val totalPieces = billItems.sumOf { it.quantity * it.product.defaultPieces }
        
        if (totalServings > 0) {
            val servingsText = "Total Servings: $totalServings"
            val servingsWidth = paint.measureText(servingsText)
            canvas.drawText(servingsText, imageWidth - margin - servingsWidth, yPos, paint)
            yPos += lineSpacing
        }
        
        if (totalPieces > 0) {
            val piecesText = "Total Pieces: $totalPieces"
            val piecesWidth = paint.measureText(piecesText)
            canvas.drawText(piecesText, imageWidth - margin - piecesWidth, yPos, paint)
            yPos += lineSpacing
        }
        
        yPos += dividerSpacing
        
        // Grand Total (Bold, 40f)
        paint.apply {
            textSize = totalFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        val grandTotalText = "Total: ${currencyFormat.format(grandTotal)}"
        val grandTotalWidth = paint.measureText(grandTotalText)
        canvas.drawText(grandTotalText, imageWidth - margin - grandTotalWidth, yPos, paint)
        yPos += lineSpacing * 1.5f
        
        // Thank you message (Normal - 32f)
        paint.apply {
            textSize = normalFontSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.GRAY
        }
        val thankYouText = "Thank you for your order!"
        val thankYouWidth = paint.measureText(thankYouText)
        canvas.drawText(thankYouText, (imageWidth - thankYouWidth) / 2, yPos, paint)
        
        return bitmap
    }
    
    /**
     * Calculate dynamic image height based on content
     */
    private fun calculateImageHeight(
        billItems: List<BillItem>,
        subtotal: Double,
        deliveryCharges: Double,
        discount: Double,
        houseNumber: String? = null,
        block: String? = null,
        phoneNumber: String? = null
    ): Int {
        var height = padding.toInt() // Top padding
        
        // Restaurant name
        height += titleFontSize.toInt() + lineSpacing.toInt() * 2
        
        // Invoice number and date
        height += normalFontSize.toInt() * 2 + lineSpacing.toInt()
        
        // Customer info (if provided)
        if (!houseNumber.isNullOrBlank() || !block.isNullOrBlank()) {
            height += normalFontSize.toInt() + lineSpacing.toInt()
        }
        if (!phoneNumber.isNullOrBlank()) {
            height += normalFontSize.toInt() + lineSpacing.toInt()
        }
        
        // Divider
        height += dividerSpacing.toInt() * 2
        
        // Items (each item takes itemSpacing, plus extra for add-ons if present)
        billItems.forEach { item ->
            height += itemSpacing.toInt()
            if (item.addOns.isNotEmpty()) {
                height += (itemSpacing * 0.7f).toInt()
            }
        }
        
        // Divider
        height += dividerSpacing.toInt() * 2
        
        // Summary lines
        var summaryLines = 0
        if (subtotal > 0) summaryLines++
        if (deliveryCharges > 0) summaryLines++
        if (discount > 0) summaryLines++
        summaryLines += 2 // Total servings and pieces
        height += summaryLines * lineSpacing.toInt()
        
        // Grand total
        height += totalFontSize.toInt() + lineSpacing.toInt()
        
        // Thank you message
        height += normalFontSize.toInt() + padding.toInt()
        
        return height
    }
    
    /**
     * Save invoice image to file
     */
    fun saveInvoiceImage(
        bitmap: Bitmap,
        billNumber: Int
    ): File? {
        return try {
            val imagesDir = File(context.cacheDir, "invoice_images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            // Timestamped filename
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFile = File(imagesDir, "invoice_${billNumber}_$timestamp.png")
            
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            
            imageFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Share invoice image
     */
    fun shareInvoiceImage(file: File): Uri? {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Generate and share invoice image (convenience method)
     */
    fun generateAndShareInvoice(
        billItems: List<BillItem>,
        billNumber: Int,
        dateTime: String,
        grandTotal: Double,
        subtotal: Double,
        deliveryCharges: Double,
        discount: Double,
        houseNumber: String? = null,
        block: String? = null,
        phoneNumber: String? = null
    ): Uri? {
        val bitmap = generateInvoiceBitmap(
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
        
        val file = saveInvoiceImage(bitmap, billNumber) ?: return null
        return shareInvoiceImage(file)
    }
}

