package com.paisatrack.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Refresh
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
import com.paisatrack.app.ui.components.StatCard
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
    onAddTransactionClick: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToSmsSync: () -> Unit,
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
                containerColor = AccentBlue,
                contentColor = TextPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PaisaTrack",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
                            fontSize = 14.sp,
                            color = TextMuted
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSmsSync,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceDark)
                    ) {
                        Text("📩", fontSize = 18.sp)
                    }
                }
            }

            // Hero Spending Card (Mirrors the Home Screen Widget)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceCard)
                        .border(1.5.dp, BorderDark, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "THIS MONTH'S SPEND",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                                letterSpacing = 1.sp
                            )
                            val statusText = when {
                                percentUsed > 95 -> "● Over Budget"
                                percentUsed > 75 -> "● Caution ($percentUsed%)"
                                else -> "● On Track ($percentUsed%)"
                            }
                            val statusColor = when {
                                percentUsed > 95 -> AccentRed
                                percentUsed > 75 -> AccentAmber
                                else -> AccentEmerald
                            }
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Text(
                            text = currencyFormat.format(totalExpense),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        LinearProgressIndicator(
                            progress = { (percentUsed / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (percentUsed > 95) AccentRed else if (percentUsed > 75) AccentAmber else AccentEmerald,
                            trackColor = BorderDark
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Budget: ${currencyFormat.format(monthlyBudget)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${currencyFormat.format(dailyBurnRate)}/day safe left",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentSky
                            )
                        }
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Income",
                        value = currencyFormat.format(totalIncome),
                        subtitle = "This Month",
                        valueColor = AccentEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Net Savings",
                        value = currencyFormat.format((totalIncome - totalExpense).coerceAtLeast(0.0)),
                        subtitle = "${if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100).toInt() else 0}% saved",
                        valueColor = AccentBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Category Spend Breakdown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spending by Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            if (categorySpends.isEmpty()) {
                item {
                    Text(
                        text = "No expenses recorded this month yet. Incoming bank SMS will automatically appear here!",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
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

            // Recent Transactions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "View All",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue,
                        modifier = Modifier.clickable { onNavigateToTransactions() }
                    )
                }
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Text(
                        text = "No recent transactions found.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(recentTransactions.take(8)) { txn ->
                    val cat = categoryMap[txn.categoryId]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CanvasDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = cat?.iconEmoji ?: "💳", fontSize = 20.sp)
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
                                    text = dateFormat.format(Date(txn.timestamp)),
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                if (txn.isAutoCategorized) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "⚡ SMS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentEmerald
                                    )
                                }
                            }
                        }

                        Text(
                            text = (if (txn.type == TransactionType.EXPENSE) "- " else "+ ") + currencyFormat.format(txn.amount),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (txn.type == TransactionType.EXPENSE) TextPrimary else AccentEmerald
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp)) // Padding for FAB
            }
        }
    }
}
