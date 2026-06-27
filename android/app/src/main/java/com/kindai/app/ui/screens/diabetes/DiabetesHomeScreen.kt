package com.kindai.app.ui.screens.diabetes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.local.TokenManager
import com.kindai.app.data.model.diabetes.DiabetesDashboardDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiabetesHomeScreen(
    apiService: ApiService,
    tokenManager: TokenManager,
    onNavigateToAddGlucose: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMeds: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToTrends: () -> Unit,
    onNavigateToDoctorSummary: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToSos: () -> Unit,
    onNavigateToFamilySharing: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSetup: () -> Unit = onNavigateToAddGlucose,
) {
    val scope = rememberCoroutineScope()
    var dashboard by remember { mutableStateOf<DiabetesDashboardDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var needsSetup by remember { mutableStateOf(false) }

    fun load() {
        scope.launch {
            isLoading = true
            errorMsg = null
            val result = safeApiCall { apiService.getDiabetesDashboard() }
            when (result) {
                is UiState.Success -> dashboard = result.data
                is UiState.Error -> {
                    if (result.message.contains("topilmadi", ignoreCase = true) || result.message.contains("404"))
                        needsSetup = true
                    else if (result.message == "SESSION_EXPIRED") onLogout()
                    else errorMsg = result.message
                }
                else -> {}
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kind AI", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = KindColors.Primary)
                        Text("Qand nazorati", fontSize = 12.sp, color = KindColors.TextTertiary)
                    }
                },
                actions = {
                    IconButton(onClick = { load() }) {
                        Icon(Icons.Default.Refresh, null, tint = KindColors.TextSecondary)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, null, tint = KindColors.TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KindColors.Surface)
            )
        }
    ) { padding ->
        when {
            needsSetup -> {
                Box(Modifier.fillMaxSize().padding(padding).background(KindColors.Background), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.MonitorHeart, null, tint = KindColors.Primary, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Profilingiz yo'q", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Qand nazoratini boshlash uchun profilingizni yarating.", fontSize = 15.sp, color = KindColors.TextSecondary)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToSetup,
                            shape = KindShapes.buttonPill,
                            colors = ButtonDefaults.buttonColors(containerColor = KindColors.Primary),
                            modifier = Modifier.fillMaxWidth(0.7f).height(52.dp)
                        ) { Text("Profilni yaratish", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }

            isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding).background(KindColors.Background), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Yuklanmoqda...", color = KindColors.TextSecondary)
                    }
                }
            }

            errorMsg != null -> {
                Box(Modifier.fillMaxSize().padding(padding).background(KindColors.Background), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.ErrorOutline, null, tint = KindColors.Error, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(errorMsg!!, color = KindColors.TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { load() }, shape = KindShapes.buttonPill) { Text("Qayta urinish") }
                    }
                }
            }

            else -> {
                val d = dashboard
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(KindColors.Background)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ── Header gradient ───────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KindGradients.PrimaryGradient)
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                    ) {
                        Column {
                            Text(
                                "Salom, ${d?.profile?.fullName?.split(" ")?.firstOrNull() ?: ""}!",
                                fontSize = 22.sp, fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Bugungi holatingiz qanday?", fontSize = 14.sp, color = Color.White.copy(0.8f))

                            // Alert banner
                            if ((d?.unreadAlerts ?: 0) > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    shape = KindShapes.large,
                                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.15f)),
                                    onClick = onNavigateToAlerts
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("${d?.unreadAlerts} ta o'qilmagan xabar", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // AI risk banner
                            val riskLevel = d?.latestAiAssessment?.riskLevel
                            if (riskLevel == "HIGH" || riskLevel == "EMERGENCY") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    shape = KindShapes.large,
                                    colors = CardDefaults.cardColors(containerColor = KindColors.Error.copy(0.2f)),
                                    onClick = onNavigateToAi
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AI: $riskLevel xavf signali — AI tahlilni oching", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                        // ── Today glucose card ────────────────────────────────
                        val latest = d?.latestGlucose
                        if (latest != null) {
                            GlucoseStatusCard(
                                value = latest.glucoseValue,
                                context = latest.measurementContext,
                                time = latest.measuredAt.take(16).replace("T", " "),
                                onClick = onNavigateToHistory
                            )
                        } else {
                            EmptyGlucoseCard(onAddGlucose = onNavigateToAddGlucose)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Medication today summary ──────────────────────────
                        val adherence = d?.medicationAdherence
                        val totalMeds = d?.todayMedications?.size ?: 0
                        MedicationSummaryCard(
                            totalMeds = totalMeds,
                            takenCount = adherence?.totalTaken ?: 0,
                            adherencePct = adherence?.adherencePercent ?: 0.0,
                            onClick = onNavigateToMeds
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Weekly summary ────────────────────────────────────
                        val weekly = d?.weeklySummary
                        if (weekly != null && weekly.totalRecords > 0) {
                            WeeklySummaryCard(
                                avgGlucose = weekly.averageGlucose,
                                highestGlucose = weekly.highestGlucose,
                                lowestGlucose = weekly.lowestGlucose,
                                trend = weekly.trendDirection,
                                totalRecords = weekly.totalRecords,
                                onClick = onNavigateToTrends
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // ── Quick actions ─────────────────────────────────────
                        Text("Tezkor amallar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        QuickActionsGrid(
                            onAddGlucose = onNavigateToAddGlucose,
                            onMeds = onNavigateToMeds,
                            onAi = onNavigateToAi,
                            onDoctorSummary = onNavigateToDoctorSummary,
                            onHistory = onNavigateToHistory,
                            onTrends = onNavigateToTrends,
                            onFamily = onNavigateToFamilySharing,
                            onAlerts = onNavigateToAlerts,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── SOS ───────────────────────────────────────────────
                        Card(
                            onClick = onNavigateToSos,
                            modifier = Modifier.fillMaxWidth(),
                            shape = KindShapes.xl,
                            colors = CardDefaults.cardColors(containerColor = KindColors.ErrorBg),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Emergency, null, tint = KindColors.Error, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("SOS — Yordam kerak", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.Error)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GlucoseStatusCard(value: Double, context: String, time: String, onClick: () -> Unit) {
    val (bg, textColor, label) = glucoseRiskStyle(value)
    val contextUz = when (context) {
        "FASTING" -> "Och qoringa"
        "BEFORE_MEAL" -> "Ovqatdan oldin"
        "AFTER_MEAL" -> "Ovqatdan keyin"
        "BEDTIME" -> "Uyqudan oldin"
        else -> "Tasodifiy"
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.cardLarge,
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("So'nggi qand ko'rsatkichi", fontSize = 12.sp, color = KindColors.TextTertiary)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("$value", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("mmol/L", fontSize = 14.sp, color = KindColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.clip(KindShapes.chip)
                            .background(textColor.copy(0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) { Text(label, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.SemiBold) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(contextUz, fontSize = 12.sp, color = KindColors.TextSecondary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(time, fontSize = 11.sp, color = KindColors.TextTertiary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = KindColors.TextTertiary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun EmptyGlucoseCard(onAddGlucose: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.cardLarge,
        colors = CardDefaults.cardColors(containerColor = KindColors.PrimaryLight),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AddCircle, null, tint = KindColors.Primary, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("Bugun qand o'lchovingiz yo'q", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Glukometringizdan o'lchab, natijani kiriting.", fontSize = 13.sp, color = KindColors.TextSecondary)
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAddGlucose,
                shape = KindShapes.buttonPill,
                colors = ButtonDefaults.buttonColors(containerColor = KindColors.Primary)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Qand kiritish")
            }
        }
    }
}

@Composable
private fun MedicationSummaryCard(totalMeds: Int, takenCount: Int, adherencePct: Double, onClick: () -> Unit) {
    val pctColor = when {
        adherencePct >= 80 -> KindColors.RiskLow
        adherencePct >= 50 -> KindColors.RiskMedium
        else -> KindColors.RiskHigh
    }
    Card(
        onClick = onClick,
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
                modifier = Modifier.size(48.dp).clip(KindShapes.large).background(KindColors.Secondary.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Medication, null, tint = KindColors.Secondary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Dorilar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                if (totalMeds == 0)
                    Text("Tayinlangan dori yo'q", fontSize = 13.sp, color = KindColors.TextTertiary)
                else
                    Text("$takenCount/$totalMeds qabul qilindi • ${adherencePct.toInt()}%", fontSize = 13.sp, color = pctColor)
            }
            Icon(Icons.Default.ChevronRight, null, tint = KindColors.TextTertiary)
        }
    }
}

@Composable
private fun WeeklySummaryCard(
    avgGlucose: Double?,
    highestGlucose: Double?,
    lowestGlucose: Double?,
    trend: String,
    totalRecords: Int,
    onClick: () -> Unit,
) {
    val (trendIcon, trendColor) = when (trend) {
        "IMPROVED" -> Icons.Default.TrendingDown to KindColors.RiskLow
        "WORSE" -> Icons.Default.TrendingUp to KindColors.RiskHigh
        "NEEDS_ATTENTION" -> Icons.Default.TrendingUp to KindColors.RiskMedium
        else -> Icons.Default.TrendingFlat to KindColors.TextSecondary
    }
    val trendUz = when (trend) {
        "IMPROVED" -> "Yaxshilanmoqda"
        "WORSE" -> "Yomonlashmoqda"
        "NEEDS_ATTENTION" -> "E'tibor kerak"
        else -> "Barqaror"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("7 kunlik xulosa", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary, modifier = Modifier.weight(1f))
                Icon(trendIcon, null, tint = trendColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(trendUz, fontSize = 12.sp, color = trendColor, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                WeeklyStatItem("O'rtacha", avgGlucose?.let { "%.1f".format(it) } ?: "—")
                WeeklyStatItem("Eng yuqori", highestGlucose?.let { "%.1f".format(it) } ?: "—")
                WeeklyStatItem("Eng past", lowestGlucose?.let { "%.1f".format(it) } ?: "—")
                WeeklyStatItem("O'lchov", "$totalRecords ta")
            }
        }
    }
}

@Composable
private fun WeeklyStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
        Text(label, fontSize = 11.sp, color = KindColors.TextTertiary)
    }
}

@Composable
private fun QuickActionsGrid(
    onAddGlucose: () -> Unit,
    onMeds: () -> Unit,
    onAi: () -> Unit,
    onDoctorSummary: () -> Unit,
    onHistory: () -> Unit,
    onTrends: () -> Unit,
    onFamily: () -> Unit,
    onAlerts: () -> Unit,
) {
    val items = listOf(
        Triple(Icons.Default.AddCircle, "Qand kiritish", onAddGlucose) to KindColors.Primary,
        Triple(Icons.Default.Medication, "Dorilar", onMeds) to KindColors.Secondary,
        Triple(Icons.Default.AutoAwesome, "AI tahlil", onAi) to KindColors.AiPurple,
        Triple(Icons.Default.MedicalInformation, "Shifokor xulosasi", onDoctorSummary) to KindColors.Accent,
        Triple(Icons.Default.History, "Tarix", onHistory) to KindColors.TextSecondary,
        Triple(Icons.Default.BarChart, "Tendensiyalar", onTrends) to KindColors.Warning,
        Triple(Icons.Default.FamilyRestroom, "Oila ulashish", onFamily) to KindColors.Secondary,
        Triple(Icons.Default.Notifications, "Xabarnomalar", onAlerts) to KindColors.RiskHigh,
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (triple, color) ->
                    val (icon, label, action) = triple
                    QuickActionCard(modifier = Modifier.weight(1f), icon = icon, label = label, color = color, onClick = action)
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickActionCard(modifier: Modifier, icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(KindShapes.large).background(color.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 16.sp)
        }
    }
}

fun glucoseRiskStyle(value: Double): Triple<Color, Color, String> = when {
    value < 3.0 -> Triple(KindColors.RiskEmergencyBg, KindColors.RiskEmergency, "Juda past signal!")
    value < 4.0 -> Triple(KindColors.RiskHighBg, KindColors.RiskHigh, "Past signal")
    value <= 7.8 -> Triple(KindColors.RiskLowBg, KindColors.RiskLow, "Normal diapazonda")
    value <= 11.0 -> Triple(KindColors.RiskMediumBg, KindColors.RiskMedium, "Biroz yuqori signal")
    value <= 16.7 -> Triple(KindColors.RiskHighBg, KindColors.RiskHigh, "Yuqori signal")
    else -> Triple(KindColors.RiskEmergencyBg, KindColors.RiskEmergency, "Juda yuqori signal!")
}
