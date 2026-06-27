package com.kindai.app.ui.screens.elderly_own

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
import com.kindai.app.data.model.alert.AlertDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindTopAppBar
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderlyOwnAlertsScreen(
    apiService: ApiService,
    onBack: () -> Unit
) {
    var alerts by remember { mutableStateOf<List<AlertDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val result = safeApiCall { apiService.getMyElderlyAlerts() }
        if (result is UiState.Success) alerts = result.data
        isLoading = false
    }

    Scaffold(
        topBar = { KindTopAppBar(title = "Bildirishnomalar", onBack = onBack) }
    ) { padding ->
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(40.dp))
            }

            alerts.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).background(KindColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, tint = KindColors.TextTertiary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Bildirishnomalar yo'q", fontSize = 18.sp, color = KindColors.TextSecondary)
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(KindColors.Background)
                    .padding(horizontal = KindSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(alerts) { alert ->
                    ElderlyAlertCard(alert)
                }
            }
        }
    }
}

@Composable
private fun ElderlyAlertCard(alert: AlertDto) {
    val (bgColor, iconColor, icon) = when (alert.severity) {
        "EMERGENCY" -> Triple(KindColors.RiskEmergencyBg, KindColors.RiskEmergency, Icons.Default.LocalHospital)
        "HIGH" -> Triple(KindColors.RiskHighBg, KindColors.RiskHigh, Icons.Default.PriorityHigh)
        "MEDIUM" -> Triple(KindColors.RiskMediumBg, KindColors.RiskMedium, Icons.Default.Warning)
        else -> Triple(KindColors.RiskLowBg, KindColors.RiskLow, Icons.Default.Info)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(KindShapes.large)
                    .background(iconColor.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alert.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(alert.message, fontSize = 14.sp, color = KindColors.TextSecondary, lineHeight = 20.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    alert.createdAt.take(16).replace("T", " "),
                    fontSize = 12.sp,
                    color = KindColors.TextTertiary
                )
            }
        }
    }
}
