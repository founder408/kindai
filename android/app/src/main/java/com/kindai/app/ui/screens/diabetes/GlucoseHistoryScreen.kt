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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.diabetes.DiabetesRecordDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlucoseHistoryScreen(
    apiService: ApiService,
    onBack: () -> Unit,
    onNavigateToAddGlucose: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var records by remember { mutableStateOf<List<DiabetesRecordDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf("7d") }

    fun load(period: String) {
        scope.launch {
            isLoading = true
            val result = safeApiCall { apiService.getGlucoseRecords(period = period) }
            if (result is UiState.Success) records = result.data
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load(selectedPeriod) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Qand tarixi", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KindColors.Surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddGlucose,
                containerColor = KindColors.Primary,
                shape = KindShapes.full
            ) {
                Icon(Icons.Default.Add, null, tint = androidx.compose.ui.graphics.Color.White)
            }
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("7d" to "7 kun", "30d" to "30 kun", "90d" to "90 kun").forEach { (code, label) ->
                    FilterChip(
                        selected = selectedPeriod == code,
                        onClick = { selectedPeriod = code; load(code) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KindColors.Primary,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        )
                    )
                }
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(40.dp))
                }

                records.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.SearchOff, null, tint = KindColors.TextTertiary, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Bu davrda o'lchov yo'q", fontSize = 16.sp, color = KindColors.TextSecondary)
                        Spacer(Modifier.height(6.dp))
                        Text("Glukometringizdan o'lchab kiriting.", fontSize = 13.sp, color = KindColors.TextTertiary)
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = KindSpacing.screenHorizontal, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Summary row
                    item {
                        if (records.isNotEmpty()) {
                            val vals = records.map { it.glucoseValue }
                            val avg = vals.average()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = KindShapes.xl,
                                colors = CardDefaults.cardColors(containerColor = KindColors.PrimaryLight),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    SumItem("${records.size}", "O'lchov")
                                    SumItem("%.1f".format(avg), "O'rtacha")
                                    SumItem("%.1f".format(vals.max()), "Eng yuqori")
                                    SumItem("%.1f".format(vals.min()), "Eng past")
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    items(records) { rec ->
                        GlucoseRecordCard(rec)
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SumItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = KindColors.Primary)
        Text(label, fontSize = 11.sp, color = KindColors.TextTertiary)
    }
}

@Composable
private fun GlucoseRecordCard(rec: DiabetesRecordDto) {
    val (bg, color, label) = glucoseRiskStyle(rec.glucoseValue)
    val contextUz = when (rec.measurementContext) {
        "FASTING" -> "Och qoringa"
        "BEFORE_MEAL" -> "Ovqatdan oldin"
        "AFTER_MEAL" -> "Ovqatdan keyin"
        "BEDTIME" -> "Uyqudan oldin"
        else -> "Tasodifiy"
    }
    val sourceIcon = when (rec.source) {
        "GLUCOMETER" -> Icons.Default.DeviceHub
        "HEALTH_CONNECT" -> Icons.Default.PhoneAndroid
        else -> Icons.Default.Edit
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator + value
            Box(
                modifier = Modifier.size(56.dp).clip(KindShapes.large).background(bg),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%.1f".format(rec.glucoseValue), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
                    Text("mmol/L", fontSize = 8.sp, color = color.copy(0.7f))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.clip(KindShapes.chip).background(color.copy(0.12f)).padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(label, fontSize = 10.sp, color = color, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(contextUz, fontSize = 12.sp, color = KindColors.TextSecondary)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    rec.measuredAt.take(16).replace("T", " "),
                    fontSize = 12.sp, color = KindColors.TextTertiary
                )
                if (!rec.symptoms.isNullOrBlank()) {
                    Spacer(Modifier.height(3.dp))
                    Text("Alomatlar: ${rec.symptoms}", fontSize = 11.sp, color = KindColors.TextSecondary)
                }
            }
            Icon(sourceIcon, null, tint = KindColors.TextTertiary, modifier = Modifier.size(16.dp))
        }
    }
}
