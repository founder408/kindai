package com.kindai.app.ui.screens.diabetes

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.diabetes.DiabetesAiAssessmentDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiabetesAiScreen(
    apiService: ApiService,
    autoRun: Boolean = false,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var assessment by remember { mutableStateOf<DiabetesAiAssessmentDto?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var hasRun by remember { mutableStateOf(false) }

    fun runAi() {
        scope.launch {
            isLoading = true
            errorMsg = null
            val result = safeApiCall { apiService.runDiabetesAiAssessment() }
            isLoading = false
            hasRun = true
            when (result) {
                is UiState.Success -> assessment = result.data
                is UiState.Error -> errorMsg = result.message
                else -> {}
            }
        }
    }

    LaunchedEffect(autoRun) { if (autoRun && !hasRun) runAi() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Tahlil", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KindColors.Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KindColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Header gradient card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KindShapes.xl)
                    .background(Brush.linearGradient(listOf(KindColors.AiPurple, KindColors.Primary)))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Kind AI Tahlil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "So'nggi qand ko'rsatkichlaringiz, dori qabul holatингiz va alomatlaringizga asoslanib AI shaxsiy tahlil tayyorlaydi.",
                        fontSize = 13.sp, color = Color.White.copy(0.85f), lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = { runAi() },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = KindShapes.buttonPill,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = KindColors.AiPurple, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Tahlil qilinmoqda...", color = KindColors.AiPurple, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Default.PlayArrow, null, tint = KindColors.AiPurple, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (hasRun) "Qayta tahlil" else "Tahlil boshlash", color = KindColors.AiPurple, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            errorMsg?.let {
                Spacer(Modifier.height(14.dp))
                Card(Modifier.fillMaxWidth(), shape = KindShapes.large, colors = CardDefaults.cardColors(containerColor = KindColors.ErrorBg)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, null, tint = KindColors.Error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(it, fontSize = 14.sp, color = KindColors.Error)
                    }
                }
            }

            assessment?.let { ai ->
                Spacer(Modifier.height(16.dp))
                RiskLevelCard(ai.riskLevel, ai.summary)
                Spacer(Modifier.height(12.dp))

                val concerns = ai.keyConcerns.orEmpty()
                val nextSteps = ai.recommendedNextSteps.orEmpty()
                val toMonitor = ai.whatToMonitor.orEmpty()
                if (concerns.isNotEmpty()) {
                    AiSectionCard(
                        title = "Asosiy tashvishlar",
                        icon = Icons.Default.Warning,
                        iconColor = KindColors.Warning,
                        items = concerns
                    )
                    Spacer(Modifier.height(10.dp))
                }
                if (nextSteps.isNotEmpty()) {
                    AiSectionCard(
                        title = "Tavsiya etilgan qadamlar",
                        icon = Icons.Default.Checklist,
                        iconColor = KindColors.Secondary,
                        items = nextSteps
                    )
                    Spacer(Modifier.height(10.dp))
                }
                if (toMonitor.isNotEmpty()) {
                    AiSectionCard(
                        title = "Kuzatish kerak",
                        icon = Icons.Default.Visibility,
                        iconColor = KindColors.Primary,
                        items = toMonitor
                    )
                    Spacer(Modifier.height(10.dp))
                }
                if (!ai.doctorNote.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KindShapes.xl,
                        colors = CardDefaults.cardColors(containerColor = KindColors.PrimaryLight),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocalHospital, null, tint = KindColors.Primary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Shifokorga eslatma", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KindColors.Primary)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(ai.doctorNote, fontSize = 13.sp, color = KindColors.TextSecondary, lineHeight = 19.sp)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
                // Source + time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = KindColors.TextTertiary, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${if (ai.source == "azure_openai") "Azure OpenAI" else "Qoida asosida"} · ${ai.createdAt.take(16).replace("T", " ")}",
                        fontSize = 11.sp, color = KindColors.TextTertiary
                    )
                }
                Spacer(Modifier.height(10.dp))
                // Disclaimer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.xl,
                    colors = CardDefaults.cardColors(containerColor = KindColors.WarningBg),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Shield, null, tint = KindColors.Warning, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(ai.disclaimer, fontSize = 12.sp, color = KindColors.TextSecondary, lineHeight = 17.sp)
                    }
                }
            }

            if (!hasRun && !isLoading) {
                Spacer(Modifier.height(32.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.AutoAwesome, null, tint = KindColors.AiPurple.copy(0.3f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("Yuqoridagi tugmani bosib AI tahlilni ishga tushiring", fontSize = 15.sp, color = KindColors.TextTertiary)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun RiskLevelCard(riskLevel: String, summary: String) {
    val (bg, color, label, icon) = when (riskLevel) {
        "EMERGENCY" -> quadOf(KindColors.RiskEmergencyBg, KindColors.RiskEmergency, "FAVQULODDA", Icons.Default.Emergency)
        "HIGH" -> quadOf(KindColors.RiskHighBg, KindColors.RiskHigh, "YUQORI XAVF", Icons.Default.WarningAmber)
        "MEDIUM" -> quadOf(KindColors.RiskMediumBg, KindColors.RiskMedium, "O'RTA XAVF", Icons.Default.Info)
        else -> quadOf(KindColors.RiskLowBg, KindColors.RiskLow, "PAST XAVF", Icons.Default.CheckCircle)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).clip(KindShapes.large).background(color.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, null, tint = color, modifier = Modifier.size(24.dp)) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Xavf darajasi", fontSize = 12.sp, color = KindColors.TextTertiary)
                    Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(summary, fontSize = 14.sp, color = KindColors.TextSecondary, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun AiSectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color, items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
            }
            Spacer(Modifier.height(8.dp))
            items.forEach { item ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text("•", fontSize = 14.sp, color = iconColor, modifier = Modifier.width(14.dp))
                    Text(item, fontSize = 13.sp, color = KindColors.TextSecondary, lineHeight = 19.sp)
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
private fun <A, B, C, D> quadOf(a: A, b: B, c: C, d: D) = Quad(a, b, c, d)
private operator fun <A, B, C, D> Quad<A, B, C, D>.component1() = a
private operator fun <A, B, C, D> Quad<A, B, C, D>.component2() = b
private operator fun <A, B, C, D> Quad<A, B, C, D>.component3() = c
private operator fun <A, B, C, D> Quad<A, B, C, D>.component4() = d
