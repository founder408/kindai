package com.kindai.app.ui.screens.elderly_own

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.devices.DeviceInfoDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.HealthConnectPanel
import com.kindai.app.ui.components.KindTopAppBar
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderlyDevicesScreen(
    apiService: ApiService,
    onBack: () -> Unit
) {
    var backendDevices by remember { mutableStateOf<List<DeviceInfoDto>>(emptyList()) }
    var isLoadingBackend by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val result = safeApiCall { apiService.getMyDevices() }
        if (result is UiState.Success) backendDevices = result.data.devices
        isLoadingBackend = false
    }

    Scaffold(
        topBar = { KindTopAppBar(title = "Qurilmalarim", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KindColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KindSpacing.screenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ── Telefon (Health Connect) section ─────────────────────────────
            SectionHeader(title = "Telefon sog'lik ma'lumotlari", icon = Icons.Default.PhoneAndroid, color = KindColors.Primary)
            Spacer(modifier = Modifier.height(10.dp))
            HealthConnectPanel(modifier = Modifier.fillMaxWidth())

            // ── Smart watch section ───────────────────────────────────────────
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Smart soat", icon = Icons.Default.Watch, color = KindColors.Accent)
            Spacer(modifier = Modifier.height(10.dp))

            if (isLoadingBackend) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.xl,
                    colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = KindColors.Accent, modifier = Modifier.size(28.dp))
                    }
                }
            } else {
                val watchDevices = backendDevices.filter { it.type == "smartwatch" }
                if (watchDevices.isEmpty()) {
                    NotConnectedCard(name = "Smart soat", desc = "Hali ulangan emas")
                } else {
                    watchDevices.forEach { device ->
                        WatchCard(device)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // ── Coming soon devices ───────────────────────────────────────────
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(title = "Qo'shimcha qurilmalar", icon = Icons.Default.Devices, color = KindColors.TextSecondary)
            Spacer(modifier = Modifier.height(10.dp))

            listOf(
                Triple(Icons.Default.Bluetooth, "Bluetooth tonometr", "Qon bosimini avtomatik o'lchaydi"),
                Triple(Icons.Default.BluetoothConnected, "Glukometr", "Qon shakarni o'lchaydi"),
                Triple(Icons.Default.MonitorWeight, "Aqlli tarozu", "Vazn va BMI nazorati"),
                Triple(Icons.Default.Watch, "Kind Band", "Kattalar uchun maxsus bilakuzuk"),
            ).forEach { (icon, name, desc) ->
                ComingSoonCard(icon = icon, name = name, desc = desc)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(30.dp).clip(KindShapes.medium).background(color.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
    }
}

@Composable
private fun NotConnectedCard(name: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(KindShapes.large).background(KindColors.SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Watch, null, tint = KindColors.TextTertiary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(KindShapes.full).background(KindColors.TextTertiary))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(desc, fontSize = 13.sp, color = KindColors.TextTertiary)
                }
            }
        }
    }
}

@Composable
private fun WatchCard(device: DeviceInfoDto) {
    val isConnected = device.connected
    val connColor = if (isConnected) KindColors.RiskLow else KindColors.TextTertiary
    val connBg = if (isConnected) KindColors.RiskLowBg else KindColors.Surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = connBg),
        elevation = CardDefaults.cardElevation(if (isConnected) 0.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(KindShapes.large).background(connColor.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Watch, null, tint = connColor, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(7.dp).clip(KindShapes.full).background(connColor))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(if (isConnected) "Ulangan" else "Ulanmagan", fontSize = 13.sp, color = connColor)
                }
                if (!device.lastSync.isNullOrBlank()) {
                    Text(
                        "So'nggi: ${device.lastSync.take(16).replace("T", " ")}",
                        fontSize = 11.sp, color = KindColors.TextTertiary
                    )
                }
            }
            if (device.battery != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        if (device.battery > 30) Icons.Default.BatteryFull else Icons.Default.BatteryAlert,
                        null,
                        tint = if (device.battery > 30) KindColors.RiskLow else KindColors.Error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("${device.battery}%", fontSize = 11.sp, color = KindColors.TextTertiary)
                }
            }
        }
    }
}

@Composable
private fun ComingSoonCard(icon: ImageVector, name: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(KindShapes.large).background(KindColors.SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = KindColors.TextTertiary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextSecondary)
                Text(desc, fontSize = 12.sp, color = KindColors.TextTertiary)
            }
            Box(
                modifier = Modifier.clip(KindShapes.chip).background(KindColors.AccentLight)
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text("Tez orada", fontSize = 11.sp, color = KindColors.Accent, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
