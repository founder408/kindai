package com.kindai.app.ui.screens.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.health.HealthRecordCreateRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindAiTopBar
import com.kindai.app.ui.components.PrimaryButton
import com.kindai.app.ui.theme.KindBlue
import kotlinx.coroutines.launch

@Composable
fun AddHealthRecordScreen(
    elderlyId: String,
    mode: String,
    apiService: ApiService,
    onBack: () -> Unit,
    onNavigateToAi: () -> Unit
) {
    var systolic by remember { mutableStateOf("") }
    var diastolic by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("") }
    var bloodSugar by remember { mutableStateOf("") }
    var spo2 by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val isSimple = mode == "simple"

    Scaffold(
        topBar = {
            KindAiTopBar(
                title = if (isSimple) "Sog'lig'imni kiritish" else "Sog'liq ma'lumoti",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (success) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = KindBlue.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ma'lumotlar saqlandi!", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onNavigateToAi) {
                            Text("AI tahlilni ishga tushirish")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onBack) {
                            Text("Orqaga")
                        }
                    }
                }
                return@Scaffold
            }

            // Blood pressure
            Text("Qon bosimi", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = systolic,
                    onValueChange = { systolic = it },
                    label = { Text("Yuqori") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = diastolic,
                    onValueChange = { diastolic = it },
                    label = { Text("Pastki") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = heartRate,
                onValueChange = { heartRate = it },
                label = { Text("Yurak urishi (bpm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (!isSimple) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = bloodSugar,
                    onValueChange = { bloodSugar = it },
                    label = { Text("Qon shakar (mmol/L)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = spo2,
                    onValueChange = { spo2 = it },
                    label = { Text("SpO2 (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = temperature,
                    onValueChange = { temperature = it },
                    label = { Text("Harorat (°C)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = symptoms,
                onValueChange = { symptoms = it },
                label = { Text("Alomatlar") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = mood,
                onValueChange = { mood = it },
                label = { Text("Kayfiyat") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Saqlash",
                isLoading = isLoading,
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val result = safeApiCall {
                            apiService.createHealthRecord(
                                elderlyId,
                                HealthRecordCreateRequest(
                                    systolicBp = systolic.toIntOrNull(),
                                    diastolicBp = diastolic.toIntOrNull(),
                                    bloodSugar = bloodSugar.toDoubleOrNull(),
                                    heartRate = heartRate.toIntOrNull(),
                                    spo2 = spo2.toIntOrNull(),
                                    temperature = temperature.toDoubleOrNull(),
                                    symptoms = symptoms.trim().ifBlank { null },
                                    mood = mood.trim().ifBlank { null }
                                )
                            )
                        }
                        when (result) {
                            is UiState.Success -> success = true
                            is UiState.Error -> errorMessage = result.message
                            else -> {}
                        }
                        isLoading = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
