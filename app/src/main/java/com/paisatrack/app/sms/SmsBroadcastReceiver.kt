package com.paisatrack.app.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.paisatrack.app.MainActivity
import com.paisatrack.app.R
import com.paisatrack.app.data.local.AppDatabase
import com.paisatrack.app.data.model.Transaction
import com.paisatrack.app.data.model.TransactionType
import com.paisatrack.app.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (messages.isEmpty()) return

        val sender = messages[0].originatingAddress ?: "Unknown"
        val fullBody = StringBuilder()
        for (sms in messages) {
            fullBody.append(sms.displayMessageBody)
        }
        val body = fullBody.toString()

        val parsed = SmsParserEngine.parse(sender, body) ?: return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)

                // Prevent duplicate processing
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

                    // Immediately update Home Screen Widget
                    WidgetUpdateHelper.updateAllWidgets(context)

                    // Show notification
                    showTransactionNotification(context, parsed)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showTransactionNotification(context: Context, parsed: ParsedTransaction) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "paisatrack_transactions"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Transaction Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when a new bank SMS is categorized"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val amountStr = currencyFormat.format(parsed.amount)
        val title = if (parsed.type == TransactionType.EXPENSE) {
            "Spent $amountStr at ${parsed.merchant}"
        } else {
            "Received $amountStr from ${parsed.merchant}"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setContentTitle(title)
            .setContentText("Categorized under ${parsed.categoryId.replaceFirstChar { it.uppercase() }}. Tap to review.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
