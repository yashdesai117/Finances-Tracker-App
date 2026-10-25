package com.paisatrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BentoAllocationBar(
    totalExpense: Double,
    categories: List<Category>,
    categorySpends: List<CategorySpend>,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    val spendMap = remember(categorySpends) {
        categorySpends.associate { it.categoryId to it.totalAmount }
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val rowBgColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFF1F5F9)
    val borderColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Ensure all default categories are included even if not yet spent
    val allExpenseCategories = remember(categories) {
        val list = if (categories.isEmpty()) Category.DEFAULT_CATEGORIES else categories
        list.filter { !it.isIncome }
    }

    // Sort categories: Active categories first (highest spend), then others
    val sortedCategories = remember(allExpenseCategories, spendMap) {
        allExpenseCategories.sortedWith(
            compareByDescending<Category> { spendMap[it.id] ?: 0.0 }
                .thenBy { it.name }
        )
    }

    val activeCount = sortedCategories.count { (spendMap[it.id] ?: 0.0) > 0 }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(surfaceColor)
            .border(1.dp, borderColor, RoundedCornerShape(26.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CATEGORY ALLOCATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMuted,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "$activeCount/${sortedCategories.size} Active",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Segmented Horizontal Bar (Bento AllocationTile style)
            if (totalExpense > 0 && categorySpends.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(rowBgColor),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    categorySpends.forEach { spend ->
                        val cat = categories.find { it.id == spend.categoryId }
                        val catColor = try {
                            Color(android.graphics.Color.parseColor(cat?.colorHex ?: "#64748B"))
                        } catch (e: Exception) {
                            Color(0xFF6366F1)
                        }
                        val weight = (spend.totalAmount / totalExpense).toFloat().coerceAtLeast(0.02f)

                        Box(
                            modifier = Modifier
                                .weight(weight)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(catColor)
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(borderColor)
                )
            }

            // Full List of ALL Categories (Food, Groceries, Shopping, Transport, Bills, etc.)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sortedCategories.forEach { cat ->
                    val spent = spendMap[cat.id] ?: 0.0
                    val percent = if (totalExpense > 0) ((spent / totalExpense) * 100).toInt() else 0
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(cat.colorHex))
                    } catch (e: Exception) {
                        Color(0xFF6366F1)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (spent > 0) rowBgColor else Color.Transparent)
                            .border(
                                1.dp,
                                if (spent > 0) borderColor else Color.Transparent,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(catColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(cat.iconEmoji, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cat.name,
                                fontSize = 13.sp,
                                fontWeight = if (spent > 0) FontWeight.Bold else FontWeight.Medium,
                                color = if (spent > 0) textPrimary else textMuted
                            )
                            if (cat.defaultBudget > 0) {
                                Text(
                                    text = "Budget: ${currencyFormat.format(cat.defaultBudget)}",
                                    fontSize = 10.sp,
                                    color = textMuted
                                )
                            }
                        }

                        if (spent > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = currencyFormat.format(spent),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = textPrimary
                                )
                                Text(
                                    text = "$percent% of month",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (percent > 40) Color(0xFFF43F5E) else MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Text(
                                text = "₹0",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
