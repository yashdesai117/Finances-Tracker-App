package com.paisatrack.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.paisatrack.app.sms.SmsHistoricalScanner
import com.paisatrack.app.sms.SmsParserEngine
import com.paisatrack.app.ui.theme.*
import com.paisatrack.app.update.AppUpdateManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsSyncScreen(
    isFloatingBubbleActive: Boolean,
    onToggleFloatingBubble: () -> Unit,
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
    var scanStatusMessage by remember { mutableStateOf("") }

    // In-app updater state
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<AppUpdateManager.UpdateInfo?>(null) }

    // Manual test SMS input
    var testSmsInput by remember { mutableStateOf("") }
    var testParseResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = CanvasDark,
        topBar = {
            TopAppBar(
                title = { Text("Settings & Sync", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Floating Overlay Pill Across All Apps
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderDark, RoundedCornerShape(22.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Floating Spending Pill",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Keeps your monthly spend visible across ALL screens & apps (WhatsApp, Instagram, etc.)",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                            Switch(
                                checked = isFloatingBubbleActive,
                                onCheckedChange = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        onToggleFloatingBubble()
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextPrimary,
                                    checkedTrackColor = AccentEmerald
                                )
                            )
                        }
                    }
                }
            }

            // Section 2: In-App 1-Tap Auto-Updater (No manual APK downloads)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderDark, RoundedCornerShape(22.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instant In-App Updates",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Current App Version: v${AppUpdateManager.CURRENT_VERSION}. Updates install directly inside the app with 1 tap.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        if (isCheckingUpdates) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = AccentCobalt
                            )
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = {
                                        isCheckingUpdates = true
                                        scope.launch {
                                            val info = AppUpdateManager.checkForUpdates()
                                            isCheckingUpdates = false
                                            updateResult = info
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Check for Updates", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }

                                if (updateResult?.isUpdateAvailable == true && updateResult?.apkUrl != null) {
                                    Button(
                                        onClick = {
                                            AppUpdateManager.downloadAndInstall(context, updateResult!!.apkUrl!!)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Update Now (v${updateResult!!.latestVersion})", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (updateResult != null) {
                            Text(
                                text = if (updateResult!!.isUpdateAvailable)
                                    "✨ New version v${updateResult!!.latestVersion} ready to install!"
                                else
                                    "✓ You are already on the latest version.",
                                fontSize = 12.sp,
                                color = if (updateResult!!.isUpdateAvailable) AccentEmerald else TextMuted
                            )
                        }
                    }
                }
            }

            // Section 3: SMS Inbox Scanning (Full History Scan & Clean Rescan)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderDark, RoundedCornerShape(22.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "SMS INBOX SCANNER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCobalt,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Scans all bank messages in your inbox. Use 'Clear & Full Rescan' to wipe any previously misclassified promo messages and scan everything cleanly.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            lineHeight = 17.sp
                        )

                        if (isScanning) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(scanStatusMessage, fontSize = 12.sp, color = AccentSky)
                                LinearProgressIndicator(
                                    progress = { scanProgress },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = AccentCobalt,
                                    trackColor = SurfaceCard
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Scan All Messages
                                Button(
                                    onClick = {
                                        if (!hasSmsPermission) {
                                            permissionLauncher.launch(
                                                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                            )
                                            return@Button
                                        }
                                        isScanning = true
                                        scanProgress = 0.05f
                                        scanStatusMessage = "Reading inbox..."
                                        scope.launch {
                                            val added = SmsHistoricalScanner.scanHistoricalSms(
                                                context = context,
                                                daysBack = 0, // 0 = entire inbox
                                                clearExisting = false
                                            ) { current, total, addedCount ->
                                                if (total > 0) scanProgress = current.toFloat() / total
                                                scanStatusMessage = "Scanned $current of $total messages ($addedCount transactions found)..."
                                            }
                                            isScanning = false
                                            scanResultText = "Done! Found & recorded $added new transactions."
                                            onHistoricalSyncComplete()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentCobalt),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Scan All SMS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                // Clear & Full Rescan
                                Button(
                                    onClick = {
                                        if (!hasSmsPermission) {
                                            permissionLauncher.launch(
                                                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                            )
                                            return@Button
                                        }
                                        isScanning = true
                                        scanProgress = 0.05f
                                        scanStatusMessage = "Clearing old data & scanning all inbox messages..."
                                        scope.launch {
                                            val added = SmsHistoricalScanner.scanHistoricalSms(
                                                context = context,
                                                daysBack = 0, // 0 = entire inbox
                                                clearExisting = true
                                            ) { current, total, addedCount ->
                                                if (total > 0) scanProgress = current.toFloat() / total
                                                scanStatusMessage = "Scanned $current of $total messages ($addedCount clean transactions)..."
                                            }
                                            isScanning = false
                                            scanResultText = "Clean scan complete! Imported $added valid transactions."
                                            onHistoricalSyncComplete()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Clear & Rescan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }

                        if (scanResultText != null) {
                            Text(
                                text = scanResultText!!,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentEmerald
                            )
                        }
                    }
                }
            }

            // Section 4: Live SMS Parser Sandbox (Test Promo rejection)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderDark, RoundedCornerShape(22.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "TEST MESSAGE PARSER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentCobalt,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Paste any bank SMS or promotional offer below to verify how it gets parsed or blocked:",
                            fontSize = 12.sp,
                            color = TextMuted
                        )

                        OutlinedTextField(
                            value = testSmsInput,
                            onValueChange = { testSmsInput = it },
                            placeholder = { Text("e.g. Paste EazyDiner or HDFC message here...", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = AccentCobalt,
                                unfocusedBorderColor = BorderDark
                            )
                        )

                        Button(
                            onClick = {
                                val parsed = SmsParserEngine.parse("BANK", testSmsInput)
                                testParseResult = if (parsed != null) {
                                    "✅ VALID TRANSACTION\nType: ${parsed.type}\nAmount: ₹${parsed.amount}\nMerchant: ${parsed.merchant}\nCategory: ${parsed.categoryId.uppercase()}"
                                } else {
                                    "🛡️ REJECTED: Classified as non-transaction, promotional offer, discount coupon, or OTP."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCobalt),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Test Message", fontWeight = FontWeight.Bold)
                        }

                        if (testParseResult != null) {
                            Text(
                                text = testParseResult!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (testParseResult!!.startsWith("✅")) AccentEmerald else AccentAmber,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
