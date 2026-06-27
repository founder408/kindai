package com.kindai.app.ui.screens.elderly_own

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.local.TokenManager
import com.kindai.app.data.model.elderly.ElderlyDashboardDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.EmergencyDisclaimerCard
import com.kindai.app.ui.components.KindLoadingState
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@Composable
fun ElderlyOwnHomeScreen(
    apiService: ApiService,
    tokenManager: TokenManager,
    onNavigateToHealth: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToTrends: () -> Unit = {},
    onNavigateToDoctorSummary: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {},
    onNavigateToSos: () -> Unit,
    onLogout: () -> Unit
) {
    var dashboard by remember { mutableStateOf<ElderlyDashboardDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = safeApiCall { apiService.getMyDashboard() }
        if (result is UiState.Success) dashboard = result.data
        isLoading = false
    }

    if (isLoading) {
        KindLoadingState()
        return
    }

    val profile = dashboard?.profile
    val riskLevel = dashboard?.latestRiskLevel
    val (riskColor, riskBg, riskLabel) = when (riskLevel?.uppercase()) {
        "LOW" -> Triple(KindColors.RiskLow, KindColors.RiskLowBg, "Holat yaxshi")
        "MEDIUM", "MODERATE" -> Triple(KindColors.RiskMedium, KindColors.RiskMediumBg, "E'tibor kerak")
        "HIGH" -> Triple(KindColors.RiskHigh, KindColors.RiskHighBg, "Murojaat qiling")
        "EMERGENCY" -> Triple(KindColors.RiskEmergency, KindColors.RiskEmergencyBg, "Favqulodda!")
        else -> Triple(KindColors.Secondary, KindColors.SecondaryLight, "Tahlil yo'q")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KindColors.ElderlyBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KindGradients.HeaderGradientLarge)
                .padding(horizontal = KindSpacing.screenHorizontal)
                .padding(top = 52.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Assalomu alaykum,",
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile?.fullName ?: "Hurmatli foydalanuvchi",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // Risk status pill
                    Box(
                        modifier = Modifier
                            .clip(KindShapes.buttonPill)
                            .background(riskBg.copy(alpha = 0.9f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = riskLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = riskColor
                        )
                    }
                }
                IconButton(
                    onClick = {
                        scope.launch {
                            tokenManager.clearToken()
                            onLogout()
                        }
                    }
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = "Chiqish",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Quick stats row
        if (dashboard != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KindSpacing.screenHorizontal)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Medication,
                    value = "${dashboard!!.takenToday}/${dashboard!!.totalMeds}",
                    label = "Dori",
                    color = KindColors.Primary
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Notifications,
                    value = "${dashboard!!.unreadAlerts}",
                    label = "Xabar",
                    color = if (dashboard!!.unreadAlerts > 0) KindColors.Warning else KindColors.Secondary
                )
                StatChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Psychology,
                    value = riskLevel ?: "—",
                    label = "Xavf",
                    color = riskColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main action buttons
        Column(
            modifier = Modifier.padding(horizontal = KindSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ElderlyBigActionButton(
                icon = Icons.Default.Medication,
                title = "Dorilarim",
                subtitle = "Bugungi dorilar ro'yxati",
                accentColor = KindColors.Primary,
                onClick = onNavigateToMedications
            )
            ElderlyBigActionButton(
                icon = Icons.Default.Favorite,
                title = "Sog'lig'im",
                subtitle = "Ko'rsatkichlarni kiriting",
                accentColor = KindColors.Secondary,
                onClick = onNavigateToHealth
            )
            ElderlyBigActionButton(
                icon = Icons.Default.Psychology,
                title = "AI tahlil",
                subtitle = "Sun'iy intellekt xulosasi",
                accentColor = KindColors.AiPurple,
                onClick = onNavigateToAi
            )
            ElderlyBigActionButton(
                icon = Icons.Default.TrendingUp,
                title = "Trendlar",
                subtitle = "Kunlik/haftalik grafik",
                accentColor = KindColors.Info,
                onClick = onNavigateToTrends
            )
            ElderlyBigActionButton(
                icon = Icons.Default.Notifications,
                title = "Bildirishnomalar",
                subtitle = "Muhim xabarlar",
                accentColor = KindColors.Warning,
                onClick = onNavigateToAlerts
            )
            ElderlyBigActionButton(
                icon = Icons.Default.Description,
                title = "Shifokor uchun xulosa",
                subtitle = "Sog'liq hisobotini ko'rish",
                accentColor = KindColors.TextSecondary,
                onClick = onNavigateToDoctorSummary
            )
            ElderlyBigActionButton(
                icon = Icons.Default.Watch,
                title = "Qurilmalarim",
                subtitle = "Smart soat holati",
                accentColor = KindColors.Accent,
                onClick = onNavigateToDevices
            )

            Spacer(modifier = Modifier.height(4.dp))

            // SOS Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = KindShapes.xl,
                        ambientColor = KindColors.Error.copy(alpha = 0.35f),
                        spotColor = KindColors.Error.copy(alpha = 0.35f)
                    )
                    .clip(KindShapes.xl)
                    .background(KindGradients.DangerGradient)
                    .clickable { onNavigateToSos() },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "SOS — Tez yordam",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Favqulodda holatda bosing",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        Column(modifier = Modifier.padding(horizontal = KindSpacing.screenHorizontal)) {
            EmergencyDisclaimerCard()
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = KindColors.TextPrimary
            )
            Text(text = label, fontSize = 11.sp, color = KindColors.TextSecondary)
        }
    }
}

@Composable
fun ElderlyBigActionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(KindSpacing.elderlyButtonHeight)
            .shadow(
                elevation = 3.dp,
                shape = KindShapes.xl,
                ambientColor = accentColor.copy(alpha = 0.08f),
                spotColor = accentColor.copy(alpha = 0.08f)
            )
            .clickable { onClick() },
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(KindShapes.large)
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                Text(text = subtitle, fontSize = 13.sp, color = KindColors.TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, null, tint = accentColor.copy(0.6f), modifier = Modifier.size(22.dp))
        }
    }
}
