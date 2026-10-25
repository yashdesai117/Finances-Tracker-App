package com.paisatrack.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.paisatrack.app.sms.SmsHistoricalScanner
import com.paisatrack.app.sms.SmsParserEngine
import com.paisatrack.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSyncScreen(
    onBackClick: () -> Unit,
    onHistoricalSyncComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasSmsPermission = permissions[Manifest.permission.RECEIVE_SMS] == true &&
                permissions[Manifest.permission.READ_SMS] == true
    }

    var isScanning by remember { mutableStateOf(false) }
    var scanResultText by remember { mutableStateOf<String?>(null) }
    var scanProgress by remember { mutableStateOf(0f) }

    // Manual test SMS input
    var testSmsInput by remember { mutableStateOf("") }
    var testParseResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = CanvasDark,
        topBar = {
            TopAppBar(
                title = { Text("SMS Tracking & Sync", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // Privacy Shield Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard)
                        .border(1.dp, AccentEmerald.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🔒", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "100% Offline & Private",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "PaisaTrack has NO internet permission. Your banking SMS, transaction amounts, and financial history never leave this phone.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Permission Status Card
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
                            text = "AUTOMATIC SMS TRACKING",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        if (hasSmsPermission) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SMS Permission Granted",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentEmerald
                                )
                            }
                            Text(
                                text = "PaisaTrack will automatically detect debit & credit SMS from HDFC, SBI, ICICI, Axis, GPay, Paytm, etc., and update your Home Screen Widget instantly.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        } else {
                            Text(
                                text = "To automatically categorize transactions and update your home screen widget whenever you spend, please grant SMS permission.",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Button(
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.RECEIVE_SMS,
                                            Manifest.permission.READ_SMS
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Grant SMS Permission", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Historical Scan Card
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
                            text = "IMPORT PAST EXPENSES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Scan your SMS inbox for the last 60 days to backfill this month's spending and initialize your widget immediately.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        if (isScanning) {
                            LinearProgressIndicator(
                                progress = { scanProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = AccentBlue,
                                trackColor = BorderDark
                            )
                        } else {
                            Button(
                                onClick = {
                                    if (!hasSmsPermission) {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.RECEIVE_SMS,
                                                Manifest.permission.READ_SMS
                                            )
                                        )
                                        return@Button
                                    }
                                    isScanning = true
                                    scanProgress = 0.1f
                                    scope.launch {
                                        val added = SmsHistoricalScanner.scanHistoricalSms(
                                            context = context,
                                            daysBack = 60
                                        ) { current, total ->
                                            if (total > 0) scanProgress = current.toFloat() / total
                                        }
                                        isScanning = false
                                        scanResultText = "Successfully imported $added transactions from your SMS inbox!"
                                        onHistoricalSyncComplete()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Scan Past 60 Days SMS", fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }

                        if (scanResultText != null) {
                            Text(
                                text = scanResultText!!,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentEmerald
                            )
                        }
                    }
                }
            }

            // Test SMS Parser Sandbox
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
                            text = "SMS PARSER TESTBENCH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Paste any bank SMS here to test how the parser detects amounts and categories:",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = testSmsInput,
                            onValueChange = { testSmsInput = it },
                            placeholder = { Text("e.g. INR 450.00 debited from A/C **1234 on 25-Oct towards SWIGGY", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = BorderDark
                            )
                        )

                        Button(
                            onClick = {
                                val parsed = SmsParserEngine.parse("HDFCBK", testSmsInput)
                                testParseResult = if (parsed != null) {
                                    "✅ Detected: ${parsed.type}\nAmount: ₹${parsed.amount}\nMerchant: ${parsed.merchant}\nCategory: ${parsed.categoryId.uppercase()}"
                                } else {
                                    "❌ Could not detect financial transaction in this text."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Test Parsing", fontWeight = FontWeight.Bold)
                        }

                        if (testParseResult != null) {
                            Text(
                                text = testParseResult!!,
                                fontSize = 13.sp,
                                color = if (testParseResult!!.startsWith("✅")) AccentEmerald else AccentRed,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
