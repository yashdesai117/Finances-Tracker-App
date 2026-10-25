package com.paisatrack.app.data.local

import androidx.room.*
import com.paisatrack.app.data.model.CategorySpend
import com.paisatrack.app.data.model.Transaction
import com.paisatrack.app.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>): List<Long>

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsForRange(startTime: Long, endTime: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getTransactionsForRangeSync(startTime: Long, endTime: Long): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalExpenseForRange(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getTotalExpenseForRangeSync(startTime: Long, endTime: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'INCOME' AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalIncomeForRange(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT categoryId, SUM(amount) as totalAmount, COUNT(id) as count FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime AND timestamp <= :endTime GROUP BY categoryId ORDER BY totalAmount DESC")
    fun getCategorySpendSummary(startTime: Long, endTime: Long): Flow<List<CategorySpend>>

    @Query("SELECT COUNT(*) FROM transactions WHERE rawSms = :rawSms")
    suspend fun countByRawSms(rawSms: String): Int
}
