package com.lotus.inventoryapp.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "product_table")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val barcode: String?,

    val quantity: Int,

    val expiryDate: String?
) : Parcelable