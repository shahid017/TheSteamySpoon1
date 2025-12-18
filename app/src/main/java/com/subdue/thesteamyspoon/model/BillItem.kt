package com.subdue.thesteamyspoon.model

import com.subdue.thesteamyspoon.data.Product

data class BillItem(
    val product: Product,
    val quantity: Int,
    val addOns: List<String> = emptyList()
) {
    val addOnPrice: Double
        get() = addOns.sumOf { addOn ->
            if (addOn.equals("Cheese", ignoreCase = true)) 100.0 else 50.0
        }
    
    val totalPrice: Double
        get() = (product.pricePerServing * quantity) + addOnPrice
}

