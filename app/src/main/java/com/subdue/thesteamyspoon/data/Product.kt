package com.subdue.thesteamyspoon.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val pricePerServing: Double,
    val defaultServing: Int,
    val defaultPieces: Int,
    val description: String = "", // Optional description/variant like "Less Spicy"
    val category: String = "" // Category like "Soups", "Light Cravings", etc.
)

