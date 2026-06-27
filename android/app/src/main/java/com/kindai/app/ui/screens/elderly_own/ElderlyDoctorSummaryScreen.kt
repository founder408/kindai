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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.elderly.DoctorSummaryDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindTopAppBar
import com.kindai.app.ui.components.SafetyDisclaimerCard
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderlyDoctorSummaryScreen(
    apiService: ApiService,
    onBack: () -> Unit
) {
    var summary by remember { mutableStateOf<DoctorSummaryDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val result = safeApiCall { apiService.getDoctorSummary() }
        if (result is UiState.Success) summary = result.data
        isLoading = false
    }

    Scaffold(
        topBar = { KindTopAppBar(title = "Shifokor uchun xulosa", onBack = onBack) }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(40.dp))
            }
        } else if (summary == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Ma'lumot topilmadi", fontSize = 18.sp, color = KindColors.TextSecondary)
            }
        } else {
            val data = summary!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(KindColors.Background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = KindSpacing.screenHorizontal)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Header card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.cardLarge,
                    colors = CardDefaults.cardColors(containerColor = KindColors.PrimaryLight),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(KindSpacing.cardPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(KindShapes.full).background(KindColors.Primary.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = KindColors.Primary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(data.elderlyName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                            if (data.age != null) Text("Yosh: ${data.age}", fontSize = 15.sp, color = KindColors.TextSecondary)
                            Text(
                                "Hisobot: ${data.generatedAt.take(16).replace("T", " ")}",
                                fontSize = 12.sp,
                                color = KindColors.TextTertiary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!data.chronicDiseases.isNullOrBlank()) {
                    SummaryRow(icon = Icons.Default.MedicalServices, label = "Surunkali kasalliklar", value = data.chronicDiseases)
                    Spacer(modifier = Modifier.height(10.dp))
                }

                val riskColor = when (data.latestRiskLevel?.uppercase()) {
                    "LOW" -> KindColors.RiskLow
                    "MEDIUM", "MODERATE" -> KindColors.RiskMedium
                    "HIGH" -> KindColors.RiskHigh
                    "EMERGENCY" -> KindColors.RiskEmergency
                    else -> KindColors.TextTertiary
                }
                SummaryRow(
                    icon = Icons.Default.Psychology,
                    label = "So'nggi AI xavf darajasi",
                    value = data.latestRiskLevel ?: "Tahlil qilinmagan",
                    valueColor = riskColor
                )
                Spacer(modifier = Modifier.height(10.dp))
                SummaryRow(icon = Icons.Default.Analytics, label = "So'nggi 30 kunda tahlillar", value = "${data.recentAssessmentsCount} marta")
                Spacer(modifier = Modifier.height(10.dp))
                SummaryRow(icon = Icons.Default.Medication, label = "Faol dorilar", value = "${data.activeMedications} ta")

                if (data.lastHealthRecord != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("So'nggi ko'rsatkichlar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
                    Spacer(modifier = Modifier.height(10.dp))

                    val hr = data.lastHealthRecord
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KindShapes.card,
                        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(KindSpacing.cardPadding)) {
                            if (hr.systolicBp != null) VitalRow("Qon bosimi", "${hr.systolicBp}/${hr.diastolicBp} mmHg")
                            if (hr.heartRate != null) VitalRow("Yurak urishi", "${hr.heartRate} bpm")
                            if (hr.bloodSugar != null) VitalRow("Qon shakar", "${hr.bloodSugar} mmol/L")
                            if (hr.spo2 != null) VitalRow("SpO2", "${hr.spo2}%")
                            if (hr.temperature != null) VitalRow("Harorat", "${hr.temperature}°C")
                            if (!hr.mood.isNullOrBlank()) VitalRow("Kayfiyat", hr.mood)
                            Text(
                                "Sana: ${hr.createdAt.take(10)}",
                                fontSize = 12.sp,
                                color = KindColors.TextTertiary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                SafetyDisclaimerCard()
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SummaryRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = KindColors.TextPrimary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(KindShapes.medium).background(KindColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = KindColors.Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 13.sp, color = KindColors.TextTertiary)
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
            }
        }
    }
}

@Composable
private fun VitalRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("$label: ", fontSize = 14.sp, color = KindColors.TextSecondary, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, color = KindColors.TextPrimary, fontWeight = FontWeight.SemiBold)
    }
}
