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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.ai.AiAssessmentDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.EmergencyDisclaimerCard
import com.kindai.app.ui.components.KindTopAppBar
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderlyOwnAiScreen(
    apiService: ApiService,
    onBack: () -> Unit,
    onNavigateToSos: () -> Unit = {}
) {
    var uiState by remember { mutableStateOf<UiState<AiAssessmentDto>>(UiState.Idle) }
    val scope = rememberCoroutineScope()

    fun runAssessment() {
        scope.launch {
            uiState = UiState.Loading
            uiState = safeApiCall { apiService.runMyAiAssessment() }
        }
    }

    LaunchedEffect(Unit) { runAssessment() }

    Scaffold(
        topBar = { KindTopAppBar(title = "Sog'liq holati", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KindColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KindSpacing.screenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is UiState.Loading -> {
                    // Analyzing state
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(KindShapes.full)
                                .background(KindGradients.AiGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome, null,
                                tint = Color.White, modifier = Modifier.size(44.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Tahlil qilinmoqda...",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = KindColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Bir oz kuting",
                            fontSize = 16.sp,
                            color = KindColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(0.5f).clip(KindShapes.full),
                            color = KindColors.AiPurple,
                            trackColor = KindColors.AiPurpleLight
                        )
                    }
                }

                is UiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Warning, null, tint = KindColors.Error, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Xatolik yuz berdi", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = KindColors.Error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(state.message, fontSize = 15.sp, color = KindColors.TextSecondary, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { runAssessment() },
                            shape = KindShapes.buttonPill,
                            colors = ButtonDefaults.buttonColors(containerColor = KindColors.Primary)
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Qayta urinish", fontSize = 16.sp)
                        }
                    }
                }

                is UiState.Success -> {
                    val data = state.data
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
                            "Farzandingizni xabardor qiling",
                            KindColors.RiskHigh,
                            KindColors.RiskHighBg,
                            Icons.Default.SentimentDissatisfied
                        )
                        "EMERGENCY" -> listOf(
                            "Zudlik bilan yordam chaqiring!",
                            KindColors.RiskEmergency,
                            KindColors.RiskEmergencyBg,
                            Icons.Default.LocalHospital
                        )
                        else -> listOf("Tahlil bajarildi", KindColors.TextSecondary, KindColors.SurfaceVariant, Icons.Default.Info)
                    }

                    // Big status card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KindShapes.cardLarge,
                        colors = CardDefaults.cardColors(containerColor = statusBg as Color),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(KindSpacing.cardPaddingXL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = bigIcon as androidx.compose.ui.graphics.vector.ImageVector,
                                contentDescription = null,
                                tint = statusColor as Color,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = statusText as String,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = data.elderlySummary ?: data.summary,
                                fontSize = 17.sp,
                                color = KindColors.TextSecondary,
                                lineHeight = 26.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // For HIGH/EMERGENCY show SOS button
                    if (data.riskLevel.uppercase() in listOf("HIGH", "EMERGENCY")) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToSos,
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = KindShapes.buttonPill,
                            colors = ButtonDefaults.buttonColors(containerColor = KindColors.Error)
                        ) {
                            Icon(Icons.Default.LocalHospital, null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("SOS — Yordam chaqirish", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { runAssessment() },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = KindShapes.buttonPill,
                        colors = ButtonDefaults.buttonColors(containerColor = KindColors.AiPurple)
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Qayta tahlil", fontSize = 16.sp)
                    }
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))
            EmergencyDisclaimerCard()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
