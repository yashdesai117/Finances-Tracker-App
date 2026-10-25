package com.paisatrack.app.sms

import com.paisatrack.app.data.model.TransactionType

data class ParsedTransaction(
    val amount: Double,
    val type: TransactionType,
    val merchant: String,
    val accountName: String,
    val timestamp: Long,
    val rawSms: String,
    val sender: String,
    val categoryId: String,
    val isAutoCategorized: Boolean = true
)
