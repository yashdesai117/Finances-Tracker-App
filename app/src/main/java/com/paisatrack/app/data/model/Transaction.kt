package com.paisatrack.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: String,
    val merchant: String,
    val accountName: String,
    val timestamp: Long,
    val rawSms: String? = null,
    val sender: String? = null,
    val isAutoCategorized: Boolean = true,
    val notes: String = ""
)

data class CategorySpend(
    val categoryId: String,
    val totalAmount: Double,
    val count: Int
)
