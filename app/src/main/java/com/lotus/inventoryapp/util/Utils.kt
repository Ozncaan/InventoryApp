package com.lotus.inventoryapp.util

import android.content.Context
import androidx.preference.PreferenceManager

fun Context.getLowStockThreshold(): Int {
    val str = PreferenceManager
        .getDefaultSharedPreferences(this)
        .getString("low_stock_threshold", "5")
    return str?.toIntOrNull() ?: 5
}