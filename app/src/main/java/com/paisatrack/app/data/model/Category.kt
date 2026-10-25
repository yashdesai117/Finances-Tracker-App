package com.paisatrack.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String,
    val name: String,
    val iconEmoji: String,
    val colorHex: String,
    val defaultBudget: Double = 0.0,
    val isIncome: Boolean = false
) {
    companion object {
        val DEFAULT_CATEGORIES = listOf(
            Category("food", "Food & Dining", "🍔", "#F59E0B", 8000.0),
            Category("groceries", "Groceries", "🛒", "#10B981", 6000.0),
            Category("shopping", "Shopping", "🛍️", "#EC4899", 5000.0),
            Category("transport", "Transport & Fuel", "🚗", "#3B82F6", 4000.0),
            Category("bills", "Bills & Utilities", "💡", "#8B5CF6", 6000.0),
            Category("entertainment", "Entertainment", "🎬", "#F43F5E", 2500.0),
            Category("health", "Health & Medical", "💊", "#14B8A6", 2000.0),
            Category("investments", "Investments", "📈", "#6366F1", 5000.0),
            Category("salary", "Salary & Income", "💼", "#22C55E", 0.0, isIncome = true),
            Category("cashback", "Cashback & Refunds", "🎁", "#A855F7", 0.0, isIncome = true),
            Category("other", "Other Expenses", "📦", "#64748B", 1500.0)
        )
    }
}
