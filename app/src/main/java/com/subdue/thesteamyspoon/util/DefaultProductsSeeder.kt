package com.subdue.thesteamyspoon.util

import com.subdue.thesteamyspoon.data.Product
import com.subdue.thesteamyspoon.data.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Seeds the database with default products from The Steamy Spoon menu
 */
class DefaultProductsSeeder(private val productRepository: ProductRepository) {
    
    fun seedDefaultProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            val defaultProducts = getDefaultProducts()
            defaultProducts.forEach { product ->
                productRepository.insertProduct(product)
            }
        }
    }
    
    private fun getDefaultProducts(): List<Product> {
        return listOf(
            // SOUP SPECIAL Section
            Product(
                name = "The Steam Special Soup",
                pricePerServing = 350.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            Product(
                name = "Creamy Spinach Soup",
                pricePerServing = 550.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            Product(
                name = "Chicken Corn Soup",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            Product(
                name = "Chicken Broth",
                pricePerServing = 250.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            Product(
                name = "Mix Veg Noodle Soup",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            Product(
                name = "Manchow soup",
                pricePerServing = 450.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            
            // Light Cravings Section
            Product(
                name = "Drum Sticks",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 2,
                description = ""
            ),
            Product(
                name = "Chicken Lollypops",
                pricePerServing = 500.0,
                defaultServing = 1,
                defaultPieces = 6,
                description = ""
            ),
            Product(
                name = "Kiddy Dumplings",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = ""
            ),
            Product(
                name = "Soupy Dumplings",
                pricePerServing = 330.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = ""
            ),
            Product(
                name = "Cheesy Dumplings",
                pricePerServing = 400.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = ""
            ),
            Product(
                name = "Spicy Dumplings",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = ""
            ),
            Product(
                name = "Fried Dumplings",
                pricePerServing = 350.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = ""
            ),
            Product(
                name = "Steamy Smosa",
                pricePerServing = 50.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            Product(
                name = "Steamy Fries",
                pricePerServing = 200.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Regular"
            ),
            Product(
                name = "Chicken Nuggets",
                pricePerServing = 500.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = ""
            ),
            Product(
                name = "Potato chicken Cutlet",
                pricePerServing = 100.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            Product(
                name = "Chicken roll",
                pricePerServing = 50.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            
            // Confectionery Section
            Product(
                name = "Plain Donut",
                pricePerServing = 150.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            Product(
                name = "Chocolate Donut",
                pricePerServing = 200.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            Product(
                name = "Fruity Gummies",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = ""
            ),
            
            // Add Ons Section
            Product(
                name = "Boiled Eggs",
                pricePerServing = 0.0, // Price not specified, user can set
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On"
            ),
            Product(
                name = "Cream",
                pricePerServing = 0.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On"
            ),
            Product(
                name = "Mint Raita",
                pricePerServing = 0.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On"
            ),
            Product(
                name = "Dumpling Sauce",
                pricePerServing = 0.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On"
            ),
            Product(
                name = "Schezwan Chilli Oil",
                pricePerServing = 0.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On"
            ),
            Product(
                name = "Cheese",
                pricePerServing = 0.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On"
            )
        )
    }
}


