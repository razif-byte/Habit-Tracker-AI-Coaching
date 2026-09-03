package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.receiver.BootCompletedReceiver
import com.example.service.OverlayBubbleService
import com.example.ui.viewmodel.HabitViewModel
import com.example.util.AppIntegrationHelper
import com.example.util.DetectedAppUsage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppActivityMonitorScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activitySummary by viewModel.dailyActivitySummary.collectAsStateWithLifecycle()
    val isOverlayActive by viewModel.isOverlayActive.collectAsStateWithLifecycle()

    var isOverlayPermissionGranted by remember { mutableStateOf(AppIntegrationHelper.isOverlayPermissionGranted(context)) }
    var isUsagePermissionGranted by remember { mutableStateOf(AppIntegrationHelper.isUsageAccessPermissionGranted(context)) }

    val prefs = remember { context.getSharedPreferences(BootCompletedReceiver.PREFS_NAME, Context.MODE_PRIVATE) }
    var autostartBoot by remember { mutableStateOf(prefs.getBoolean(BootCompletedReceiver.KEY_AUTOSTART_OVERLAY, true)) }

    LaunchedEffect(Unit) {
        viewModel.refreshAppActivityUsage()
        isOverlayPermissionGranted = AppIntegrationHelper.isOverlayPermissionGranted(context)
        isUsagePermissionGranted = AppIntegrationHelper.isUsageAccessPermissionGranted(context)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PENGESAN SISTEM & OVERLAY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Pengesan Aktiviti & Latar Belakang",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.refreshAppActivityUsage()
                            isOverlayPermissionGranted = AppIntegrationHelper.isOverlayPermissionGranted(context)
                            isUsagePermissionGranted = AppIntegrationHelper.isUsageAccessPermissionGranted(context)
                        },
                        modifier = Modifier.testTag("btn_refresh_activity")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat Semula")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Floating Overlay Controller Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Floating Overlay Window",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isOverlayActive) "Aktif terapung atas semua apps" else "Tidak aktif",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isOverlayActive) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isOverlayActive,
                                onCheckedChange = { enable ->
                                    if (enable && !isOverlayPermissionGranted) {
                                        AppIntegrationHelper.openOverlaySettings(context)
                                    } else {
                                        viewModel.toggleOverlayService(enable)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("switch_overlay_toggle")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Widget terapung membolehkan anda menyemak tabiat harian dan mengakses pautan trending tanpa perlu keluar dari aplikasi yang sedang dibuka.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!isOverlayPermissionGranted) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { AppIntegrationHelper.openOverlaySettings(context) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Beri Kebenaran Paparan di Atas Apps (Overlay)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. Bootloader & Background System Auto-start Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Auto-Start Semasa Boot",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Jalankan sistem pengesan tabiat & overlay secara automatik selepas peranti di-boot semula",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = autostartBoot,
                            onCheckedChange = { enabled ->
                                autostartBoot = enabled
                                prefs.edit().putBoolean(BootCompletedReceiver.KEY_AUTOSTART_OVERLAY, enabled).apply()
                            }
                        )
                    }
                }
            }

            // 3. Screen Time & App Activity Overview Card
            item {
                activitySummary?.let { summary ->
                    val totalMinutes = summary.totalScreenTimeMinutes
                    val totalHours = totalMinutes / 60
                    val remMin = totalMinutes % 60

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pengesan Aktiviti Penggunaan",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isUsagePermissionGranted) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isUsagePermissionGranted) "LIVE DATA AKTIF" else "MOD DEMO (BELUM DIBENARKAN)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUsagePermissionGranted) Color(0xFF059669) else Color(0xFFDC2626),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text("Jumlah Masa Skrin Hari Ini", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${totalHours}j ${remMin}m",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = "Status: Terkawal ✨",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF10B981)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Breakdown bars
                            ActivityBreakdownRow(
                                label = "Media Sosial (TikTok, Instagram, dsb.)",
                                minutes = summary.socialTimeMinutes,
                                totalMinutes = if (totalMinutes > 0) totalMinutes else 1,
                                color = Color(0xFF0284C7)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ActivityBreakdownRow(
                                label = "AI Tools & Pembelajaran (ChatGPT, Gemini)",
                                minutes = summary.aiTimeMinutes,
                                totalMinutes = if (totalMinutes > 0) totalMinutes else 1,
                                color = Color(0xFF10A37F)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ActivityBreakdownRow(
                                label = "Utility & Produktiviti (Notion, Drive)",
                                minutes = summary.utilityTimeMinutes,
                                totalMinutes = if (totalMinutes > 0) totalMinutes else 1,
                                color = Color(0xFF7C3AED)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ActivityBreakdownRow(
                                label = "Hiburan & Karaoke (YouTube, Smule)",
                                minutes = summary.entertainmentTimeMinutes,
                                totalMinutes = if (totalMinutes > 0) totalMinutes else 1,
                                color = Color(0xFFE91E63)
                            )

                            if (!isUsagePermissionGranted) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { AppIntegrationHelper.openUsageAccessSettings(context) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Buka Tetapan Akses Penggunaan (Usage Access)", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Detected Apps Usage List
            item {
                Text(
                    text = "Aktiviti Aplikasi Dikesan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            activitySummary?.topApps?.let { apps ->
                if (apps.isEmpty()) {
                    item {
                        Text(
                            text = "Tiada aplikasi aktif dikesan hari ini.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(apps, key = { it.packageName }) { app ->
                        DetectedAppItemCard(app = app)
                    }
                }
            }

            // 5. Interoperability & External Apps Integration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Integrasi Pautan & Perkongsian",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aplikasi ini sedia menerima teks atau pautan terus dari mana-mana pelayar, YouTube, atau Smule melalui menu 'Share'.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                AppIntegrationHelper.shareContent(
                                    context = context,
                                    title = "Kemajuan Tabiat & Aktiviti",
                                    message = "Saya sedang mengesan tabiat dan masa skrin saya menggunakan Habit Coach! 🎯",
                                    url = ""
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Kongsi Status ke WhatsApp / Telegram")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityBreakdownRow(
    label: String,
    minutes: Long,
    totalMinutes: Long,
    color: Color
) {
    val fraction = (minutes.toFloat() / totalMinutes.toFloat()).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text("${minutes}m", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun DetectedAppItemCard(app: DetectedAppUsage) {
    val categoryColor = when (app.category) {
        "Social" -> Color(0xFF0284C7)
        "AI Tools" -> Color(0xFF10A37F)
        "Karaoke & Media" -> Color(0xFFE91E63)
        "Utility" -> Color(0xFF7C3AED)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (app.category) {
                        "Social" -> Icons.Default.Smartphone
                        "AI Tools" -> Icons.Default.AutoAwesome
                        "Karaoke & Media" -> Icons.Default.Mic
                        else -> Icons.Default.Widgets
                    }
                    Icon(icon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = app.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = categoryColor,
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = app.durationFormatted,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Masa Aktif",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
