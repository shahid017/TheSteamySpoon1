package com.subdue.thesteamyspoon.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.subdue.thesteamyspoon.data.AppDatabase
import com.subdue.thesteamyspoon.data.InvoiceRepository
import com.subdue.thesteamyspoon.data.ProductRepository
import com.subdue.thesteamyspoon.viewmodel.BillViewModel
import com.subdue.thesteamyspoon.viewmodel.InvoiceViewModel
import com.subdue.thesteamyspoon.viewmodel.ProductViewModel

object AppContainer {
    lateinit var database: AppDatabase
        private set
    
    lateinit var productRepository: ProductRepository
        private set
    
    lateinit var invoiceRepository: InvoiceRepository
        private set
    
    fun initialize(application: Application) {
        database = AppDatabase.getDatabase(application)
        productRepository = ProductRepository(database.productDao())
        invoiceRepository = InvoiceRepository(database.invoiceDao())
    }
    
    val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            ProductViewModel(productRepository)
        }
        initializer {
            BillViewModel(invoiceRepository)
        }
        initializer {
            InvoiceViewModel(invoiceRepository)
        }
    }
}

