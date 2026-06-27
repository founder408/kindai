package com.kindai.app.ui.screens.elderly_own

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.health.HealthRecordCreateRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindTopAppBar
import com.kindai.app.ui.components.SafetyDisclaimerCard
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderlyOwnHealthScreen(
    apiService: ApiService,
    onBack: () -> Unit,
    onNavigateToAi: () -> Unit = {}
) {
    var systolicBp by remember { mutableStateOf("") }
    var diastolicBp by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var bloodSugar by remember { mutableStateOf("") }
    var spo2 by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val moods = listOf("Yaxshi", "O'rtacha", "Yomon", "Kasal")

    fun save() {
        scope.launch {
            isSaving = true
            errorMsg = null
            val request = HealthRecordCreateRequest(
                systolicBp = systolicBp.toIntOrNull(),
                diastolicBp = diastolicBp.toIntOrNull(),
                heartRate = heartRate.toIntOrNull(),
                bloodSugar = bloodSugar.toDoubleOrNull(),
                spo2 = spo2.toIntOrNull(),
                temperature = temperature.toDoubleOrNull(),
                symptoms = symptoms.takeIf { it.isNotBlank() },
                mood = mood.takeIf { it.isNotBlank() }
            )
            val result = safeApiCall { apiService.createMyHealthRecord(request) }
            if (result is UiState.Success) {
                showSuccessDialog = true
            } else if (result is UiState.Error) {
                errorMsg = result.message
            }
            isSaving = false
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Default.CheckCircle, null, tint = KindColors.Secondary, modifier = Modifier.size(40.dp)) },
            title = { Text("Saqlandi!", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = { Text("Ma'lumotlaringiz saqlandi. AI tahlil qilsinmi?", fontSize = 16.sp, lineHeight = 24.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onNavigateToAi()
                }) { Text("Ha, tahlil qiling", fontSize = 16.sp, color = KindColors.AiPurple) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onBack()
                }) { Text("Keyinroq", fontSize = 16.sp, color = KindColors.TextSecondary) }
            },
            shape = KindShapes.cardLarge
        )
    }

    Scaffold(
        topBar = { KindTopAppBar(title = "Sog'ligimni kiritish", onBack = onBack) }
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

            HealthSectionHeader(icon = Icons.Default.Favorite, title = "Qon bosimi", color = KindColors.Error)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ElderlyInputField(
                    modifier = Modifier.weight(1f),
                    value = systolicBp,
                    onValueChange = { systolicBp = it },
                    label = "Yuqori (mmHg)",
                    placeholder = "120",
                    keyboardType = KeyboardType.Number
                )
                ElderlyInputField(
                    modifier = Modifier.weight(1f),
                    value = diastolicBp,
                    onValueChange = { diastolicBp = it },
                    label = "Quyi (mmHg)",
                    placeholder = "80",
                    keyboardType = KeyboardType.Number
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            HealthSectionHeader(icon = Icons.Default.MonitorHeart, title = "Yurak urishi", color = KindColors.RiskHigh)
            Spacer(modifier = Modifier.height(12.dp))
            ElderlyInputField(
                modifier = Modifier.fillMaxWidth(),
                value = heartRate,
                onValueChange = { heartRate = it },
                label = "Yurak urishi (bpm)",
                placeholder = "72",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(20.dp))
            HealthSectionHeader(icon = Icons.Default.Bloodtype, title = "Qon shakar", color = KindColors.Warning)
            Spacer(modifier = Modifier.height(12.dp))
            ElderlyInputField(
                modifier = Modifier.fillMaxWidth(),
                value = bloodSugar,
                onValueChange = { bloodSugar = it },
                label = "Qon shakar (mmol/L)",
                placeholder = "5.5",
                keyboardType = KeyboardType.Decimal
            )

            Spacer(modifier = Modifier.height(20.dp))
            HealthSectionHeader(icon = Icons.Default.Air, title = "Kislorod (SpO2)", color = KindColors.Info)
            Spacer(modifier = Modifier.height(12.dp))
            ElderlyInputField(
                modifier = Modifier.fillMaxWidth(),
                value = spo2,
                onValueChange = { spo2 = it },
                label = "SpO2 (%)",
                placeholder = "98",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(20.dp))
            HealthSectionHeader(icon = Icons.Default.Thermostat, title = "Harorat", color = KindColors.Secondary)
            Spacer(modifier = Modifier.height(12.dp))
            ElderlyInputField(
                modifier = Modifier.fillMaxWidth(),
                value = temperature,
                onValueChange = { temperature = it },
                label = "Harorat (°C)",
                placeholder = "36.6",
                keyboardType = KeyboardType.Decimal
            )

            Spacer(modifier = Modifier.height(20.dp))
            HealthSectionHeader(icon = Icons.Default.SentimentSatisfied, title = "Kayfiyat", color = KindColors.AiPurple)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                moods.forEach { m ->
                    FilterChip(
                        selected = mood == m,
                        onClick = { mood = if (mood == m) "" else m },
                        label = { Text(m, fontSize = 14.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KindColors.AiPurple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HealthSectionHeader(icon = Icons.Default.Notes, title = "Belgilar / Shikoyatlar", color = KindColors.TextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = symptoms,
                onValueChange = { symptoms = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                placeholder = { Text("Bosh og'riq, ko'ngil aynish...", fontSize = 15.sp) },
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                shape = KindShapes.card,
                maxLines = 4
            )

            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = KindColors.ErrorBg),
                    shape = KindShapes.card
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = KindColors.Error, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMsg!!, fontSize = 14.sp, color = KindColors.Error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SafetyDisclaimerCard()
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                enabled = !isSaving,
                shape = KindShapes.buttonPill,
                colors = ButtonDefaults.buttonColors(containerColor = KindColors.Secondary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Saqlash", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HealthSectionHeader(icon: ImageVector, title: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(KindShapes.medium)
                .background(color.copy(0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
    }
}

@Composable
private fun ElderlyInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(64.dp),
        label = { Text(label, fontSize = 13.sp) },
        placeholder = { Text(placeholder, fontSize = 15.sp) },
        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = KindShapes.card,
        singleLine = true
    )
}
