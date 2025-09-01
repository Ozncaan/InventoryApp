package com.lotus.inventoryapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lotus.inventoryapp.data.model.Product
import com.lotus.inventoryapp.data.repository.ProductRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> =
        repository.getAllProducts()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addProduct(product: Product) = viewModelScope.launch {
        repository.insertProduct(product)
    }

    fun updateProduct(product: Product) = viewModelScope.launch {
        repository.updateProduct(product)
    }

    fun deleteProduct(product: Product) = viewModelScope.launch {
        repository.deleteProduct(product)
    }
    suspend fun findByBarcode(barcode: String): Product? =
        repository.getProductByBarcode(barcode)

}