package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.ui.theme.*
import java.util.*

fun formatDouble(value: Double): String {
    val rounded = Math.round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) {
        String.format(Locale.US, "%,d", rounded.toLong())
    } else {
        val str = String.format(Locale.US, "%,.2f", rounded)
        if (str.endsWith("0")) str.dropLast(1) else str
    }
}

fun formatInputAmount(value: Double): String {
    val rounded = Math.round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        val str = String.format(Locale.US, "%.2f", rounded)
        if (str.endsWith("0")) str.dropLast(1) else str
    }
}
