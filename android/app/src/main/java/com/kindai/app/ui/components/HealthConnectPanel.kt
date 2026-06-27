package com.kindai.app.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.PermissionController
import com.kindai.app.core.DaySteps
import com.kindai.app.core.HcHealthData
import com.kindai.app.core.HealthConnectManager
import com.kindai.app.core.readHcHealthData
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import kotlinx.coroutines.launch

@Composable
fun HealthConnectPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hcData by remember { mutableStateOf(HcHealthData(isLoading = true)) }

    val permLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { _ ->
        scope.launch {
            hcData = hcData.copy(isLoading = true)
            hcData = readHcHealthData(context)
        }
    }

    LaunchedEffect(Unit) {
        hcData = readHcHealthData(context)
    }

    Column(modifier = modifier) {
        when {
            // ── SDK mavjud emas ───────────────────────────────────────────────
            !hcData.sdkAvailable && !hcData.isLoading -> {
                HcUnavailableCard()
            }

            // ── Yuklanmoqda ───────────────────────────────────────────────────
            hcData.isLoading -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.card,
                    colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = KindColors.Primary,
                            strokeWidth = 2.dp
                        )
                        Text("Sog'liq ma'lumotlari yuklanmoqda...", fontSize = 14.sp, color = KindColors.TextSecondary)
                    }
                }
            }

            // ── Ruxsat yo'q ───────────────────────────────────────────────────
            !hcData.anyPermissionGranted -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.card,
                    colors = CardDefaults.cardColors(containerColor = KindColors.PrimaryLight),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(KindShapes.medium)
                                    .background(KindColors.Primary.copy(0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MonitorHeart, null, tint = KindColors.Primary, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Telefon sog'lik ma'lumotlari", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
                                Text("Ruxsat berilmagan", fontSize = 13.sp, color = KindColors.TextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Qadamlar, uyqu, yurak urishi, kislorod — bularni telefondan o'qish uchun bir marta ruxsat bering.",
                            fontSize = 13.sp, color = KindColors.TextSecondary, lineHeight = 19.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { permLauncher.launch(HealthConnectManager.PERMISSIONS) },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = KindShapes.buttonPill,
                            colors = ButtonDefaults.buttonColors(containerColor = KindColors.Primary)
                        ) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ruxsat berish", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // ── Ma'lumotlar bor ───────────────────────────────────────────────
            else -> {
                HcDataContent(
                    hcData = hcData,
                    onRefresh = {
                        scope.launch {
                            hcData = hcData.copy(isLoading = true)
                            hcData = readHcHealthData(context)
                        }
                    },
                    onRequestPermissions = { permLauncher.launch(HealthConnectManager.PERMISSIONS) }
                )
            }
        }
    }
}

