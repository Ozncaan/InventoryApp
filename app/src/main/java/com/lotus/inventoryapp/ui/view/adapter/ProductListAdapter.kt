package com.lotus.inventoryapp.ui.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lotus.inventoryapp.R
import com.lotus.inventoryapp.data.model.Product
import com.lotus.inventoryapp.databinding.ItemProductBinding
import com.lotus.inventoryapp.util.getLowStockThreshold

class ProductListAdapter(
    private val onItemClick: (Product) -> Unit,
    private val onIncrease: (Product) -> Unit,
    private val onDecrease: (Product) -> Unit
) : ListAdapter<Product, ProductListAdapter.ProductViewHolder>(DIFF_CALLBACK) {

    private var fullList: List<Product> = emptyList()

    override fun submitList(list: List<Product>?) {
        fullList = list.orEmpty()
        super.submitList(list)
    }

    fun filter(query: String?) {
        val filtered = if (query.isNullOrBlank()) fullList
        else fullList.filter {
            it.name.contains(query, true)
                    || (it.barcode?.contains(query, true) ?: false)
        }
        super.submitList(filtered)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProductViewHolder(
        ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvName.text       = product.name
            binding.tvBarcodeQty.text = "Barkod: ${product.barcode.orEmpty()}"
            binding.tvQuantity.text   = product.quantity.toString()
            binding.tvExpiry.text     =
                product.expiryDate?.let { "Son Kullanma: $it" } ?: ""

            // Dinamik düşük stok eşiğini al
            val threshold = binding.root.context.getLowStockThreshold()
            val colorRes = if (product.quantity <= threshold)
                R.color.low_stock_red
            else
                android.R.color.black

            binding.tvQuantity.setTextColor(
                ContextCompat.getColor(binding.root.context, colorRes)
            )

            binding.btnIncrease.setOnClickListener { onIncrease(product) }
            binding.btnDecrease.setOnClickListener { onDecrease(product) }
            binding.root.setOnClickListener { onItemClick(product) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Product>() {
            override fun areItemsTheSame(old: Product, new: Product) = old.id == new.id
            override fun areContentsTheSame(old: Product, new: Product) = old == new
        }
    }
}