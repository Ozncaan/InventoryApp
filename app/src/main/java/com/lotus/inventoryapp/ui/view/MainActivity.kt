package com.lotus.inventoryapp.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.lotus.inventoryapp.R
import com.lotus.inventoryapp.data.AppDatabase
import com.lotus.inventoryapp.data.repository.ProductRepository
import com.lotus.inventoryapp.databinding.ActivityMainBinding
import com.lotus.inventoryapp.ui.view.adapter.ProductListAdapter
import com.lotus.inventoryapp.ui.viewmodel.ProductViewModel
import com.lotus.inventoryapp.ui.viewmodel.ProductViewModelFactory
import com.lotus.inventoryapp.util.getLowStockThreshold
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ProductListAdapter
    private var alertShown = false

    private val viewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(
            ProductRepository(AppDatabase.getDatabase(this).productDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar’ı ActionBar olarak ayarla
        setSupportActionBar(binding.toolbar)

        // Adapter’i oluştur
        adapter = ProductListAdapter(
            onItemClick = { product ->
                startActivity(
                    Intent(this, AddEditProductActivity::class.java)
                        .putExtra("product", product)
                )
            },
            onIncrease = { product ->
                viewModel.updateProduct(product.copy(quantity = product.quantity + 1))
            },
            onDecrease = { product ->
                if (product.quantity > 0) {
                    viewModel.updateProduct(product.copy(quantity = product.quantity - 1))
                }
            }
        )

        // RecyclerView
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        setupSwipeToDelete()

        // Ürün listesini gözlemle, filtrele, düşük stok uyarısı
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collectLatest { list ->
                    adapter.submitList(list)

                    if (!alertShown) {
                        val threshold = getLowStockThreshold()
                        val lowCount = list.count { it.quantity <= threshold }
                        if (lowCount > 0) {
                            Snackbar.make(
                                binding.root,
                                "Dikkat: $lowCount ürünün stoğu az",
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                        alertShown = true
                    }
                }
            }
        }

        // FAB’lar
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditProductActivity::class.java))
        }
        binding.fabScan.setOnClickListener {
            startActivity(Intent(this, ScanProductActivity::class.java))
        }
    }

    private fun setupSwipeToDelete() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                target: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val product = adapter.currentList[position]
                viewModel.deleteProduct(product)
                Snackbar.make(binding.root, "${product.name} silindi", Snackbar.LENGTH_LONG)
                    .setAction("Geri Al") {
                        viewModel.addProduct(product)
                    }
                    .show()
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.rvProducts)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.queryHint = "Ürün adı veya barkoda göre ara"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText)
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}