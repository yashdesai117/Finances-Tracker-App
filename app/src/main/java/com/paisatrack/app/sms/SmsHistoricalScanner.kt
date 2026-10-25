package com.paisatrack.app.sms

import android.content.Context
import android.net.Uri
import com.paisatrack.app.data.local.AppDatabase
import com.paisatrack.app.data.model.Transaction
import com.paisatrack.app.widget.WidgetUpdateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsHistoricalScanner {

    suspend fun scanHistoricalSms(
        context: Context,
        daysBack: Int = 60,
        onProgress: ((current: Int, total: Int) -> Unit)? = null
    ): Int = withContext(Dispatchers.IO) {
        val db = AppDatabase.getInstance(context)
        val cutoff = System.currentTimeMillis() - (daysBack.toLong() * 24 * 60 * 60 * 1000)

        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")
        val selection = "date >= ?"
        val selectionArgs = arrayOf(cutoff.toString())
        val sortOrder = "date DESC"

        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            ?: return@withContext 0

        var addedCount = 0
        val total = cursor.count

        cursor.use {
            var index = 0
            while (it.moveToNext()) {
                index++
                val address = it.getString(it.getColumnIndexOrThrow("address")) ?: ""
                val body = it.getString(it.getColumnIndexOrThrow("body")) ?: ""
                val date = it.getLong(it.getColumnIndexOrThrow("date"))

                val parsed = SmsParserEngine.parse(address, body, date)
                if (parsed != null) {
                    if (db.transactionDao().countByRawSms(body) == 0) {
                        val transaction = Transaction(
                            amount = parsed.amount,
                            type = parsed.type,
                            categoryId = parsed.categoryId,
                            merchant = parsed.merchant,
                            accountName = parsed.accountName,
                            timestamp = parsed.timestamp,
                            rawSms = parsed.rawSms,
                            sender = parsed.sender,
                            isAutoCategorized = true
                        )
                        db.transactionDao().insert(transaction)
                        addedCount++
                    }
                }

                if (index % 10 == 0 || index == total) {
                    onProgress?.invoke(index, total)
                }
            }
        }

        if (addedCount > 0) {
            WidgetUpdateHelper.updateAllWidgets(context)
        }

        return@withContext addedCount
    }
}
