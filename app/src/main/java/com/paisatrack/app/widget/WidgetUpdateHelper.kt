package com.paisatrack.app.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // 1. Update Full Card Widgets
        val fullComponentName = ComponentName(context, ExpenseWidgetProvider::class.java)
        val fullIds = appWidgetManager.getAppWidgetIds(fullComponentName)
        if (fullIds != null && fullIds.isNotEmpty()) {
            val intent = Intent(context, ExpenseWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, fullIds)
            }
            context.sendBroadcast(intent)
        }

        // 2. Update Compact Slim Widgets
        val compactComponentName = ComponentName(context, ExpenseWidgetCompactProvider::class.java)
        val compactIds = appWidgetManager.getAppWidgetIds(compactComponentName)
        if (compactIds != null && compactIds.isNotEmpty()) {
            val intent = Intent(context, ExpenseWidgetCompactProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, compactIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
