package com.paisatrack.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.TextView
import com.paisatrack.app.MainActivity
import com.paisatrack.app.R
import com.paisatrack.app.data.local.AppDatabase
import kotlinx.coroutines.*
import java.text.NumberFormat
import java.util.*

class FloatingBubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createFloatingView()
        updateSpendingData()
    }

    private fun createFloatingView() {
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = layoutInflater.inflate(R.layout.layout_floating_pill, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 200
        }

        windowManager?.addView(floatingView, params)

        // Make the floating pill draggable across the screen
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isClick = false
                    }
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager?.updateViewLayout(floatingView, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        // Open main dashboard
                        val appIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(appIntent)
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun updateSpendingData() {
        serviceScope.launch(Dispatchers.IO) {
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

            val db = AppDatabase.getInstance(applicationContext)
            val totalExpense = db.transactionDao().getTotalExpenseForRangeSync(startCal.timeInMillis, endCal.timeInMillis) ?: 0.0

            val monthKey = "%d-%02d".format(currentYear, currentMonth + 1)
            val budgetObj = db.budgetDao().getBudgetForMonthSync(monthKey)
            val monthlyBudget = budgetObj?.overallBudget ?: 35000.0

            val percentUsed = if (monthlyBudget > 0) ((totalExpense / monthlyBudget) * 100).toInt().coerceIn(0, 100) else 0

            withContext(Dispatchers.Main) {
                val tvBadge = floatingView?.findViewById<TextView>(R.id.floating_tv_badge)
                val tvPercent = floatingView?.findViewById<TextView>(R.id.floating_tv_percent)

                val formatted = if (totalExpense >= 1000) {
                    "₹%.1fk".format(totalExpense / 1000.0)
                } else {
                    "₹%d".format(totalExpense.toInt())
                }

                tvBadge?.text = formatted
                tvPercent?.text = "$percentUsed%"

                val percentColor = when {
                    percentUsed > 95 -> Color.parseColor("#EF4444")
                    percentUsed > 75 -> Color.parseColor("#F59E0B")
                    else -> Color.parseColor("#10B981")
                }
                tvPercent?.setTextColor(percentColor)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (floatingView != null) {
            windowManager?.removeView(floatingView)
        }
    }
}
