package com.paisatrack.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paisatrack.app.data.model.Category
import com.paisatrack.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    currentBudget: Double,
    categories: List<Category>,
    onSaveBudget: (Double) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var budgetInput by remember(currentBudget) { mutableStateOf(currentBudget.toInt().toString()) }
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }
    }

    Scaffold(
        containerColor = CanvasDark,
        topBar = {
            TopAppBar(
                title = { Text("Budget & Home Widget", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CanvasDark)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Overall Monthly Target Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "MONTHLY SPENDING LIMIT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "This sets the target shown on your phone's Home Screen Widget and dashboard.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = budgetInput,
                            onValueChange = { budgetInput = it },
                            label = { Text("Monthly Budget (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = BorderDark
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val newBudget = budgetInput.toDoubleOrNull() ?: return@Button
                                onSaveBudget(newBudget)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Save Budget Target", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Widget Tutorial Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderDark, RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "📱 How to add the Widget to your Phone's Home Screen",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentSky
                        )
                        Text(
                            text = "1. Go to your phone's home screen.\n" +
                                   "2. Long-press on any empty space.\n" +
                                   "3. Tap 'Widgets' from the popup menu.\n" +
                                   "4. Search or scroll to 'PaisaTrack'.\n" +
                                   "5. Drag and drop 'Monthly Spend Tracker' onto your home screen!\n" +
                                   "The widget will stay permanently visible, showing your monthly spend, budget progress, and daily safe allowance.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Default Category Allocations
            item {
                Text(
                    text = "Category Budget Caps",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(categories.filter { !it.isIncome }) { cat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceDark)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = cat.iconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = cat.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (cat.defaultBudget > 0) currencyFormat.format(cat.defaultBudget) else "Flexible",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
