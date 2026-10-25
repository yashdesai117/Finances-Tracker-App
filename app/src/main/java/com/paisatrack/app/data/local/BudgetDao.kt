package com.paisatrack.app.data.local

import androidx.room.*
import com.paisatrack.app.data.model.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE monthKey = :monthKey LIMIT 1")
    fun getBudgetForMonth(monthKey: String): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE monthKey = :monthKey LIMIT 1")
    suspend fun getBudgetForMonthSync(monthKey: String): Budget?
}
