package com.kindai.app.ui.screens.watch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.watch.WatchDataCreateRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindAiTopBar
import com.kindai.app.ui.components.PrimaryButton
import com.kindai.app.ui.theme.KindBlue
import com.kindai.app.ui.theme.KindOrange
import com.kindai.app.ui.theme.KindRedDark
import kotlinx.coroutines.launch

@Composable
fun WatchDemoScreen(
    elderlyId: String,
    apiService: ApiService,
    onBack: () -> Unit,
    onNavigateToAi: () -> Unit
) {
    var heartRate by remember { mutableStateOf("78") }
    var steps by remember { mutableStateOf("3500") }
    var sleepHours by remember { mutableStateOf("7.2") }
    var spo2 by remember { mutableStateOf("97") }
    var fallDetected by remember { mutableStateOf(false) }
    var battery by remember { mutableStateOf("80") }
    var isLoading by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun applyPreset(hr: Int, st: Int, sl: Double, sp: Int, fall: Boolean, bat: Int) {
        heartRate = hr.toString()
        steps = st.toString()
        sleepHours = sl.toString()
        spo2 = sp.toString()
        fallDetected = fall
        battery = bat.toString()
    }

    Scaffold(
        topBar = { KindAiTopBar(title = "Smart soat demo", onBack = onBack) }
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
                        Text("Smart soat ma'lumotlari yuborildi!", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onNavigateToAi) {
                            Text("AI tahlilni ishga tushirish")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { success = false }) {
                            Text("Yana yuborish")
                        }
                    }
                }
                return@Scaffold
            }

            // Presets
            Text("Demo presetlar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { applyPreset(78, 3500, 7.2, 97, false, 80) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Normal", fontSize = MaterialTheme.typography.labelLarge.fontSize)
                }
                Button(
                    onClick = { applyPreset(130, 120, 3.5, 91, false, 25) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KindOrange)
                ) {
                    Text("Xavfli", fontSize = MaterialTheme.typography.labelLarge.fontSize)
                }
                Button(
                    onClick = { applyPreset(128, 50, 2.8, 90, true, 12) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = KindRedDark)
                ) {
                    Text("Yiqilish", fontSize = MaterialTheme.typography.labelLarge.fontSize)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = heartRate,
                onValueChange = { heartRate = it },
                label = { Text("Yurak urishi (bpm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = steps,
                onValueChange = { steps = it },
                label = { Text("Qadam") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = sleepHours,
                onValueChange = { sleepHours = it },
                label = { Text("Uxlash (soat)") },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Yiqilish aniqlandi", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = fallDetected, onCheckedChange = { fallDetected = it })
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = battery,
                onValueChange = { battery = it },
                label = { Text("Batareya (%)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Yuborish",
                isLoading = isLoading,
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val result = safeApiCall {
                            apiService.createWatchData(
                                elderlyId,
                                WatchDataCreateRequest(
                                    heartRate = heartRate.toIntOrNull(),
                                    steps = steps.toIntOrNull(),
                                    sleepHours = sleepHours.toDoubleOrNull(),
                                    spo2 = spo2.toIntOrNull(),
                                    fallDetected = fallDetected,
                                    battery = battery.toIntOrNull()
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
