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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.health.HealthTrendsDto
import com.kindai.app.data.model.health.TrendPointDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindTopAppBar
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderlyTrendsScreen(
    apiService: ApiService,
    onBack: () -> Unit
) {
    var selectedPeriod by remember { mutableStateOf("weekly") }
    var trends by remember { mutableStateOf<HealthTrendsDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun load(period: String) {
        scope.launch {
            isLoading = true
            val result = safeApiCall { apiService.getMyHealthTrends(period) }
            if (result is UiState.Success) trends = result.data
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load(selectedPeriod) }

    Scaffold(
        topBar = { KindTopAppBar(title = "Sog'liq trendlari", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KindColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KindSpacing.screenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Period selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("daily" to "Kunlik", "weekly" to "Haftalik", "monthly" to "Oylik").forEach { (key, label) ->
                    FilterChip(
                        selected = selectedPeriod == key,
                        onClick = {
                            selectedPeriod = key
                            load(key)
                        },
                        label = { Text(label, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KindColors.Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(40.dp))
                }
            } else if (trends == null || trends!!.points.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TrendingUp, null, tint = KindColors.TextTertiary, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Hali ma'lumot yo'q", fontSize = 18.sp, color = KindColors.TextSecondary)
                        Text("Sog'liq ko'rsatkichlarini kiriting", fontSize = 14.sp, color = KindColors.TextTertiary)
                    }
                }
            } else {
                val points = trends!!.points

                TrendMetricCard(
                    title = "Qon bosimi",
                    icon = Icons.Default.Favorite,
                    color = KindColors.Error,
                    points = points,
                    getValue = { it.systolicBp?.let { v -> "%.0f/%.0f".format(v, it.diastolicBp ?: 0.0) } },
                    getNumericValue = { it.systolicBp }
                )

                Spacer(modifier = Modifier.height(12.dp))

                TrendMetricCard(
                    title = "Yurak urishi (bpm)",
                    icon = Icons.Default.MonitorHeart,
                    color = KindColors.RiskHigh,
                    points = points,
                    getValue = { it.heartRate?.let { v -> "%.0f".format(v) } },
                    getNumericValue = { it.heartRate }
                )

                Spacer(modifier = Modifier.height(12.dp))

                TrendMetricCard(
                    title = "Qon shakar (mmol/L)",
                    icon = Icons.Default.Bloodtype,
                    color = KindColors.Warning,
                    points = points,
                    getValue = { it.bloodSugar?.let { v -> "%.1f".format(v) } },
                    getNumericValue = { it.bloodSugar }
                )

                Spacer(modifier = Modifier.height(12.dp))

                TrendMetricCard(
                    title = "SpO2 (%)",
                    icon = Icons.Default.Air,
                    color = KindColors.Info,
                    points = points,
                    getValue = { it.spo2?.let { v -> "%.0f".format(v) } },
                    getNumericValue = { it.spo2 }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TrendMetricCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    points: List<TrendPointDto>,
    getValue: (TrendPointDto) -> String?,
    getNumericValue: (TrendPointDto) -> Double?,
) {
    val validPoints = points.filter { getNumericValue(it) != null }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(KindSpacing.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(KindShapes.medium).background(color.copy(0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
            }

            if (validPoints.isEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Ma'lumot yo'q", fontSize = 14.sp, color = KindColors.TextTertiary)
            } else {
                Spacer(modifier = Modifier.height(12.dp))

                // Simple bar visualization
                val maxVal = validPoints.mapNotNull { getNumericValue(it) }.maxOrNull() ?: 1.0
                val minVal = validPoints.mapNotNull { getNumericValue(it) }.minOrNull() ?: 0.0
                val range = (maxVal - minVal).takeIf { it > 0 } ?: 1.0

                validPoints.takeLast(7).forEach { point ->
                    val value = getNumericValue(point) ?: return@forEach
                    val fraction = ((value - minVal) / range).toFloat().coerceIn(0.05f, 1f)
                    val displayValue = getValue(point) ?: ""

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            point.date.takeLast(5),
                            fontSize = 12.sp,
                            color = KindColors.TextTertiary,
                            modifier = Modifier.width(44.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp)
                                .clip(KindShapes.full)
                                .background(KindColors.Divider)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .clip(KindShapes.full)
                                    .background(color.copy(0.7f))
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            displayValue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = color,
                            modifier = Modifier.width(52.dp)
                        )
                    }
                }

                // Latest value prominent display
                val latest = validPoints.last()
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = KindColors.Divider)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("So'nggi: ", fontSize = 13.sp, color = KindColors.TextTertiary)
                    Text(
                        getValue(latest) ?: "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }
}
