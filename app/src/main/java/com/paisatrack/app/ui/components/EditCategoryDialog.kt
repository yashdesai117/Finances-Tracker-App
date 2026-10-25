package com.paisatrack.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.paisatrack.app.data.model.Category
import com.paisatrack.app.data.model.Transaction
import com.paisatrack.app.ui.theme.*

@Composable
fun EditCategoryDialog(
    transaction: Transaction,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val surfaceColor = MaterialTheme.colorScheme.surface
    val itemBgColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9)
    val borderColor = MaterialTheme.colorScheme.outline
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderColor, RoundedCornerShape(26.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "Change Category",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "${transaction.merchant} (₹%,d)".format(transaction.amount.toInt()),
                        fontSize = 13.sp,
                        color = textMuted
                    )
                }

                val availableCats = categories.filter { it.isIncome == (transaction.type == com.paisatrack.app.data.model.TransactionType.INCOME) }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 340.dp)
                ) {
                    items(availableCats) { cat ->
                        val isSelected = cat.id == transaction.categoryId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) AccentCobalt else itemBgColor)
                                .border(1.dp, if (isSelected) AccentCobalt else borderColor, RoundedCornerShape(14.dp))
                                .clickable {
                                    onCategorySelected(cat.id)
                                    onDismiss()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(cat.iconEmoji, fontSize = 16.sp)
                                Text(
                                    text = cat.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else textPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = textMuted)
                    }
                }
            }
        }
    }
}
