package com.subdue.thesteamyspoon.util

import android.content.Context
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
import java.util.Locale

class BillImageGenerator(private val context: Context) {
    private val restaurantName = "The Steamy Spoon"
    
    fun generateBillImage(
        billItems: List<BillItem>,
        billNumber: Int,
        dateTime: String,
        grandTotal: Double,
        subtotal: Double = 0.0,
        taxRate: Double = 0.0,
        taxAmount: Double = 0.0,
        discount: Double = 0.0
    ): Uri? {
        val width = 800
        val height = calculateHeight(billItems, taxRate > 0 || discount > 0)
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Background
        canvas.drawColor(Color.WHITE)
        
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        var yPos = 80f
        
        // Restaurant Name
        paint.apply {
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        val restaurantNameWidth = paint.measureText(restaurantName)
        canvas.drawText(restaurantName, (width - restaurantNameWidth) / 2, yPos, paint)
        yPos += 60f
        
        // Bill Number and Date
        paint.apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.GRAY
        }
        val billInfo = "Bill #$billNumber | $dateTime"
        val billInfoWidth = paint.measureText(billInfo)
        canvas.drawText(billInfo, (width - billInfoWidth) / 2, yPos, paint)
        yPos += 50f
        
        // Divider line
        paint.apply {
            color = Color.BLACK
            strokeWidth = 2f
        }
        canvas.drawLine(40f, yPos, width - 40f, yPos, paint)
        yPos += 30f
        
        // Headers
        paint.apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        canvas.drawText("Item", 50f, yPos, paint)
        canvas.drawText("Qty", 350f, yPos, paint)
        canvas.drawText("Price", 500f, yPos, paint)
        canvas.drawText("Total", 650f, yPos, paint)
        yPos += 40f
        
        // Divider line
        paint.apply {
            strokeWidth = 1f
        }
        canvas.drawLine(40f, yPos, width - 40f, yPos, paint)
        yPos += 30f
        
        // Bill Items
        paint.apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.BLACK
        }
        
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
        
        billItems.forEach { item ->
            val product = item.product
            
            // Item name
            canvas.drawText(product.name, 50f, yPos, paint)
            
            // Quantity - show servings
            val qtyText = "${item.quantity} serving${if (item.quantity != 1) "s" else ""}"
            canvas.drawText(qtyText, 350f, yPos, paint)
            
            // Price per serving
            canvas.drawText(currencyFormat.format(product.pricePerServing), 500f, yPos, paint)
            
            // Total
            canvas.drawText(currencyFormat.format(item.totalPrice), 650f, yPos, paint)
            
            yPos += 35f
        }
        
        yPos += 20f
        
        // Divider line
        paint.apply {
            strokeWidth = 2f
        }
        canvas.drawLine(40f, yPos, width - 40f, yPos, paint)
        yPos += 30f
        
        // Summary Section
        paint.apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.BLACK
        }
        
        // Subtotal (always show if there are items)
        if (subtotal > 0) {
            val subtotalText = "Subtotal: ${currencyFormat.format(subtotal)}"
            val subtotalWidth = paint.measureText(subtotalText)
            canvas.drawText(subtotalText, width - subtotalWidth - 50f, yPos, paint)
            yPos += 30f
        }
        
        // Tax (only show if tax rate > 0)
        if (taxRate > 0 && taxAmount > 0) {
            val taxText = "Tax ${taxRate.toInt()}%: ${currencyFormat.format(taxAmount)}"
            val taxWidth = paint.measureText(taxText)
            canvas.drawText(taxText, width - taxWidth - 50f, yPos, paint)
            yPos += 30f
        }
        
        // Discount (only show if discount > 0)
        if (discount > 0) {
            val discountText = "Discount: ${currencyFormat.format(discount)}"
            val discountWidth = paint.measureText(discountText)
            canvas.drawText(discountText, width - discountWidth - 50f, yPos, paint)
            yPos += 30f
        }
        
        // Only show divider if we have summary items
        if (subtotal > 0 && (taxRate > 0 || discount > 0)) {
            yPos += 10f
            // Divider line
            paint.strokeWidth = 1f
            canvas.drawLine(40f, yPos, width - 40f, yPos, paint)
            yPos += 20f
        }
        
        // Grand Total
        paint.apply {
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.BLACK
        }
        val grandTotalText = "Grand Total: ${currencyFormat.format(grandTotal)}"
        val grandTotalWidth = paint.measureText(grandTotalText)
        canvas.drawText(grandTotalText, width - grandTotalWidth - 50f, yPos, paint)
        yPos += 60f
        
        // Thank you message
        paint.apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            color = Color.GRAY
        }
        val thankYouText = "Thank you for your order!"
        val thankYouWidth = paint.measureText(thankYouText)
        canvas.drawText(thankYouText, (width - thankYouWidth) / 2, yPos, paint)
        
        // Save bitmap to file
        return saveBitmapToFile(bitmap, billNumber)
    }
    
    private fun calculateHeight(billItems: List<BillItem>, hasSummary: Boolean): Int {
        val baseHeight = 400
        val itemHeight = 35
        val summaryHeight = if (hasSummary) 120 else 0
        return baseHeight + (billItems.size * itemHeight) + summaryHeight
    }
    
    private fun saveBitmapToFile(bitmap: Bitmap, billNumber: Int): Uri? {
        return try {
            val imagesDir = File(context.cacheDir, "bills")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            val imageFile = File(imagesDir, "bill_$billNumber.png")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

