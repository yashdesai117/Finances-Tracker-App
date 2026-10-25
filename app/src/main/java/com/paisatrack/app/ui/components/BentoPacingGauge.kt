package com.paisatrack.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paisatrack.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BentoPacingGauge(
    percentUsed: Int,
    dailyBurnRate: Double,
    daysLeft: Int,
    modifier: Modifier = Modifier
) {
    val targetT = (percentUsed / 100f).coerceIn(0f, 1f)
    val animatedT by animateFloatAsState(
        targetValue = targetT,
        animationSpec = tween(durationMillis = 1000),
        label = "gauge"
    )

    val gaugeColor = when {
        percentUsed > 95 -> AccentRed
        percentUsed > 75 -> AccentAmber
        else -> AccentEmerald
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val trackArcColor = if (isDark) Color(0x22FFFFFF) else Color(0x1F000000)
    val needleColor = if (isDark) Color.White else Color(0xFF0F172A)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(surfaceColor)
            .border(1.dp, borderColor, RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BUDGET VELOCITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMuted,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "$daysLeft days left",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Semicircle S-Curve Gauge
            Box(
                modifier = Modifier
                    .size(width = 170.dp, height = 95.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, (size.height * 2) - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Track Arc (180 degrees from left to right)
                    drawArc(
                        color = trackArcColor,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Fill Arc
                    drawArc(
                        color = gaugeColor,
                        startAngle = 180f,
                        sweepAngle = 180f * animatedT,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Needle
                    val angleRad = Math.toRadians((180f + (180f * animatedT)).toDouble())
                    val centerX = size.width / 2
                    val centerY = size.height - (strokeWidth / 4)
                    val needleLength = size.height - 24.dp.toPx()

                    val endX = centerX + (needleLength * cos(angleRad)).toFloat()
                    val endY = centerY + (needleLength * sin(angleRad)).toFloat()

                    drawLine(
                        color = needleColor,
                        start = Offset(centerX, centerY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Pivot circle
                    drawCircle(
                        color = needleColor,
                        radius = 5.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "₹%,d / day".format(dailyBurnRate.toInt()),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = AccentSky
            )

            Text(
                text = if (percentUsed > 95) "Pacing too fast • Reduce spend" else "Safe spending pace to hit target",
                fontSize = 11.sp,
                color = textMuted
            )
        }
    }
}
