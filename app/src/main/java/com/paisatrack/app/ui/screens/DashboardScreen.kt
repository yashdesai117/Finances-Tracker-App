package com.paisatrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paisatrack.app.data.model.Category
import com.paisatrack.app.data.model.CategorySpend
import com.paisatrack.app.data.model.Transaction
import com.paisatrack.app.data.model.TransactionType
import com.paisatrack.app.ui.components.*
import com.paisatrack.app.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    totalExpense: Double,
    totalIncome: Double,
    monthlyBudget: Double,
    recentTransactions: List<Transaction>,
    categories: List<Category>,
    categorySpends: List<CategorySpend>,
    isFloatingBubbleActive: Boolean,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleFloatingBubble: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onUpdateTransactionCategory: (Transaction, String) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToSmsSync: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    val remainingBudget = (monthlyBudget - totalExpense).coerceAtLeast(0.0)
    val percentUsed = if (monthlyBudget > 0) ((totalExpense / monthlyBudget) * 100).toInt().coerceIn(0, 100) else 0

    val calendar = Calendar.getInstance()
    val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
    val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val daysLeft = (totalDays - todayDay + 1).coerceAtLeast(1)
    val dailyBurnRate = remainingBudget / daysLeft

    val categoryMap = remember(categories) { categories.associateBy { it.id } }

    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    val isDark = isDarkTheme
    val canvasBg = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val itemBg = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF1F5F9)
    val borderColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Scaffold(
        containerColor = canvasBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = AccentCobalt,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Brand, Month & Theme / Settings Actions
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PAISATRACK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCobalt,
                            letterSpacing = 1.5.sp
                        )
                        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                        Text(
                            text = monthName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Top Action Icons: Theme Switcher, Budget, SMS Sync
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Light / Dark Theme Switcher Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(surfaceColor)
                                .border(1.dp, borderColor, CircleShape)
                                .clickable { onToggleTheme() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isDark) "☀️" else "🌙",
                                fontSize = 16.sp
                            )
                        }

                        // Budget Target Settings
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(surfaceColor)
                                .border(1.dp, borderColor, CircleShape)
                                .clickable { onNavigateToBudgets() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎯", fontSize = 16.sp)
                        }

                        // SMS Inbox Sync
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(surfaceColor)
                                .border(1.dp, borderColor, CircleShape)
                                .clickable { onNavigateToSmsSync() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📩", fontSize = 16.sp)
                        }
                    }
                }
            }

            // 1. Bento Hero Card: Spend Headline + Progress Bar + 30-Day Activity Strip
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(surfaceColor)
                        .border(1.dp, borderColor, RoundedCornerShape(26.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL SPENT THIS MONTH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textMuted,
                                letterSpacing = 1.2.sp
                            )

                            val statusColor = when {
                                percentUsed > 95 -> AccentRed
                                percentUsed > 75 -> AccentAmber
                                else -> AccentEmerald
                            }
                            val statusText = when {
                                percentUsed > 95 -> "Over Budget ($percentUsed%)"
                                percentUsed > 75 -> "Caution ($percentUsed%)"
                                else -> "On Track ($percentUsed%)"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(statusColor.copy(alpha = 0.12f))
                                    .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "● $statusText",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }

                        // Giant Typographic Spend
                        Text(
                            text = currencyFormat.format(totalExpense),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = textPrimary,
                            letterSpacing = (-0.5).sp
                        )

                        // Visual Progress Bar
                        LinearProgressIndicator(
                            progress = { (percentUsed / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (percentUsed > 95) AccentRed else if (percentUsed > 75) AccentAmber else AccentEmerald,
                            trackColor = itemBg
                        )

                        // Budget Limit & Daily Safe Allowance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Remaining of ${currencyFormat.format(monthlyBudget)}", fontSize = 12.sp, color = textMuted)
                                Text(
                                    text = currencyFormat.format(remainingBudget),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = textPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Safe daily allowance", fontSize = 12.sp, color = textMuted)
                                Text(
                                    text = "${currencyFormat.format(dailyBurnRate)}/day",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = AccentSky
                                )
                            }
                        }

                        Divider(color = borderColor.copy(alpha = 0.4f), thickness = 0.5.dp)

                        // 30-Day Daily Spend Strip (Bento Hero Tile style)
                        BentoDailyStrip(transactions = recentTransactions)
                    }
                }
            }

            // 2. Bento Pacing Speedometer Gauge
            item {
                BentoPacingGauge(
                    percentUsed = percentUsed,
                    dailyBurnRate = dailyBurnRate,
                    daysLeft = daysLeft
                )
            }

            // 3. Bento 3D Bank Cards Stack (ICICI Credit Card **2006, Savings, Cash)
            item {
                BentoCardStack(transactions = recentTransactions)
            }

            // 4. Quick Action Toolbar: Floating Overlay Pill & Manual Add
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Floating Overlay Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isFloatingBubbleActive) AccentEmerald.copy(alpha = 0.15f) else surfaceColor)
                            .border(1.dp, if (isFloatingBubbleActive) AccentEmerald else borderColor, RoundedCornerShape(16.dp))
                            .clickable { onToggleFloatingBubble() }
                            .padding(vertical = 12.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (isFloatingBubbleActive) "🟢" else "⚡", fontSize = 14.sp)
                            Column {
                                Text(
                                    text = if (isFloatingBubbleActive) "Floating Pill: ON" else "Floating Pill: OFF",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFloatingBubbleActive) AccentEmerald else textPrimary
                                )
                                Text("Show over all apps", fontSize = 10.sp, color = textMuted)
                            }
                        }
                    }

                    // Log Cash Quick Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(surfaceColor)
                            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable { onAddTransactionClick() }
                            .padding(vertical = 12.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💵", fontSize = 16.sp)
                            Column {
                                Text("Log Cash Expense", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                                Text("Manual entry", fontSize = 10.sp, color = textMuted)
                            }
                        }
                    }
                }
            }

            // 5. Bento Category Allocation (Full Breakdown of ALL Categories)
            item {
                BentoAllocationBar(
                    totalExpense = totalExpense,
                    categories = categories,
                    categorySpends = categorySpends
                )
            }

            // 6. Recent Transactions Stream with Tap-to-Recategorize
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recent Transactions",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Tap any transaction to change category",
                            fontSize = 11.sp,
                            color = textMuted
                        )
                    }
                    Text(
                        text = "View All →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCobalt,
                        modifier = Modifier.clickable { onNavigateToTransactions() }
                    )
                }
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Text("No transactions found.", fontSize = 13.sp, color = textMuted)
                }
            } else {
                items(recentTransactions.take(8), key = { it.id }) { txn ->
                    val cat = categoryMap[txn.categoryId]
                    val isExpense = txn.type == TransactionType.EXPENSE
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingTransaction = txn },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = itemBg)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, borderColor, RoundedCornerShape(18.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(surfaceColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = cat?.iconEmoji ?: "💳", fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = txn.merchant,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${cat?.name ?: "Other"} • ${txn.accountName}",
                                        fontSize = 11.sp,
                                        color = textMuted
                                    )
                                    if (txn.isAutoCategorized) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("⚡ SMS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AccentEmerald)
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = (if (isExpense) "- " else "+ ") + currencyFormat.format(txn.amount),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isExpense) textPrimary else AccentEmerald
                                )

                                IconButton(
                                    onClick = { onDeleteTransaction(txn) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = textMuted.copy(alpha = 0.6f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Modal Dialog to edit category when tapped
    editingTransaction?.let { txn ->
        EditCategoryDialog(
            transaction = txn,
            categories = categories,
            onDismiss = { editingTransaction = null },
            onCategorySelected = { newCatId ->
                onUpdateTransactionCategory(txn, newCatId)
            }
        )
    }
}
