package com.paisatrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paisatrack.app.data.model.Category
import com.paisatrack.app.data.model.CategorySpend
import com.paisatrack.app.data.model.Transaction
import com.paisatrack.app.data.model.TransactionType
import com.paisatrack.app.ui.components.CategorySpendBar
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
    onToggleFloatingBubble: () -> Unit,
    onAddTransactionClick: () -> Unit,
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

    Scaffold(
        containerColor = CanvasDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = AccentCobalt,
                contentColor = TextPrimary,
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header: App Title, Month & Navigation icons
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
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Budget Settings Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark)
                                .border(1.dp, BorderDark, CircleShape)
                                .clickable { onNavigateToBudgets() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎯", fontSize = 16.sp)
                        }

                        // SMS Sync & Full Scan Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark)
                                .border(1.dp, BorderDark, CircleShape)
                                .clickable { onNavigateToSmsSync() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📩", fontSize = 16.sp)
                        }
                    }
                }
            }

            // Hero Card: Revolut-Style Spending Balance & Gauge
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderDark, RoundedCornerShape(28.dp))
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL SPENT THIS MONTH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
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

                        // Giant typographic spend value
                        Text(
                            text = currencyFormat.format(totalExpense),
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
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
                            trackColor = SurfaceDark
                        )

                        // Budget Limit & Daily Safe Allowance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Remaining of ${currencyFormat.format(monthlyBudget)}", fontSize = 12.sp, color = TextMuted)
                                Text(currencyFormat.format(remainingBudget), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Safe daily allowance", fontSize = 12.sp, color = TextMuted)
                                Text("${currencyFormat.format(dailyBurnRate)}/day", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AccentSky)
                            }
                        }
                    }
                }
            }

            // Quick Action Toolbar: Floating Overlay Pill & Manual Add
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
                            .background(if (isFloatingBubbleActive) AccentEmerald.copy(alpha = 0.15f) else SurfaceDark)
                            .border(1.dp, if (isFloatingBubbleActive) AccentEmerald else BorderDark, RoundedCornerShape(16.dp))
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
                                    color = if (isFloatingBubbleActive) AccentEmerald else TextPrimary
                                )
                                Text("Show over all apps", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }

                    // Log Cash Quick Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                            .clickable { onAddTransactionClick() }
                            .padding(vertical = 12.dp, horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("💵", fontSize = 16.sp)
                            Column {
                                Text("Log Cash Expense", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Manual entry", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            // Spending by Category Breakdown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spending Breakdown",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            if (categorySpends.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .border(1.dp, BorderDark, RoundedCornerShape(16.dp))
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "No expenses recorded this month yet. Your bank SMS will automatically appear here!",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(categorySpends) { spend ->
                    val cat = categoryMap[spend.categoryId]
                    CategorySpendBar(
                        categoryName = cat?.name ?: spend.categoryId.replaceFirstChar { it.uppercase() },
                        iconEmoji = cat?.iconEmoji ?: "🏷️",
                        spentAmount = spend.totalAmount,
                        budgetAmount = cat?.defaultBudget ?: 0.0,
                        colorHex = cat?.colorHex ?: "#3B82F6"
                    )
                }
            }

            // Recent Transactions Stream
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
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
                    Text("No transactions found.", fontSize = 13.sp, color = TextMuted)
                }
            } else {
                items(recentTransactions.take(8), key = { it.id }) { txn ->
                    val cat = categoryMap[txn.categoryId]
                    val isExpense = txn.type == TransactionType.EXPENSE
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderDark, RoundedCornerShape(18.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceCard),
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
                                    color = TextPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${cat?.name ?: "Other"} • ${txn.accountName}",
                                        fontSize = 11.sp,
                                        color = TextMuted
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
                                    color = if (isExpense) TextPrimary else AccentEmerald
                                )

                                // Instant Delete / Not a transaction action
                                IconButton(
                                    onClick = { onDeleteTransaction(txn) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = TextMuted.copy(alpha = 0.6f),
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
}
