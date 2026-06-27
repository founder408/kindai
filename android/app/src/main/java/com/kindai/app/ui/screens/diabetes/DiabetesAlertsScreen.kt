package com.kindai.app.ui.screens.diabetes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.kindai.app.data.model.diabetes.DiabetesAlertDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiabetesAlertsScreen(
    apiService: ApiService,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var alerts by remember { mutableStateOf<List<DiabetesAlertDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun load() {
        scope.launch {
            isLoading = true
            val result = safeApiCall { apiService.getDiabetesAlerts() }
            if (result is UiState.Success) alerts = result.data
            isLoading = false
        }
    }

    fun markRead(id: String) {
        scope.launch {
            safeApiCall { apiService.markDiabetesAlertRead(id) }
            alerts = alerts.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Ogohlantirishlar", fontWeight = FontWeight.Bold)
                        val unread = alerts.count { !it.isRead }
                        if (unread > 0) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.size(20.dp).clip(KindShapes.full).background(KindColors.Error),
                                contentAlignment = Alignment.Center
                            ) { Text("$unread", fontSize = 10.sp, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold) }
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KindColors.Surface)
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(40.dp))
            }

            alerts.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, tint = KindColors.TextTertiary, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Ogohlantirishlar yo'q", fontSize = 16.sp, color = KindColors.TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    Text("Barcha ko'rsatkichlar normal", fontSize = 13.sp, color = KindColors.TextTertiary)
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(KindColors.Background),
                contentPadding = PaddingValues(horizontal = KindSpacing.screenHorizontal, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    AlertCard(alert, onMarkRead = { markRead(alert.id) })
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertCard(alert: DiabetesAlertDto, onMarkRead: () -> Unit) {
    val (bg, iconColor, icon) = alertStyle(alert.type, alert.severity)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = if (alert.isRead) KindColors.Surface else bg),
        elevation = CardDefaults.cardElevation(if (alert.isRead) 0.dp else 2.dp),
        onClick = { if (!alert.isRead) onMarkRead() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(KindShapes.large).background(iconColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(alert.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary, modifier = Modifier.weight(1f))
                    if (!alert.isRead) {
                        Box(
                            modifier = Modifier.size(8.dp).clip(KindShapes.full).background(iconColor)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(alert.message, fontSize = 13.sp, color = KindColors.TextSecondary, lineHeight = 18.sp)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SeverityChip(alert.severity, iconColor)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        alert.createdAt.take(16).replace("T", " "),
                        fontSize = 11.sp,
                        color = KindColors.TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun SeverityChip(severity: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier.clip(KindShapes.chip).background(color.copy(0.12f)).padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(
            when (severity) { "EMERGENCY" -> "Favqulodda"; "HIGH" -> "Yuqori"; "MEDIUM" -> "O'rta"; else -> "Past" },
            fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold
        )
    }
}

private data class AlertStyle(val bg: androidx.compose.ui.graphics.Color, val iconColor: androidx.compose.ui.graphics.Color, val icon: ImageVector)

private fun alertStyle(type: String, severity: String): AlertStyle {
    val (color, bg) = when (severity) {
        "EMERGENCY" -> KindColors.RiskEmergency to KindColors.RiskEmergencyBg
        "HIGH" -> KindColors.RiskHigh to KindColors.RiskHighBg
        "MEDIUM" -> KindColors.RiskMedium to KindColors.RiskMediumBg
        else -> KindColors.Primary to KindColors.PrimaryLight
    }
    val icon = when (type) {
        "HIGH_GLUCOSE" -> Icons.Default.ArrowUpward
        "LOW_GLUCOSE" -> Icons.Default.ArrowDownward
        "MISSED_MEDICATION" -> Icons.Default.Medication
        "AI_RISK" -> Icons.Default.AutoAwesome
        "SOS" -> Icons.Default.Emergency
        "SYMPTOMS" -> Icons.Default.Warning
        else -> Icons.Default.Notifications
    }
    return AlertStyle(bg, color, icon)
}
