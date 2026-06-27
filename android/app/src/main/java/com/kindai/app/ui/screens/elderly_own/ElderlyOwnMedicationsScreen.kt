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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.medication.MedicationDto
import com.kindai.app.data.model.medication.MedicationTakenRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindTopAppBar
import com.kindai.app.ui.components.SafetyDisclaimerCard
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderlyOwnMedicationsScreen(
    apiService: ApiService,
    onBack: () -> Unit
) {
    var medications by remember { mutableStateOf<List<MedicationDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val takenSet = remember { mutableStateOf(setOf<String>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            isLoading = true
            val result = safeApiCall { apiService.getMyMedications() }
            if (result is UiState.Success) medications = result.data
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = { KindTopAppBar(title = "Dorilarim", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(40.dp))
            }
        } else if (medications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(KindColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Medication, null, tint = KindColors.TextTertiary, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Dorilar tayinlanmagan", fontSize = 18.sp, color = KindColors.TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Farzandingiz dori tayinlasa bu yerda ko'rinadi", fontSize = 14.sp, color = KindColors.TextTertiary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(KindColors.Background)
                    .padding(horizontal = KindSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    Text(
                        "Bugungi dorilar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KindColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(medications) { med ->
                    val isTaken = med.id in takenSet.value
                    MedicationTakenCard(
                        med = med,
                        isTaken = isTaken,
                        onMarkTaken = {
                            scope.launch {
                                val today = LocalDate.now()
                                val timeStr = med.timeToTake.padStart(8, '0') // "HH:mm:ss"
                                val scheduledIso = try {
                                    val lt = LocalTime.parse(timeStr)
                                    today.atTime(lt).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                } catch (e: Exception) {
                                    "${today}T${med.timeToTake}:00+00:00"
                                }

                                val result = safeApiCall {
                                    apiService.markMyMedicationTaken(
                                        med.id,
                                        MedicationTakenRequest(scheduledTime = scheduledIso)
                                    )
                                }
                                if (result is UiState.Success) {
                                    takenSet.value = takenSet.value + med.id
                                    snackbarHostState.showSnackbar("${med.name} — qabul qilindi ✓")
                                }
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    SafetyDisclaimerCard()
                }
            }
        }
    }
}

@Composable
private fun MedicationTakenCard(
    med: MedicationDto,
    isTaken: Boolean,
    onMarkTaken: () -> Unit
) {
    val cardBg = if (isTaken) KindColors.RiskLowBg else KindColors.Surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(if (isTaken) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(KindShapes.large)
                    .background(if (isTaken) KindColors.RiskLow.copy(0.15f) else KindColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isTaken) Icons.Default.CheckCircle else Icons.Default.Medication,
                    null,
                    tint = if (isTaken) KindColors.RiskLow else KindColors.Primary,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = med.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = KindColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (!med.dosage.isNullOrBlank()) {
                    Text(med.dosage, fontSize = 14.sp, color = KindColors.TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = KindColors.TextTertiary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(med.timeToTake, fontSize = 14.sp, color = KindColors.TextSecondary)
                    if (!med.beforeOrAfterMeal.isNullOrBlank()) {
                        Text(" · ${med.beforeOrAfterMeal}", fontSize = 13.sp, color = KindColors.TextTertiary)
                    }
                }
                if (!med.instruction.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(med.instruction, fontSize = 13.sp, color = KindColors.TextTertiary, lineHeight = 18.sp)
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isTaken) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, null, tint = KindColors.RiskLow, modifier = Modifier.size(28.dp))
                    Text("Qabul\nqilindi", fontSize = 11.sp, color = KindColors.RiskLow, lineHeight = 15.sp)
                }
            } else {
                Button(
                    onClick = onMarkTaken,
                    modifier = Modifier.height(44.dp),
                    shape = KindShapes.buttonPill,
                    colors = ButtonDefaults.buttonColors(containerColor = KindColors.Primary)
                ) {
                    Text("Ichdim", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
