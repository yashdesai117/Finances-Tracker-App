package com.paisatrack.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val monthKey: String, // e.g. "2026-10"
    val overallBudget: Double = 35000.0,
    val categoryBudgetsJson: String = "{}"
)
