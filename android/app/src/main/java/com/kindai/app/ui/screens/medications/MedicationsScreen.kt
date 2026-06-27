package com.kindai.app.ui.screens.medications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.medication.MedicationCreateRequest
import com.kindai.app.data.model.medication.MedicationDto
import com.kindai.app.data.model.medication.MedicationTakenRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.ErrorView
import com.kindai.app.ui.components.KindAiTopBar
import com.kindai.app.ui.components.LoadingView
import com.kindai.app.ui.theme.KindGreen
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MedicationsScreen(
    elderlyId: String,
    apiService: ApiService,
    onBack: () -> Unit
) {
    var uiState by remember { mutableStateOf<UiState<List<MedicationDto>>>(UiState.Loading) }
    var showAddDialog by remember { mutableStateOf(false) }
    var takenMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            uiState = UiState.Loading
            uiState = safeApiCall { apiService.getMedications(elderlyId) }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    if (showAddDialog) {
        AddMedicationDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { request ->
                scope.launch {
                    safeApiCall { apiService.createMedication(elderlyId, request) }
                    showAddDialog = false
                    loadData()
                }
            }
        )
    }

    Scaffold(
        topBar = { KindAiTopBar(title = "Dorilar", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Qo'shish")
            }
        },
        snackbarHost = {
            if (takenMessage != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { takenMessage = null }) { Text("OK") }
                    }
                ) { Text(takenMessage!!) }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorView(state.message, onRetry = { loadData() }, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                        Text("Dorilar hali qo'shilmagan.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(state.data) { med ->
                            MedicationCard(
                                medication = med,
                                onMarkTaken = {
                                    scope.launch {
                                        val now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                                        val result = safeApiCall {
                                            apiService.markMedicationTaken(
                                                med.id,
                                                MedicationTakenRequest(scheduledTime = now)
                                            )
                                        }
                                        if (result is UiState.Success) {
                                            takenMessage = "${med.name} - qabul qilindi!"
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun MedicationCard(medication: MedicationDto, onMarkTaken: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (medication.dosage != null) {
                Text("Doza: ${medication.dosage}", style = MaterialTheme.typography.bodyMedium)
            }
            Text("Vaqt: ${medication.timeToTake}", style = MaterialTheme.typography.bodyMedium)
            if (medication.instruction != null) {
                Text(medication.instruction, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onMarkTaken,
                colors = ButtonDefaults.buttonColors(containerColor = KindGreen),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Ichdim ✓", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onAdd: (MedicationCreateRequest) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var instruction by remember { mutableStateOf("") }
    var timeToTake by remember { mutableStateOf("09:00:00") }
    var beforeAfter by remember { mutableStateOf("after") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dori qo'shish") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nomi *") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Doza") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = instruction, onValueChange = { instruction = it }, label = { Text("Ko'rsatma") }, singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = timeToTake, onValueChange = { timeToTake = it }, label = { Text("Vaqt (HH:mm:ss)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        MedicationCreateRequest(
                            name = name.trim(),
                            dosage = dosage.trim().ifBlank { null },
                            instruction = instruction.trim().ifBlank { null },
                            timeToTake = timeToTake.trim(),
                            beforeOrAfterMeal = beforeAfter
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) { Text("Qo'shish") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Bekor qilish") }
        }
    )
}
