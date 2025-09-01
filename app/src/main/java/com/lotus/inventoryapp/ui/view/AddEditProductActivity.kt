package com.lotus.inventoryapp.ui.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lotus.inventoryapp.data.AppDatabase
import com.lotus.inventoryapp.data.model.Product
import com.lotus.inventoryapp.data.repository.ProductRepository
import com.lotus.inventoryapp.databinding.ActivityAddEditProductBinding
import com.lotus.inventoryapp.ui.viewmodel.ProductViewModel
import com.lotus.inventoryapp.ui.viewmodel.ProductViewModelFactory

class AddEditProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditProductBinding

    private val viewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(
            ProductRepository(AppDatabase.getDatabase(this).productDao())
        )
    }

    private var currentProduct: Product? = null

    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val code = result.data?.getStringExtra("scanned_code")
            binding.etBarcode.setText(code)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Düzenleme modundaki ürünü al
        currentProduct = intent.getParcelableExtra("product")
        currentProduct?.let { fillForm(it) }

        // Barkod tara
        binding.btnScan.setOnClickListener {
            Intent(this, BarcodeScannerActivity::class.java).also {
                scanLauncher.launch(it)
            }
        }

        // Ürünü kaydet veya güncelle
        binding.btnSave.setOnClickListener {
            saveProduct()
        }
    }

    private fun fillForm(product: Product) {
        binding.etName.setText(product.name)
        binding.etBarcode.setText(product.barcode)
        binding.etQuantity.setText(product.quantity.toString())
        binding.etExpiry.setText(product.expiryDate)
    }

    private fun saveProduct() {
        val name    = binding.etName.text.toString().trim()
        val barcode = binding.etBarcode.text.toString().trim().ifEmpty { null }
        val qtyText = binding.etQuantity.text.toString().trim()
        val expiry  = binding.etExpiry.text.toString().trim().ifEmpty { null }

        if (name.isEmpty() || qtyText.isEmpty()) {
            Toast.makeText(this, "Ürün adı ve adet zorunlu", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = qtyText.toIntOrNull() ?: run {
            Toast.makeText(this, "Geçerli bir adet girin", Toast.LENGTH_SHORT).show()
            return
        }

        val product = currentProduct
            ?.copy(name = name, barcode = barcode, quantity = quantity, expiryDate = expiry)
            ?: Product(name = name, barcode = barcode, quantity = quantity, expiryDate = expiry)

        if (currentProduct == null) {
            viewModel.addProduct(product)
            Toast.makeText(this, "Ürün eklendi", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.updateProduct(product)
            Toast.makeText(this, "Ürün güncellendi", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}