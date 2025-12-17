package com.subdue.thesteamyspoon.model

import com.subdue.thesteamyspoon.data.Product

data class BillItem(
    val product: Product,
    val quantity: Int
) {
    val totalPrice: Double
        get() = product.pricePerServing * quantity
}