@Composable
private fun HcDataContent(
    hcData: HcHealthData,
    onRefresh: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    // Permission status pill
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.clip(KindShapes.chip)
                .background(KindColors.RiskLow.copy(0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(7.dp).clip(KindShapes.full).background(KindColors.RiskLow))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    "Ulangan (${hcData.grantedCount}/${hcData.totalPermissions})",
                    fontSize = 12.sp, color = KindColors.RiskLow, fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(
            onClick = onRefresh,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp), tint = KindColors.TextTertiary)
            Spacer(modifier = Modifier.width(3.dp))
            Text("Yangilash", fontSize = 12.sp, color = KindColors.TextTertiary)
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Today metrics grid
    val metrics = buildList {
        if (hcData.todaySteps != null)
            add(Triple(Icons.Default.DirectionsWalk, "Qadamlar", "${hcData.todaySteps}"))
        if (hcData.latestHeartRate != null)
            add(Triple(Icons.Default.MonitorHeart, "Yurak", "${hcData.latestHeartRate} bpm"))
        if (hcData.latestSpo2 != null)
            add(Triple(Icons.Default.Air, "SpO2", "${hcData.latestSpo2}%"))
        if (hcData.lastNightSleepHours != null)
            add(Triple(Icons.Default.Bedtime, "Uyqu", "%.1f soat".format(hcData.lastNightSleepHours)))
        if (hcData.todayCalories != null)
            add(Triple(Icons.Default.LocalFireDepartment, "Kaloriya", "${hcData.todayCalories}"))
    }

    if (metrics.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = KindShapes.card,
            colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.HourglassEmpty, null, tint = KindColors.TextTertiary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Bugungi ma'lumot hali yozilmagan", fontSize = 14.sp, color = KindColors.TextSecondary)
                Text(
                    "Samsung Health yoki Google Fit foydalanilgan bo'lsa ko'rinadi.",
                    fontSize = 12.sp, color = KindColors.TextTertiary, lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = KindShapes.card,
            colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Bugungi ko'rsatkichlar", fontSize = 12.sp, color = KindColors.TextTertiary)
                Spacer(modifier = Modifier.height(10.dp))
                metrics.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { (icon, label, value) ->
                            HcMetricTile(modifier = Modifier.weight(1f), icon = icon, label = label, value = value)
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    // Weekly steps chart
    if (hcData.weeklySteps.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        WeeklyStepsChart(steps = hcData.weeklySteps, avgSteps = hcData.avgWeeklySteps)
    }

    // Weekly averages
    val weeklyMetrics = buildList {
        if (hcData.avgWeeklySteps != null)
            add(Triple(Icons.Default.DirectionsWalk, "Haftalik o'rtacha qadam", "${hcData.avgWeeklySteps}"))
        if (hcData.weeklyAvgHr != null)
            add(Triple(Icons.Default.MonitorHeart, "O'rtacha yurak urishi", "${hcData.weeklyAvgHr} bpm"))
        if (hcData.weeklyAvgSleep != null)
            add(Triple(Icons.Default.Bedtime, "O'rtacha uyqu", "%.1f soat".format(hcData.weeklyAvgSleep)))
    }
    if (weeklyMetrics.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = KindShapes.card,
            colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Haftalik o'rtachalar (7 kun)", fontSize = 12.sp, color = KindColors.TextTertiary)
                Spacer(modifier = Modifier.height(10.dp))
                weeklyMetrics.forEach { (icon, label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null, tint = KindColors.Primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(label, fontSize = 13.sp, color = KindColors.TextSecondary, modifier = Modifier.weight(1f))
                        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun HcMetricTile(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
) {
    Card(
        modifier = modifier,
        shape = KindShapes.medium,
        colors = CardDefaults.cardColors(containerColor = KindColors.Background),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = KindColors.Primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
            Text(label, fontSize = 11.sp, color = KindColors.TextTertiary)
        }
    }
}

@Composable
private fun WeeklyStepsChart(steps: List<DaySteps>, avgSteps: Long?) {
    if (steps.isEmpty()) return
    val maxSteps = steps.maxOf { it.steps }.takeIf { it > 0 } ?: 1L

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Haftalik qadamlar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = KindColors.TextPrimary, modifier = Modifier.weight(1f)
                )
                if (avgSteps != null) {
                    Text("O'rtacha: $avgSteps", fontSize = 12.sp, color = KindColors.TextSecondary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                steps.forEach { day ->
                    val fraction = (day.steps.toFloat() / maxSteps).coerceIn(0.05f, 1f)
                    val dayLabel = day.date.takeLast(5) // MM-DD
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 2.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fraction)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(
                                        if (day.steps >= (avgSteps ?: 0L)) KindColors.Primary
                                        else KindColors.Primary.copy(0.4f)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(dayLabel.substring(3), fontSize = 9.sp, color = KindColors.TextTertiary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(KindShapes.full).background(KindColors.Primary))
                Spacer(modifier = Modifier.width(5.dp))
                Text("O'rtachadan yuqori", fontSize = 11.sp, color = KindColors.TextTertiary)
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.size(8.dp).clip(KindShapes.full).background(KindColors.Primary.copy(0.4f)))
                Spacer(modifier = Modifier.width(5.dp))
                Text("O'rtachadan past", fontSize = 11.sp, color = KindColors.TextTertiary)
            }
        }
    }
}

@Composable
private fun HcUnavailableCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = KindColors.WarningBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, tint = KindColors.Warning, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Health Connect mavjud emas", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
                Text("Play Market orqali o'rnating", fontSize = 12.sp, color = KindColors.TextSecondary)
            }
        }
    }
}
