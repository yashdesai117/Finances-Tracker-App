package com.paisatrack.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paisatrack.app.data.model.Transaction
import com.paisatrack.app.data.model.TransactionType
import com.paisatrack.app.ui.theme.AccentCobalt
import com.paisatrack.app.ui.theme.AccentEmerald
import com.paisatrack.app.ui.theme.AccentSky
import java.text.NumberFormat
import java.util.*

@Composable
fun BentoDailyStrip(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }
    }

    val calendar = Calendar.getInstance()
    val todayDay = calendar.get(Calendar.DAY_OF_MONTH)
    val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Calculate daily spend for each day (1..totalDays)
    val dailySpend = remember(transactions) {
        val days = DoubleArray(totalDays + 1)
        val cal = Calendar.getInstance()
        transactions.filter { it.type == TransactionType.EXPENSE }.forEach { txn ->
            cal.timeInMillis = txn.timestamp
            val day = cal.get(Calendar.DAY_OF_MONTH)
            if (day in 1..totalDays) {
                days[day] += txn.amount
            }
        }
        days
    }

    val maxDailySpend = remember(dailySpend) {
        dailySpend.maxOrNull()?.coerceAtLeast(100.0) ?: 100.0
    }

    var selectedDay by remember { mutableStateOf<Int?>(todayDay) }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val barInactive = if (isDark) Color(0x22FFFFFF) else Color(0x1F000000)

    Column(modifier = modifier.fillMaxWidth()) {
        // Daily Activity Strip Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            for (day in 1..totalDays) {
                val spend = dailySpend[day]
                val isToday = day == todayDay
                val isPast = day <= todayDay
                val isSelected = selectedDay == day

                val fraction = if (isPast && spend > 0) {
                    (spend / maxDailySpend).toFloat().coerceIn(0.15f, 1f)
                } else if (isPast) {
                    0.08f
                } else {
                    0.04f
                }

                val animatedHeight by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = tween(600),
                    label = "bar_$day"
                )

                val barColor = when {
                    isSelected -> AccentCobalt
                    isToday -> AccentSky
                    spend > 0 -> AccentEmerald
                    isPast -> barInactive
                    else -> barInactive.copy(alpha = 0.04f)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(animatedHeight)
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            selectedDay = day
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Day Label & Spend detail
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Day 1",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = textMuted
            )

            val day = selectedDay ?: todayDay
            val daySpend = dailySpend[day]
            Text(
                text = if (day == todayDay) "Today (Day $day): ${currencyFormat.format(daySpend)}"
                       else "Day $day: ${currencyFormat.format(daySpend)}",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Day $totalDays",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = textMuted
            )
        }
    }
}
