package com.lotus.inventoryapp.data.repository

import com.lotus.inventoryapp.data.model.ProductDao
import com.lotus.inventoryapp.data.model.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val dao: ProductDao) {
    fun getAllProducts(): Flow<List<Product>> = dao.getAll()
    suspend fun insertProduct(product: Product) = dao.insert(product)
    suspend fun updateProduct(product: Product) = dao.update(product)
    suspend fun deleteProduct(product: Product) = dao.delete(product)
    suspend fun getProductByBarcode(barcode: String): Product? = dao.getProductByBarcode(barcode)

}

