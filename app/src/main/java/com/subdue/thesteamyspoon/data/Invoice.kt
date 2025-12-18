package com.subdue.thesteamyspoon.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.subdue.thesteamyspoon.data.converters.BillItemListConverter

@Entity(tableName = "invoices")
@TypeConverters(BillItemListConverter::class)
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val billNumber: Int,
    val dateTime: String,
    val subtotal: Double,
    val taxRate: Double,
    val taxAmount: Double,
    val discount: Double,
    val grandTotal: Double,
    val billItems: List<BillItemData>,
    val houseNumber: String? = null,
    val block: String? = null,
    val phoneNumber: String? = null
)

data class BillItemData(
    val productId: Long,
    val productName: String,
    val productDescription: String,
    val pricePerServing: Double,
    val quantity: Int, // This is the serving quantity
    val defaultServing: Int,
    val defaultPieces: Int,
    val totalPrice: Double,
    val addOns: List<String> = emptyList()
)

