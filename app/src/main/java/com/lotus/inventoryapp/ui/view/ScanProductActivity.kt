package com.lotus.inventoryapp.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.lotus.inventoryapp.data.AppDatabase
import com.lotus.inventoryapp.data.model.Product
import com.lotus.inventoryapp.data.repository.ProductRepository
import com.lotus.inventoryapp.databinding.ActivityScanProductBinding
import com.lotus.inventoryapp.ui.viewmodel.ProductViewModel
import com.lotus.inventoryapp.ui.viewmodel.ProductViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanProductBinding

    private val viewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(
            ProductRepository(
                AppDatabase.getDatabase(this).productDao()
            )
        )
    }

    private val scanLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult

        val code = result.data?.getStringExtra("scanned_code").orEmpty()
        if (code.isBlank()) {
            Toast.makeText(this, "Barkod okunamadı", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }

        binding.btnScan.isEnabled = false

        lifecycleScope.launch {
            val product: Product? = viewModel.findByBarcode(code)

            if (product == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ScanProductActivity,
                        "Ürün bulunamadı: $code",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnScan.isEnabled = true
                }
            } else {
                // Stoğu 1 azalt
                val newQty = (product.quantity - 1).coerceAtLeast(0)
                viewModel.updateProduct(product.copy(quantity = newQty))

                withContext(Dispatchers.Main) {
                    binding.tvScanName.text = product.name
                    binding.tvScanBarcode.text = "Barkod: $code"
                    binding.tvScanQuantity.text = "Adet: $newQty"
                    binding.tvScanExpiry.text =
                        "Son Kullanma: ${product.expiryDate.orEmpty()}"
                    binding.cardProduct.visibility = View.VISIBLE

                    Toast.makeText(
                        this@ScanProductActivity,
                        "Stok 1 adet azaltıldı",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnScan.isEnabled = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.btnScan.setOnClickListener {
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            scanLauncher.launch(intent)
        }
    }
}