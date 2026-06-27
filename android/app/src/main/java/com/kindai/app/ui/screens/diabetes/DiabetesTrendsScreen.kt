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
import com.kindai.app.data.model.diabetes.DailyGlucoseData
import com.kindai.app.data.model.diabetes.GlucoseTrendsDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiabetesTrendsScreen(
    apiService: ApiService,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var trends by remember { mutableStateOf<GlucoseTrendsDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf("7d") }

    fun load(period: String) {
        scope.launch {
            isLoading = true
            val result = safeApiCall { apiService.getGlucoseTrends(period) }
            if (result is UiState.Success) trends = result.data
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load(selectedPeriod) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tendensiyalar", fontWeight = FontWeight.Bold) },
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
        ) {
            // Period tabs
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
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(40.dp))
                }

                trends == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.BarChart, null, tint = KindColors.TextTertiary, modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Ma'lumot yo'q", fontSize = 16.sp, color = KindColors.TextSecondary)
                    }
                }

                else -> Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    trends?.let { t ->
                        Spacer(Modifier.height(4.dp))

                        // Stat cards row
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = KindSpacing.screenHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard("O'rtacha", "%.1f".format(t.averageGlucose ?: 0.0), "mmol/L", KindColors.Primary, Modifier.weight(1f))
                            StatCard("Eng yuqori", "%.1f".format(t.highestGlucose ?: 0.0), "mmol/L", KindColors.RiskHigh, Modifier.weight(1f))
                            StatCard("Eng past", "%.1f".format(t.lowestGlucose ?: 0.0), "mmol/L", KindColors.RiskLow, Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = KindSpacing.screenHorizontal),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard("Och qoringa", "%.1f".format(t.fastingAverage ?: 0.0), "mmol/L", KindColors.Accent, Modifier.weight(1f))
                            StatCard("Ovqatdan keyin", "%.1f".format(t.afterMealAverage ?: 0.0), "mmol/L", KindColors.AiPurple, Modifier.weight(1f))
                            StatCard("Jami o'lchov", "${t.totalRecords}", "ta", KindColors.TextSecondary, Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(16.dp))

                        // Trend direction banner
                        t.trendDirection?.let { dir ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = KindSpacing.screenHorizontal),
                                shape = KindShapes.xl,
                                colors = CardDefaults.cardColors(
                                    containerColor = when (dir) {
                                        "UP" -> KindColors.RiskHighBg
                                        "DOWN" -> KindColors.RiskLowBg
                                        else -> KindColors.PrimaryLight
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        when (dir) { "UP" -> Icons.Default.TrendingUp; "DOWN" -> Icons.Default.TrendingDown; else -> Icons.Default.TrendingFlat },
                                        null,
                                        tint = when (dir) { "UP" -> KindColors.RiskHigh; "DOWN" -> KindColors.RiskLow; else -> KindColors.Primary },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        when (dir) {
                                            "UP" -> "Qand ko'rsatkichlari o'sish tendensiyasida"
                                            "DOWN" -> "Qand ko'rsatkichlari pasayish tendensiyasida"
                                            else -> "Qand ko'rsatkichlari barqaror"
                                        },
                                        fontSize = 14.sp,
                                        color = KindColors.TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }

                        // Bar chart
                        if (t.dailyData.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = KindSpacing.screenHorizontal),
                                shape = KindShapes.xl,
                                colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Kunlik o'rtacha", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                                    Spacer(Modifier.height(12.dp))
                                    GlucoseBarChart(t.dailyData)
                                }
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, unit: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = KindShapes.xl,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Text(unit, fontSize = 9.sp, color = KindColors.TextTertiary)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 10.sp, color = KindColors.TextSecondary, maxLines = 1)
        }
    }
}

@Composable
private fun GlucoseBarChart(data: List<DailyGlucoseData>) {
    val maxVal = data.maxOfOrNull { it.avg } ?: 15.0
    val chartMaxVal = maxOf(maxVal, 10.0)

    Column {
        data.takeLast(14).forEach { day ->
            val fraction = (day.avg / chartMaxVal).coerceIn(0.0, 1.0).toFloat()
            val (_, color, _) = glucoseRiskStyle(day.avg)
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    day.date.takeLast(5),
                    fontSize = 10.sp,
                    color = KindColors.TextTertiary,
                    modifier = Modifier.width(40.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(KindShapes.full)
                        .background(color.copy(0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .clip(KindShapes.full)
                            .background(color)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "%.1f".format(day.avg),
                    fontSize = 11.sp,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}
