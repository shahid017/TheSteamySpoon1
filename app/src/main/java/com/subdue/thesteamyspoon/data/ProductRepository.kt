package com.subdue.thesteamyspoon.data

import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    
    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)
    
    fun getProductsByCategory(category: String): Flow<List<Product>> = productDao.getProductsByCategory(category)
    
    fun searchProductsByCategory(query: String, category: String): Flow<List<Product>> = 
        productDao.searchProductsByCategory(query, category)
    
    suspend fun getAllCategories(): List<String> = productDao.getAllCategories()
    
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    
    suspend fun getProductByName(name: String): Product? = productDao.getProductByName(name)
    
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    
    suspend fun insertOrUpdateProduct(product: Product) {
        val existing = getProductByName(product.name)
        if (existing != null) {
            // Update existing product with same ID
            productDao.updateProduct(product.copy(id = existing.id))
        } else {
            // Insert new product
            productDao.insertProduct(product)
        }
    }
    
    suspend fun insertIfNotExists(product: Product) {
        val existing = getProductByName(product.name)
        if (existing == null) {
            productDao.insertProduct(product)
        }
        // Don't update if exists - preserve user changes
    }
    
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun deleteProductsWithoutCategory(): Int = productDao.deleteProductsWithoutCategory()
}






