package com.subdue.thesteamyspoon.util

import com.subdue.thesteamyspoon.data.Product
import com.subdue.thesteamyspoon.data.ProductRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Seeds the database with default products from The Steamy Spoon menu
 */
class DefaultProductsSeeder(private val productRepository: ProductRepository) {
    
    fun seedDefaultProducts() {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultProductsSync()
        }
    }
    
    suspend fun seedDefaultProductsSync() {
        val defaultProducts = getDefaultProducts()
        // Only insert products that don't exist - preserve user modifications
        defaultProducts.forEach { product ->
            productRepository.insertIfNotExists(product)
        }
    }
    
    private fun getDefaultProducts(): List<Product> {
        return listOf(
            // SOUPS Section
            Product(
                name = "Chicken Corn Soup",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Shredded chicken, sweet corn, and silky egg ribbons in a savory broth",
                category = "Soups"
            ),
            Product(
                name = "Noddle Soup",
                pricePerServing = 320.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Springy noodles, crisp vegetables, and herbal savory broth",
                category = "Soups"
            ),
            Product(
                name = "Mix Veg Soup",
                pricePerServing = 330.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Fresh vegetables and aromatic herbs simmered in a light, savory chicken broth",
                category = "Soups"
            ),
            Product(
                name = "Steamy Special Soup",
                pricePerServing = 350.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Chicken, mushrooms, corn and fresh vegetables in a rich, savory broth",
                category = "Soups"
            ),
            Product(
                name = "Creamy Spinach Soup",
                pricePerServing = 480.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Smooth puréed spinach and garden vegetables with a heavy blend of cream",
                category = "Soups"
            ),
            
            // Light Cravings Section
            Product(
                name = "Drumsticks",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 2,
                description = "Crispy chicken drumsticks seasoned with classic herbs and spices",
                category = "Light Cravings"
            ),
            Product(
                name = "Chicken Lollipop",
                pricePerServing = 500.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = "Crispy chicken and cheese popsicles seasoned with aromatic spices",
                category = "Light Cravings"
            ),
            Product(
                name = "Chicken Nuggets",
                pricePerServing = 400.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = "Crispy golden-fried chicken bites seasoned with a blend of spices",
                category = "Light Cravings"
            ),
            Product(
                name = "Chicken Roll",
                pricePerServing = 60.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Shredded chicken and crisp vegetables wrapped in a wrap",
                category = "Light Cravings"
            ),
            Product(
                name = "Loaded Fries",
                pricePerServing = 650.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Fluffy stir-fried rice tossed with scrambled eggs, vegetables, and savory seasonings",
                category = "Light Cravings"
            ),
            Product(
                name = "Plain Fries",
                pricePerServing = 200.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Golden, crispy potato strips lightly seasoned with a touch of sea salt",
                category = "Light Cravings"
            ),
            
            // Dumplings Section
            Product(
                name = "Soupy Dumplings",
                pricePerServing = 330.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = "Delicate steamed dumplings filled with tender minced meat and served with savory broth",
                category = "Dumplings"
            ),
            Product(
                name = "Spicy Dumplings",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = "Steamed minced meat dumplings in oyster sauce and spices, topped with thick chili oil",
                category = "Dumplings"
            ),
            Product(
                name = "Cheesy Dumplings",
                pricePerServing = 420.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = "Delicate steamed dumplings filled with savory meat and topped with gooey melted cheese",
                category = "Dumplings"
            ),
            Product(
                name = "Fried Dumplings",
                pricePerServing = 350.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = "Crispy golden pan-fried dumplings filled with seasoned meat and aromatic herbs",
                category = "Dumplings"
            ),
            Product(
                name = "Kiddy Dumplings",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 5,
                description = "Mildly seasoned steamed dumplings crafted with simple flavors for young palates",
                category = "Dumplings"
            ),
            
            // Samosas Section
            Product(
                name = "Potato Samosa",
                pricePerServing = 45.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Crispy wrap filled with spiced mashed potatoes and aromatic herbs",
                category = "Samosas"
            ),
            Product(
                name = "Veg Samosa",
                pricePerServing = 45.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Crispy wrap filled with fresh garden vegetables",
                category = "Samosas"
            ),
            Product(
                name = "Chicken Veg Samosa",
                pricePerServing = 55.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Crispy wrap filled with spiced minced chicken, and garden vegetables",
                category = "Samosas"
            ),
            Product(
                name = "Malai Boti Samosa",
                pricePerServing = 65.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Crispy wrap filled with tender chicken in a creamy, mild spice blend",
                category = "Samosas"
            ),
            Product(
                name = "Pizza Samosa",
                pricePerServing = 80.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Crispy wrap filled with cheese, pizza sauce, and savory chicken pieces",
                category = "Samosas"
            ),
            
            // Confectionery Section
            Product(
                name = "Plain Donut",
                pricePerServing = 150.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Soft and airy ring-shaped dough glazed with a classic sweet coating",
                category = "Confectionery"
            ),
            Product(
                name = "Chocolate Donut",
                pricePerServing = 200.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Soft and airy ring-shaped dough topped with a rich, velvety chocolate glaze with light sprinkles",
                category = "Confectionery"
            ),
            Product(
                name = "Chocolate Bismarck",
                pricePerServing = 200.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Soft, round donut shells overflowing with a rich and velvety chocolate cream",
                category = "Confectionery"
            ),
            Product(
                name = "Chocolate Mug Cake",
                pricePerServing = 300.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Warm, moist chocolate sponge prepared instantly for a quick, decadent treat",
                category = "Confectionery"
            ),
            
            // Hotpots Section
            Product(
                name = "Spicy Broth Hotpot",
                pricePerServing = 1400.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "5 Piece Dumplings, 1 Boiled Egg, 3 Sausages, Noodles, Chunks of Mushroom, vegetables and Chicken with Spicy broth",
                category = "Hotpots"
            ),
            Product(
                name = "Chicken Broth Hotpot",
                pricePerServing = 1400.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "5 Piece Dumplings, 1 Boiled Egg, 3 Sausages, Noodles, Chunks of Mushroom, vegetables, and Chicken with Spicy broth",
                category = "Hotpots"
            ),
            
            // Rice and Gravy Section
            Product(
                name = "Egg Fried Rice",
                pricePerServing = 350.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Fluffy stir-fried rice tossed with scrambled eggs, vegetables, and savory seasonings",
                category = "Rice and Gravy"
            ),
            Product(
                name = "Manchurian",
                pricePerServing = 350.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Tender chicken chunks tossed in a tangy, spicy, and savory sauce",
                category = "Rice and Gravy"
            ),
            
            // Add Ons Section
            Product(
                name = "Boiled Egg",
                pricePerServing = 50.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On",
                category = "Add Ons"
            ),
            Product(
                name = "Dumpling Sauce",
                pricePerServing = 50.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On",
                category = "Add Ons"
            ),
            Product(
                name = "Schezwan Chilli Oil",
                pricePerServing = 50.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On",
                category = "Add Ons"
            ),
            Product(
                name = "Cheese",
                pricePerServing = 100.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On",
                category = "Add Ons"
            ),
            Product(
                name = "Mint Raita",
                pricePerServing = 70.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On",
                category = "Add Ons"
            ),
            Product(
                name = "Cream",
                pricePerServing = 60.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Add On",
                category = "Add Ons"
            ),
            
            // Beverages Section
            Product(
                name = "Dodh Patti/Tea",
                pricePerServing = 150.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Traditional tea",
                category = "Beverages"
            ),
            Product(
                name = "Hot Chocolate",
                pricePerServing = 320.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "Rich and creamy hot chocolate",
                category = "Beverages"
            ),
            Product(
                name = "Next Cola 1.5L",
                pricePerServing = 170.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "1.5L bottle",
                category = "Beverages"
            ),
            Product(
                name = "Next Cola 1L",
                pricePerServing = 150.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "1L bottle",
                category = "Beverages"
            ),
            Product(
                name = "Next Cola 300ML",
                pricePerServing = 80.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "300ML bottle",
                category = "Beverages"
            ),
            Product(
                name = "Fizup Next 1.5L",
                pricePerServing = 170.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "1.5L bottle",
                category = "Beverages"
            ),
            Product(
                name = "Fizup Next 1L",
                pricePerServing = 150.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "1L bottle",
                category = "Beverages"
            ),
            Product(
                name = "Fizup Next 300ML",
                pricePerServing = 80.0,
                defaultServing = 1,
                defaultPieces = 1,
                description = "300ML bottle",
                category = "Beverages"
            )
        )
    }
}






