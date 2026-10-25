package com.paisatrack.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.paisatrack.app.data.local.AppDatabase
import com.paisatrack.app.data.model.Budget
import com.paisatrack.app.data.model.Category
import com.paisatrack.app.data.model.Transaction
import com.paisatrack.app.service.FloatingBubbleService
import com.paisatrack.app.ui.components.AddTransactionDialog
import com.paisatrack.app.ui.screens.BudgetsScreen
import com.paisatrack.app.ui.screens.DashboardScreen
import com.paisatrack.app.ui.screens.SmsSyncScreen
import com.paisatrack.app.ui.screens.TransactionsScreen
import com.paisatrack.app.ui.theme.CanvasDark
import com.paisatrack.app.ui.theme.PaisaTrackTheme
import com.paisatrack.app.widget.WidgetUpdateHelper
import kotlinx.coroutines.launch
import java.util.Calendar

enum class Screen {
    DASHBOARD,
    TRANSACTIONS,
    BUDGETS,
    SMS_SYNC
}

class MainActivity : ComponentActivity() {

    private var isFloatingServiceRunning by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shouldQuickAdd = intent.getBooleanExtra("EXTRA_QUICK_ADD", false)

        setContent {
            PaisaTrackTheme {
                val db = remember { AppDatabase.getInstance(applicationContext) }
                val scope = rememberCoroutineScope()

                var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
                var showAddDialog by remember { mutableStateOf(shouldQuickAdd) }

                // Current month time range
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)

                val startCal = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, 1, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startTimestamp = startCal.timeInMillis

                val endCal = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val endTimestamp = endCal.timeInMillis

                val monthKey = "%d-%02d".format(currentYear, currentMonth + 1)

                // Reactive data streams from Room Database
                val transactions by db.transactionDao().getTransactionsForRange(startTimestamp, endTimestamp)
                    .collectAsState(initial = emptyList())
                val allTransactions by db.transactionDao().getAllTransactions()
                    .collectAsState(initial = emptyList())
                val totalExpense by db.transactionDao().getTotalExpenseForRange(startTimestamp, endTimestamp)
                    .collectAsState(initial = 0.0)
                val totalIncome by db.transactionDao().getTotalIncomeForRange(startTimestamp, endTimestamp)
                    .collectAsState(initial = 0.0)
                val categorySpends by db.transactionDao().getCategorySpendSummary(startTimestamp, endTimestamp)
                    .collectAsState(initial = emptyList())
                val categories by db.categoryDao().getAllCategories()
                    .collectAsState(initial = Category.DEFAULT_CATEGORIES)
                val budgetObj by db.budgetDao().getBudgetForMonth(monthKey)
                    .collectAsState(initial = null)

                val monthlyBudget = budgetObj?.overallBudget ?: 35000.0

                fun toggleFloatingBubble() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this@MainActivity)) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                        startActivity(intent)
                    } else {
                        if (isFloatingServiceRunning) {
                            stopService(Intent(this@MainActivity, FloatingBubbleService::class.java))
                            isFloatingServiceRunning = false
                        } else {
                            startService(Intent(this@MainActivity, FloatingBubbleService::class.java))
                            isFloatingServiceRunning = true
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CanvasDark
                ) {
                    when (currentScreen) {
                        Screen.DASHBOARD -> DashboardScreen(
                            totalExpense = totalExpense ?: 0.0,
                            totalIncome = totalIncome ?: 0.0,
                            monthlyBudget = monthlyBudget,
                            recentTransactions = transactions,
                            categories = categories,
                            categorySpends = categorySpends,
                            isFloatingBubbleActive = isFloatingServiceRunning,
                            onToggleFloatingBubble = { toggleFloatingBubble() },
                            onAddTransactionClick = { showAddDialog = true },
                            onDeleteTransaction = { txn ->
                                scope.launch {
                                    db.transactionDao().delete(txn)
                                    WidgetUpdateHelper.updateAllWidgets(applicationContext)
                                }
                            },
                            onNavigateToTransactions = { currentScreen = Screen.TRANSACTIONS },
                            onNavigateToSmsSync = { currentScreen = Screen.SMS_SYNC },
                            onNavigateToBudgets = { currentScreen = Screen.BUDGETS }
                        )
                        Screen.TRANSACTIONS -> TransactionsScreen(
                            transactions = allTransactions,
                            categories = categories,
                            onDeleteTransaction = { txn ->
                                scope.launch {
                                    db.transactionDao().delete(txn)
                                    WidgetUpdateHelper.updateAllWidgets(applicationContext)
                                }
                            },
                            onBackClick = { currentScreen = Screen.DASHBOARD }
                        )
                        Screen.BUDGETS -> BudgetsScreen(
                            currentBudget = monthlyBudget,
                            categories = categories,
                            onSaveBudget = { newTarget ->
                                scope.launch {
                                    db.budgetDao().setBudget(Budget(monthKey = monthKey, overallBudget = newTarget))
                                    WidgetUpdateHelper.updateAllWidgets(applicationContext)
                                }
                            },
                            onBackClick = { currentScreen = Screen.DASHBOARD }
                        )
                        Screen.SMS_SYNC -> SmsSyncScreen(
                            isFloatingBubbleActive = isFloatingServiceRunning,
                            onToggleFloatingBubble = { toggleFloatingBubble() },
                            onBackClick = { currentScreen = Screen.DASHBOARD },
                            onHistoricalSyncComplete = {
                                WidgetUpdateHelper.updateAllWidgets(applicationContext)
                            }
                        )
                    }

                    if (showAddDialog) {
                        AddTransactionDialog(
                            categories = categories,
                            onDismiss = { showAddDialog = false },
                            onConfirm = { newTxn ->
                                scope.launch {
                                    db.transactionDao().insert(newTxn)
                                    WidgetUpdateHelper.updateAllWidgets(applicationContext)
                                    showAddDialog = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
