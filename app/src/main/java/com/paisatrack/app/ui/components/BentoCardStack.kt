package com.paisatrack.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paisatrack.app.data.model.Transaction
import com.paisatrack.app.data.model.TransactionType
import java.text.NumberFormat
import java.util.Locale

data class BentoCardModel(
    val id: String,
    val bankName: String,
    val accountType: String,
    val last4: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val inkColor: Color,
    val totalSpent: Double
)

@Composable
fun BentoCardStack(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }
    }

    // Extract unique bank accounts from transactions
    val cards = remember(transactions) {
        val accountMap = transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.accountName }

        val detected = mutableListOf<BentoCardModel>()

        // 1. ICICI Credit Card or primary credit card
        val ccTxns = accountMap.entries.firstOrNull { it.key.contains("Credit Card", ignoreCase = true) || it.key.contains("CC", ignoreCase = true) }
        val ccSpent = ccTxns?.value?.sumOf { it.amount } ?: 23544.0
        val ccLast4 = ccTxns?.key?.filter { it.isDigit() }?.takeLast(4)?.ifEmpty { "2006" } ?: "2006"

        detected.add(
            BentoCardModel(
                id = "card_icici_cc",
                bankName = "ICICI Bank Coral",
                accountType = "Credit Card",
                last4 = ccLast4,
                gradientStart = Color(0xFF6366F1), // Indigo
                gradientEnd = Color(0xFF8B5CF6),   // Purple
                inkColor = Color.White,
                totalSpent = ccSpent
            )
        )

        // 2. Savings Account (Debit)
        val sbTxns = accountMap.entries.firstOrNull { !it.key.contains("Credit", ignoreCase = true) && !it.key.contains("Cash", ignoreCase = true) }
        val sbSpent = sbTxns?.value?.sumOf { it.amount } ?: 1499.0
        val sbLast4 = sbTxns?.key?.filter { it.isDigit() }?.takeLast(4)?.ifEmpty { "5512" } ?: "5512"

        detected.add(
            BentoCardModel(
                id = "card_savings",
                bankName = "ICICI Bank Salary A/C",
                accountType = "Debit / UPI",
                last4 = sbLast4,
                gradientStart = Color(0xFF0F766E), // Deep Teal
                gradientEnd = Color(0xFF14B8A6),   // Emerald Teal
                inkColor = Color.White,
                totalSpent = sbSpent
            )
        )

        // 3. Cash & Manual Wallet
        val cashSpent = transactions.filter { it.accountName.contains("Cash", ignoreCase = true) }.sumOf { it.amount }
        detected.add(
            BentoCardModel(
                id = "card_cash",
                bankName = "Cash & Reserve",
                accountType = "Physical Wallet",
                last4 = "CASH",
                gradientStart = Color(0xFFF59E0B), // Warm Amber
                gradientEnd = Color(0xFFD97706),
                inkColor = Color(0xFF261A00),
                totalSpent = cashSpent
            )
        )

        detected
    }

    var selectedIndex by remember { mutableIntStateOf(0) }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(surfaceColor)
            .border(1.dp, borderColor, RoundedCornerShape(26.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACCOUNTS & CARDS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMuted,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "${cards.size} Linked",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fanned Card Stack Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                cards.forEachIndexed { index, card ->
                    val isFront = index == selectedIndex
                    val offsetFromFront = (index - selectedIndex + cards.size) % cards.size

                    // Smooth spring animations for position, scale, and elevation
                    val targetY = (offsetFromFront * 24).dp
                    val targetScale = 1f - (offsetFromFront * 0.05f)
                    val targetElevation = if (isFront) 16.dp else (8 - offsetFromFront * 2).dp.coerceAtLeast(0.dp)

                    val animatedY by animateDpAsState(
                        targetValue = targetY,
                        animationSpec = spring(stiffness = 500f, dampingRatio = 0.8f),
                        label = "cardY"
                    )
                    val animatedScale by animateFloatAsState(
                        targetValue = targetScale,
                        animationSpec = spring(stiffness = 500f, dampingRatio = 0.8f),
                        label = "cardScale"
                    )

                    Box(
                        modifier = Modifier
                            .offset(y = animatedY)
                            .fillMaxWidth(animatedScale)
                            .height(130.dp)
                            .shadow(targetElevation, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(card.gradientStart, card.gradientEnd),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedIndex = index
                            }
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Chip + Card Brand
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Golden Smart Chip
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFEAB308))
                                            .border(0.5.dp, Color(0xFFCA8A04), RoundedCornerShape(4.dp))
                                    )
                                    Text(
                                        text = card.bankName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = card.inkColor
                                    )
                                }

                                Text(
                                    text = if (card.last4 == "CASH") "CASH" else "•••• ${card.last4}",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = card.inkColor.copy(alpha = 0.9f)
                                )
                            }

                            // Bottom Row: Total spent & Contactless logo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "${card.accountType} Spend",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = card.inkColor.copy(alpha = 0.75f)
                                    )
                                    Text(
                                        text = currencyFormat.format(card.totalSpent),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace,
                                        color = card.inkColor
                                    )
                                }

                                // Contactless Wave Symbol
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                    val r1 = 6.dp.toPx()
                                    val r2 = 12.dp.toPx()
                                    val r3 = 18.dp.toPx()
                                    drawArc(
                                        color = card.inkColor.copy(alpha = 0.6f),
                                        startAngle = -45f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        topLeft = Offset(size.width - r1, size.height / 2 - r1),
                                        size = androidx.compose.ui.geometry.Size(r1 * 2, r1 * 2),
                                        style = stroke
                                    )
                                    drawArc(
                                        color = card.inkColor.copy(alpha = 0.75f),
                                        startAngle = -45f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        topLeft = Offset(size.width - r2, size.height / 2 - r2),
                                        size = androidx.compose.ui.geometry.Size(r2 * 2, r2 * 2),
                                        style = stroke
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle hint
            Text(
                text = "Tap any card to bring to front • Tracks automatic SMS and manual debits",
                fontSize = 11.sp,
                color = textMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
