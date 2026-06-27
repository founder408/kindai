package com.kindai.app.ui.screens.dashboard

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.dashboard.DashboardDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.*
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    elderlyId: String,
    apiService: ApiService,
    onBack: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToSimple: () -> Unit
) {
    var uiState by remember { mutableStateOf<UiState<DashboardDto>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            uiState = UiState.Loading
            uiState = safeApiCall { apiService.getDashboard(elderlyId) }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    when (val state = uiState) {
        is UiState.Loading -> KindLoadingState()
        is UiState.Error -> KindErrorState(state.message, onRetry = { loadData() })
        is UiState.Success -> {
            val data = state.data
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(KindColors.Background)
                    .verticalScroll(rememberScrollState())
            ) {
                // Premium profile header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KindGradients.HeaderGradientLarge)
                        .padding(top = 8.dp, bottom = 28.dp)
                        .padding(horizontal = KindSpacing.screenHorizontal)
                ) {
                    Column {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.offset(x = (-12).dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, "Orqaga", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(KindShapes.full)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = data.elderly.fullName.take(1).uppercase(),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = data.elderly.fullName,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (data.elderly.age != null) {
                                        Text(
                                            text = "${data.elderly.age} yosh",
                                            fontSize = 14.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                    if (!data.elderly.relationshipToElderly.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(KindShapes.buttonPill)
                                                .background(Color.White.copy(alpha = 0.2f))
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = data.elderly.relationshipToElderly,
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = KindSpacing.screenHorizontal)) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // AI Risk card
                    if (data.latestAiAssessment != null) {
                        KindRiskCard(
                            riskLevel = data.latestAiAssessment.riskLevel,
                            summary = data.latestAiAssessment.summary,
                            onClick = onNavigateToAi
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Vitals section
                    if (data.lastHealthRecord != null) {
                        val hr = data.lastHealthRecord
                        KindSectionHeader(title = "Oxirgi ko'rsatkichlar")
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            KindMetricCard(
                                title = "Bosim",
                                value = "${hr.systolicBp ?: "-"}/${hr.diastolicBp ?: "-"}",
                                unit = "mmHg",
                                icon = Icons.Default.Favorite,
                                modifier = Modifier.weight(1f),
                                iconTint = KindColors.Error,
                                backgroundColor = KindColors.ErrorBg
                            )
                            KindMetricCard(
                                title = "Puls",
                                value = "${hr.heartRate ?: "-"}",
                                unit = "bpm",
                                icon = Icons.Default.MonitorHeart,
                                modifier = Modifier.weight(1f),
                                iconTint = KindColors.Primary,
                                backgroundColor = KindColors.PrimaryLight
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            KindMetricCard(
                                title = "SpO2",
                                value = "${hr.spo2 ?: "-"}",
                                unit = "%",
                                icon = Icons.Default.Air,
                                modifier = Modifier.weight(1f),
                                iconTint = KindColors.Accent,
                                backgroundColor = KindColors.AccentLight
                            )
                            KindMetricCard(
                                title = "Harorat",
                                value = "${hr.temperature ?: "-"}",
                                unit = "°C",
                                icon = Icons.Default.Thermostat,
                                modifier = Modifier.weight(1f),
                                iconTint = KindColors.Warning,
                                backgroundColor = KindColors.WarningBg
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Overview cards row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KindOverviewMetricCard(
                            title = "Ogohlantirishlar",
                            value = "${data.unreadAlertsCount}",
                            icon = Icons.Default.Notifications,
                            iconTint = if (data.unreadAlertsCount > 0) KindColors.Error else KindColors.Success,
                            iconBg = if (data.unreadAlertsCount > 0) KindColors.ErrorBg else KindColors.SuccessBg,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToAlerts
                        )
                        KindOverviewMetricCard(
                            title = "Dori qabuli",
                            value = "${data.medicationAdherence.adherencePercent.toInt()}%",
                            icon = Icons.Default.Medication,
                            iconTint = KindColors.Secondary,
                            iconBg = KindColors.SecondaryLight,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToMedications
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Quick actions
                    KindSectionHeader(title = "Tezkor amallar")
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KindQuickActionCard(
                            icon = Icons.Default.Favorite,
                            label = "Sog'lik",
                            onClick = onNavigateToHealth,
                            tint = KindColors.Error,
                            backgroundColor = KindColors.ErrorBg,
                            modifier = Modifier.weight(1f)
                        )
                        KindQuickActionCard(
                            icon = Icons.Default.Psychology,
                            label = "AI tahlil",
                            onClick = onNavigateToAi,
                            tint = KindColors.AiPurple,
                            backgroundColor = KindColors.AiPurpleLight,
                            modifier = Modifier.weight(1f)
                        )
                        KindQuickActionCard(
                            icon = Icons.Default.Medication,
                            label = "Dorilar",
                            onClick = onNavigateToMedications,
                            tint = KindColors.Secondary,
                            backgroundColor = KindColors.SecondaryLight,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KindQuickActionCard(
                            icon = Icons.Default.Notifications,
                            label = "Xabarlar",
                            onClick = onNavigateToAlerts,
                            tint = KindColors.Warning,
                            backgroundColor = KindColors.WarningBg,
                            modifier = Modifier.weight(1f)
                        )
                        KindQuickActionCard(
                            icon = Icons.Default.Elderly,
                            label = "Oddiy rejim",
                            onClick = onNavigateToSimple,
                            tint = KindColors.TextSecondary,
                            backgroundColor = KindColors.SurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    SafetyDisclaimerCard()
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        else -> {}
    }
}
