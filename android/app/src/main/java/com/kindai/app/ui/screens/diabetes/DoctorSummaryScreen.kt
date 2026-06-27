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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.diabetes.DiabetesDoctorSummaryDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorSummaryScreen(
    apiService: ApiService,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var summary by remember { mutableStateOf<DiabetesDoctorSummaryDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf("7d") }

    fun load(period: String) {
        scope.launch {
            isLoading = true
            val result = safeApiCall { apiService.getDiabetesDoctorSummary(period) }
            if (result is UiState.Success) summary = result.data
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load(selectedPeriod) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shifokor xulosasi", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    TextButton(onClick = { /* PDF export - coming soon */ }) {
                        Icon(Icons.Default.PictureAsPdf, null, tint = KindColors.TextTertiary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PDF", color = KindColors.TextTertiary, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KindColors.Surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KindColors.Background)
        ) {
            // Period filter
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("7d" to "7 kun", "30d" to "30 kun", "90d" to "90 kun").forEach { (code, label) ->
                    FilterChip(
                        selected = selectedPeriod == code,
                        onClick = { selectedPeriod = code; load(code) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KindColors.Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(40.dp))
                }

                summary == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HealthAndSafety, null, tint = KindColors.TextTertiary, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("Xulosa ma'lumotlari yo'q", fontSize = 15.sp, color = KindColors.TextSecondary)
                    }
                }

                else -> Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(horizontal = KindSpacing.screenHorizontal)
                ) {
                    summary?.let { s ->
                        Spacer(Modifier.height(4.dp))

                        // Patient info card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = KindShapes.xl,
                            colors = CardDefaults.cardColors(containerColor = KindColors.PrimaryLight),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(44.dp).clip(KindShapes.full).background(KindColors.Primary.copy(0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) { Icon(Icons.Default.Person, null, tint = KindColors.Primary, modifier = Modifier.size(22.dp)) }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(s.profile.fullName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                                        Text("${s.profile.age} yosh · ${diabetesTypeLabel(s.profile.diabetesType ?: "UNKNOWN")}", fontSize = 13.sp, color = KindColors.TextSecondary)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Hisobot davri: ${s.period}", fontSize = 12.sp, color = KindColors.TextTertiary)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Glucose summary
                        SummarySectionHeader("Qand ko'rsatkichlari")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = KindShapes.xl,
                            colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                val gs = s.glucoseSummary
                                SummaryRow("Jami o'lchov", "${gs.totalRecords} ta")
                                gs.averageGlucose?.let { SummaryRow("O'rtacha qand", "%.1f mmol/L".format(it)) }
                                gs.highestGlucose?.let { SummaryRow("Eng yuqori", "%.1f mmol/L".format(it)) }
                                gs.lowestGlucose?.let { SummaryRow("Eng past", "%.1f mmol/L".format(it)) }
                                gs.fastingAverage?.let { SummaryRow("Och qoringa o'rtacha", "%.1f mmol/L".format(it)) }
                                gs.afterMealAverage?.let { SummaryRow("Ovqatdan keyin o'rtacha", "%.1f mmol/L".format(it)) }
                                gs.inRangePercent?.let { SummaryRow("Normal diapazon ulushi", "%.0f%%".format(it)) }
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        // Medication adherence
                        s.medicationAdherence.let { adh ->
                            SummarySectionHeader("Dori qabul holati")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = KindShapes.xl,
                                colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    SummaryRow("Mos kelish foizi", "${adh.adherencePercent.toInt()}%")
                                    SummaryRow("Qabul qilingan dozalar", "${adh.totalTaken} ta")
                                    SummaryRow("O'tkazib yuborilgan", "${adh.totalMissed} ta")
                                    if (adh.streakDays > 0) SummaryRow("Ketma-ket kunlar", "${adh.streakDays} kun")
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        // Recent symptoms
                        if (!s.symptomsSummary.isNullOrBlank()) {
                            SummarySectionHeader("So'nggi alomatlar")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = KindShapes.xl,
                                colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    s.symptomsSummary.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(5).forEach { symptom ->
                                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                            Text("•", color = KindColors.Warning, modifier = Modifier.width(14.dp))
                                            Text(symptom, fontSize = 13.sp, color = KindColors.TextSecondary)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        // AI summary
                        s.latestAiSummary?.let { aiSum ->
                            SummarySectionHeader("So'nggi AI xulosasi")
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = KindShapes.xl,
                                colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, null, tint = KindColors.AiPurple, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("AI xulosasi", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KindColors.AiPurple)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(aiSum, fontSize = 13.sp, color = KindColors.TextSecondary, lineHeight = 19.sp)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        // Disclaimer
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = KindShapes.xl,
                            colors = CardDefaults.cardColors(containerColor = KindColors.WarningBg),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Info, null, tint = KindColors.Warning, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Bu xulosa tibbiy tashxis emas. Zarur holatda shifokorga murojaat qiling. Kind AI dori yoki insulin dozasini belgilamaydi.",
                                    fontSize = 11.sp, color = KindColors.TextSecondary, lineHeight = 16.sp
                                )
                            }
                        }

                        // PDF coming soon
                        Spacer(Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = KindShapes.xl,
                            colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Default.PictureAsPdf, null, tint = KindColors.TextTertiary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("PDF eksport tez orada...", fontSize = 13.sp, color = KindColors.TextTertiary)
                            }
                        }

                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySectionHeader(title: String) {
    Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary, modifier = Modifier.padding(vertical = 6.dp))
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = KindColors.TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
    }
    HorizontalDivider(thickness = 0.5.dp, color = KindColors.Divider)
}

private fun diabetesTypeLabel(type: String) = when (type) {
    "TYPE_1" -> "1-tur diabet"
    "TYPE_2" -> "2-tur diabet"
    "PREDIABETES" -> "Prediabet"
    "GESTATIONAL" -> "Gestatsion"
    else -> "Noma'lum"
}
