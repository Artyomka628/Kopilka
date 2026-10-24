package com.example.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Transaction(
    val id: String,
    val timestamp: Long,
    val reason: String,
    val amount: Double,
    val category: String? = null,
    val kind: String? = null,
    val debtId: String? = null,
    val debtAction: String? = null,
    val debtName: String? = null,
    val debtType: DebtType? = null,
    val debtAmountBefore: Double? = null,
    val debtTimestamp: Long? = null
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

enum class DebtType {
    I_OWE,      // To borrow (I owe)
    OWED_TO_ME  // To lend (Owed to me)
}

@JsonClass(generateAdapter = true)
data class Debt(
    val id: String,
    val name: String,
    val amount: Double,
    val type: DebtType,
    val timestamp: Long = System.currentTimeMillis()
)
