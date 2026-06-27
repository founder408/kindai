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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.diabetes.DiabetesAdherenceDto
import com.kindai.app.data.model.diabetes.DiabetesMedTakenRequest
import com.kindai.app.data.model.diabetes.DiabetesMedicationDto
import com.kindai.app.data.remote.ApiService
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
fun DiabetesMedicationScreen(
    apiService: ApiService,
    onBack: () -> Unit,
    onNavigateToAddMed: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var medications by remember { mutableStateOf<List<DiabetesMedicationDto>>(emptyList()) }
    var adherence by remember { mutableStateOf<DiabetesAdherenceDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val takenSet = remember { mutableStateOf(setOf<String>()) }
    val missedSet = remember { mutableStateOf(setOf<String>()) }
    val snackState = remember { SnackbarHostState() }

    fun load() {
        scope.launch {
            isLoading = true
            val medsResult = safeApiCall { apiService.getDiabetesMedications() }
            val adhrResult = safeApiCall { apiService.getDiabetesAdherence(7) }
            if (medsResult is UiState.Success) medications = medsResult.data
            if (adhrResult is UiState.Success) adherence = adhrResult.data
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dorilar va insulin", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(onClick = onNavigateToAddMed) {
                        Icon(Icons.Default.Add, null, tint = KindColors.Primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KindColors.Surface)
            )
        },
        snackbarHost = { SnackbarHost(snackState) }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(40.dp))
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(KindColors.Background),
                contentPadding = PaddingValues(horizontal = KindSpacing.screenHorizontal, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Adherence card
                adherence?.let { adh ->
                    item {
                        AdherenceCard(adh)
                    }
                }

                // Section header
                item {
                    Text("Bugungi jadval", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                }

                if (medications.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = KindShapes.xl,
                            colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Medication, null, tint = KindColors.TextTertiary, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(10.dp))
                                Text("Tayinlangan dori yo'q", fontSize = 16.sp, color = KindColors.TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                Text("Dori qo'shish uchun + tugmasini bosing.", fontSize = 13.sp, color = KindColors.TextTertiary)
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = onNavigateToAddMed,
                                    shape = KindShapes.buttonPill,
                                    colors = ButtonDefaults.buttonColors(containerColor = KindColors.Primary)
                                ) { Text("Dori qo'shish") }
                            }
                        }
                    }
                } else {
                    items(medications) { med ->
                        val isTaken = med.id in takenSet.value
                        val isMissed = med.id in missedSet.value
                        DiabetesMedCard(
                            med = med,
                            isTaken = isTaken,
                            isMissed = isMissed,
                            onTaken = {
                                scope.launch {
                                    val scheduledIso = buildScheduledTime(med.scheduleTime)
                                    val result = safeApiCall {
                                        apiService.markDiabetesMedTaken(
                                            med.id,
                                            DiabetesMedTakenRequest(scheduledTime = scheduledIso, status = "TAKEN")
                                        )
                                    }
                                    if (result is UiState.Success) {
                                        takenSet.value = takenSet.value + med.id
                                        snackState.showSnackbar("${med.name} — qabul qilindi ✓")
                                    }
                                }
                            },
                            onMissed = {
                                scope.launch {
                                    val scheduledIso = buildScheduledTime(med.scheduleTime)
                                    val result = safeApiCall {
                                        apiService.markDiabetesMedTaken(
                                            med.id,
                                            DiabetesMedTakenRequest(scheduledTime = scheduledIso, status = "MISSED")
                                        )
                                    }
                                    if (result is UiState.Success) {
                                        missedSet.value = missedSet.value + med.id
                                        snackState.showSnackbar("${med.name} — o'tkazib yuborildi")
                                    }
                                }
                            }
                        )
                    }
                }

                // Safety disclaimer
                item {
                    Spacer(Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = KindShapes.xl,
                        colors = CardDefaults.cardColors(containerColor = KindColors.WarningBg),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Info, null, tint = KindColors.Warning, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Kind AI dori yoki insulin dozasini belgilamaydi. Faqat shifokor tayinlagan dozani qabul qiling.",
                                fontSize = 12.sp, color = KindColors.TextSecondary, lineHeight = 17.sp
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

private fun buildScheduledTime(scheduleTime: String): String {
    return try {
        val lt = LocalTime.parse(scheduleTime)
        LocalDate.now().atTime(lt).atOffset(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    } catch (e: Exception) {
        "${LocalDate.now()}T${scheduleTime}+00:00"
    }
}

@Composable
private fun AdherenceCard(adh: DiabetesAdherenceDto) {
    val pct = adh.adherencePercent
    val (bg, color) = when {
        pct >= 80 -> KindColors.RiskLowBg to KindColors.RiskLow
        pct >= 50 -> KindColors.RiskMediumBg to KindColors.RiskMedium
        else -> KindColors.RiskHighBg to KindColors.RiskHigh
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${adh.periodDays} kunlik mos kelish", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary, modifier = Modifier.weight(1f))
                Text("${pct.toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (pct / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(KindShapes.full),
                color = color,
                trackColor = color.copy(0.2f),
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("✓ ${adh.totalTaken} qabul", fontSize = 12.sp, color = KindColors.RiskLow)
                Text("✗ ${adh.totalMissed} o'tkazildi", fontSize = 12.sp, color = KindColors.RiskHigh)
                if (adh.streakDays > 0)
                    Text("🔥 ${adh.streakDays} kun ketma-ket", fontSize = 12.sp, color = KindColors.Warning)
            }
        }
    }
}

@Composable
private fun DiabetesMedCard(
    med: DiabetesMedicationDto,
    isTaken: Boolean,
    isMissed: Boolean,
    onTaken: () -> Unit,
    onMissed: () -> Unit,
) {
    val typeColor = when (med.medicationType) {
        "INSULIN" -> KindColors.AiPurple
        "TABLET" -> KindColors.Primary
        else -> KindColors.Accent
    }
    val typeLabel = when (med.medicationType) {
        "INSULIN" -> "Insulin"
        "TABLET" -> "Tabletka"
        else -> "Boshqa"
    }
    val cardBg = when {
        isTaken -> KindColors.RiskLowBg
        isMissed -> KindColors.RiskHighBg
        else -> KindColors.Surface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(if (isTaken || isMissed) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(KindShapes.large).background(typeColor.copy(0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (med.medicationType == "INSULIN") Icons.Default.Vaccines else Icons.Default.Medication,
                        null, tint = typeColor, modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(med.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.clip(KindShapes.chip).background(typeColor.copy(0.12f)).padding(horizontal = 7.dp, vertical = 2.dp)
                        ) { Text(typeLabel, fontSize = 10.sp, color = typeColor, fontWeight = FontWeight.SemiBold) }
                        if (!med.dosage.isNullOrBlank()) {
                            Spacer(Modifier.width(6.dp))
                            Text(med.dosage, fontSize = 12.sp, color = KindColors.TextSecondary)
                        }
                        if (med.insulinUnits != null) {
                            Spacer(Modifier.width(6.dp))
                            Text("${med.insulinUnits} birlik", fontSize = 12.sp, color = KindColors.AiPurple)
                        }
                    }
                }
                // Status icon
                when {
                    isTaken -> Icon(Icons.Default.CheckCircle, null, tint = KindColors.RiskLow, modifier = Modifier.size(28.dp))
                    isMissed -> Icon(Icons.Default.Cancel, null, tint = KindColors.RiskHigh, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = KindColors.TextTertiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(med.scheduleTime, fontSize = 13.sp, color = KindColors.TextSecondary)
                if (!med.beforeOrAfterMeal.isNullOrBlank()) {
                    Text(" · ${med.beforeOrAfterMeal}", fontSize = 12.sp, color = KindColors.TextTertiary)
                }
            }

            if (!isTaken && !isMissed) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onTaken,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = KindShapes.buttonPill,
                        colors = ButtonDefaults.buttonColors(containerColor = KindColors.RiskLow)
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Qabul qildim", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onMissed,
                        modifier = Modifier.height(42.dp),
                        shape = KindShapes.buttonPill,
                    ) {
                        Text("O'tkazib yubordim", fontSize = 12.sp, color = KindColors.TextSecondary)
                    }
                }
            }
        }
    }
}
