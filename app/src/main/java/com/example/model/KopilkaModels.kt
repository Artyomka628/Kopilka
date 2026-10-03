package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: String,
    val timestamp: Long,
    val reason: String,
    val amount: Double,
    val category: String? = null
)

@JsonClass(generateAdapter = true)
data class KopilkaData(
    val balance: Double,
    val goal: Double,
    val transactions: List<Transaction>
)

@JsonClass(generateAdapter = true)
data class CloudData(
    val balance: Double,
    val goal: Double,
    val transactions: List<Transaction>,
    val deletedTxIds: List<String>
)
