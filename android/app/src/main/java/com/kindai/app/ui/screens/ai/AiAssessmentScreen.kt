package com.kindai.app.ui.screens.ai

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
import com.kindai.app.data.model.ai.AiAssessmentDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.*
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssessmentScreen(
    elderlyId: String,
    mode: String,
    apiService: ApiService,
    onBack: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    var uiState by remember { mutableStateOf<UiState<AiAssessmentDto>>(UiState.Idle) }
    val scope = rememberCoroutineScope()
    val isSimple = mode == "simple"

    fun runAssessment() {
        scope.launch {
            uiState = UiState.Loading
            uiState = safeApiCall { apiService.runAiAssessment(elderlyId) }
        }
    }

    LaunchedEffect(Unit) { runAssessment() }

    Scaffold(
        topBar = {
            KindTopAppBar(
                title = if (isSimple) "Sog'liq holati" else "AI Sog'liq Tahlili",
                onBack = onBack
            )
        }
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

            when (val state = uiState) {
                is UiState.Loading -> {
                    AiAnalyzingState()
                }

                is UiState.Error -> {
                    KindErrorState(
                        message = state.message,
                        onRetry = { runAssessment() }
                    )
                }

                is UiState.Success -> {
                    val data = state.data

                    if (isSimple) {
                        // Elderly simple mode
                        ElderlyAiResult(data = data)
                    } else {
                        // Caregiver detailed mode
                        CaregiverAiResult(
                            data = data,
                            onNavigateToAlerts = onNavigateToAlerts,
                            onRerun = { runAssessment() },
                            onBack = onBack
                        )
                    }
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AiAnalyzingState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(KindShapes.full)
                .background(KindGradients.AiGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "AI tahlil qilmoqda...",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = KindColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sog'liq ko'rsatkichlari va tarix tahlil qilinmoqda",
            fontSize = 14.sp,
            color = KindColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .clip(KindShapes.full),
            color = KindColors.AiPurple,
            trackColor = KindColors.AiPurpleLight
        )
    }
}

@Composable
private fun CaregiverAiResult(
    data: AiAssessmentDto,
    onNavigateToAlerts: () -> Unit,
    onRerun: () -> Unit,
    onBack: () -> Unit
) {
    val (riskColor, riskBg, riskLabel, riskIcon) = when (data.riskLevel.uppercase()) {
        "LOW" -> listOf(KindColors.RiskLow, KindColors.RiskLowBg, "Past xavf", Icons.Default.Shield)
        "MODERATE", "MEDIUM" -> listOf(KindColors.RiskMedium, KindColors.RiskMediumBg, "O'rtacha xavf", Icons.Default.Warning)
        "HIGH" -> listOf(KindColors.RiskHigh, KindColors.RiskHighBg, "Yuqori xavf", Icons.Default.PriorityHigh)
        "EMERGENCY" -> listOf(KindColors.RiskEmergency, KindColors.RiskEmergencyBg, "Favqulodda", Icons.Default.LocalHospital)
        else -> listOf(KindColors.TextSecondary, KindColors.SurfaceVariant, data.riskLevel, Icons.Default.Help)
    }

    // Risk level banner
    Card(
        shape = KindShapes.cardLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KindShapes.cardLarge)
                .background(KindGradients.AiGradient)
                .padding(KindSpacing.cardPaddingXL)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(KindShapes.medium)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "AI Tahlil Natijasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(KindShapes.buttonPill)
                            .background(riskBg as Color)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                riskIcon as androidx.compose.ui.graphics.vector.ImageVector,
                                contentDescription = null,
                                tint = riskColor as Color,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = riskLabel as String,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = riskColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = data.summary,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.92f),
                    lineHeight = 21.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Recommendation card
    if (data.recommendation.isNotBlank()) {
        Card(
            shape = KindShapes.card,
            colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(KindSpacing.cardPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(KindShapes.medium)
                            .background(KindColors.PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = KindColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tavsiyalar",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = KindColors.TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = data.recommendation,
                    fontSize = 14.sp,
                    color = KindColors.TextSecondary,
                    lineHeight = 21.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Caregiver summary if available
    if (!data.caregiverSummary.isNullOrBlank()) {
        Card(
            shape = KindShapes.card,
            colors = CardDefaults.cardColors(containerColor = KindColors.InfoBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(KindSpacing.cardPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = KindColors.Info,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Qarovchi uchun xulosa",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = KindColors.Info
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = data.caregiverSummary!!,
                    fontSize = 14.sp,
                    color = KindColors.TextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    SafetyDisclaimerCard()
    Spacer(modifier = Modifier.height(20.dp))

    // Action buttons
    KindSecondaryButton(
        text = "Ogohlantirishlarni ko'rish",
        icon = Icons.Default.Notifications,
        onClick = onNavigateToAlerts
    )
    Spacer(modifier = Modifier.height(10.dp))
    KindSecondaryButton(
        text = "Qayta tahlil qilish",
        icon = Icons.Default.Refresh,
        onClick = onRerun
    )
    Spacer(modifier = Modifier.height(10.dp))
    KindSecondaryButton(
        text = "Dashboardga qaytish",
        icon = Icons.Default.ArrowBack,
        onClick = onBack
    )
}

@Composable
private fun ElderlyAiResult(data: AiAssessmentDto) {
    val (statusText, statusColor, statusBg, bigIcon) = when (data.riskLevel.uppercase()) {
        "LOW" -> listOf(
            "Holatingiz yaxshi",
            KindColors.RiskLow,
            KindColors.RiskLowBg,
            Icons.Default.SentimentVerySatisfied
        )
        "MODERATE", "MEDIUM" -> listOf(
            "E'tibor kerak",
            KindColors.RiskMedium,
            KindColors.RiskMediumBg,
            Icons.Default.SentimentNeutral
        )
        "HIGH" -> listOf(
            "Farzandingiz bilan bog'laning",
            KindColors.RiskHigh,
            KindColors.RiskHighBg,
            Icons.Default.SentimentDissatisfied
        )
        "EMERGENCY" -> listOf(
            "Zudlik bilan yordam kerak bo'lishi mumkin",
            KindColors.RiskEmergency,
            KindColors.RiskEmergencyBg,
            Icons.Default.LocalHospital
        )
        else -> listOf(
            "Tahlil bajarildi",
            KindColors.TextSecondary,
            KindColors.SurfaceVariant,
            Icons.Default.Info
        )
    }

    // Big status card for elderly
    Card(
        shape = KindShapes.cardLarge,
        colors = CardDefaults.cardColors(containerColor = statusBg as Color),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KindSpacing.cardPaddingXL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = bigIcon as androidx.compose.ui.graphics.vector.ImageVector,
                contentDescription = null,
                tint = statusColor as Color,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = statusText as String,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (!data.elderlySummary.isNullOrBlank()) {
                Text(
                    text = data.elderlySummary!!,
                    fontSize = 16.sp,
                    color = KindColors.TextSecondary,
                    lineHeight = 24.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(20.dp))
    EmergencyDisclaimerCard()
}

