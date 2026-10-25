package com.paisatrack.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.paisatrack.app.MainActivity
import com.paisatrack.app.R
import com.paisatrack.app.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class ExpenseWidgetCompactProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateCompactWidgetsInternal(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.paisatrack.app.ACTION_UPDATE_WIDGET") {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, ExpenseWidgetCompactProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        updateCompactWidgetsInternal(context, appWidgetManager, ids)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    companion object {
        suspend fun updateCompactWidgetsInternal(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)

            val startCal = Calendar.getInstance().apply {
                set(currentYear, currentMonth, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endCal = Calendar.getInstance().apply {
                set(currentYear, currentMonth, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val db = AppDatabase.getInstance(context)
            val totalExpense = db.transactionDao().getTotalExpenseForRangeSync(startCal.timeInMillis, endCal.timeInMillis) ?: 0.0

            val monthKey = "%d-%02d".format(currentYear, currentMonth + 1)
            val budgetObj = db.budgetDao().getBudgetForMonthSync(monthKey)
            val monthlyBudget = budgetObj?.overallBudget ?: 35000.0

            val remainingBudget = (monthlyBudget - totalExpense).coerceAtLeast(0.0)
            val percentUsed = if (monthlyBudget > 0) ((totalExpense / monthlyBudget) * 100).toInt().coerceIn(0, 100) else 0

            val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
            val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val daysLeft = (totalDays - todayDay + 1).coerceAtLeast(1)
            val dailyBurnRate = remainingBudget / daysLeft

            val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
                maximumFractionDigits = 0
            }

            val amountStr = currencyFormat.format(totalExpense)
            val burnRateStr = "${currencyFormat.format(dailyBurnRate)}/day"

            val statusText = when {
                percentUsed > 95 -> "● Over Budget"
                percentUsed > 75 -> "● $percentUsed%"
                else -> "● $percentUsed% used"
            }
            val statusColor = when {
                percentUsed > 95 -> Color.parseColor("#EF4444")
                percentUsed > 75 -> Color.parseColor("#F59E0B")
                else -> Color.parseColor("#10B981")
            }

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_expense_tracker_compact)

                views.setTextViewText(R.id.widget_tv_amount, amountStr)
                views.setTextViewText(R.id.widget_tv_burn_rate, burnRateStr)
                views.setTextViewText(R.id.widget_tv_status, statusText)
                views.setTextColor(R.id.widget_tv_status, statusColor)
                views.setProgressBar(R.id.widget_progress_bar, 100, percentUsed, false)

                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingOpenApp = PendingIntent.getActivity(
                    context,
                    widgetId + 20000,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, pendingOpenApp)

                val quickAddIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("EXTRA_QUICK_ADD", true)
                }
                val pendingQuickAdd = PendingIntent.getActivity(
                    context,
                    widgetId + 30000,
                    quickAddIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_add, pendingQuickAdd)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }
}
