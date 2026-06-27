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
import com.kindai.app.data.model.diabetes.DiabetesRecordCreateRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindPrimaryButton
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGlucoseScreen(
    apiService: ApiService,
    onBack: () -> Unit,
    onSaved: (runAi: Boolean) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var glucoseText by remember { mutableStateOf("") }
    var selectedContext by remember { mutableStateOf("RANDOM") }
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }
    var notes by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showAiDialog by remember { mutableStateOf(false) }

    val contextOptions = listOf(
        "FASTING" to "Och qoringa",
        "BEFORE_MEAL" to "Ovqatdan oldin",
        "AFTER_MEAL" to "Ovqatdan keyin",
        "BEDTIME" to "Uyqudan oldin",
        "RANDOM" to "Tasodifiy",
    )
    val symptomOptions = listOf(
        "holsizlik", "bosh aylanishi", "terlash", "chanqash",
        "ko'rish xiralashishi", "nafas qisishi", "boshqa"
    )

    if (showAiDialog) {
        AlertDialog(
            onDismissRequest = { onSaved(false) },
            icon = { Icon(Icons.Default.AutoAwesome, null, tint = KindColors.AiPurple) },
            title = { Text("AI tahlil", fontWeight = FontWeight.Bold) },
            text = { Text("Qand ko'rsatkichi saqlandi. AI tahlilni hozir ishga tushirasizmi?") },
            confirmButton = {
                Button(
                    onClick = { onSaved(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = KindColors.AiPurple),
                    shape = KindShapes.buttonPill
                ) { Text("Ha, tahlil qiling") }
            },
            dismissButton = {
                TextButton(onClick = { onSaved(false) }) { Text("Keyinroq") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Qand kiritish", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // ── Glucose input ─────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = KindShapes.cardLarge,
                colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Qand miqdori", fontSize = 14.sp, color = KindColors.TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = glucoseText,
                        onValueChange = { v ->
                            if (v.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?$"))) glucoseText = v
                        },
                        placeholder = { Text("0.0", fontSize = 32.sp, color = KindColors.TextTertiary) },
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary),
                        trailingIcon = {
                            Text("mmol/L", fontSize = 14.sp, color = KindColors.TextSecondary, modifier = Modifier.padding(end = 12.dp))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = KindShapes.large,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                    )
                    // Normal range hint
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Normal: 4.0–7.8 (och qoringa), 5.0–10.0 (ovqatdan keyin)", fontSize = 11.sp, color = KindColors.TextTertiary)

                    // Live risk preview
                    val glucoseVal = glucoseText.toDoubleOrNull()
                    if (glucoseVal != null && glucoseVal > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val (_, color, label) = glucoseRiskStyle(glucoseVal)
                        Box(
                            modifier = Modifier.clip(KindShapes.chip).background(color.copy(0.12f)).padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Context selector ──────────────────────────────────────────────
            Text("O'lchov holati", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                contextOptions.chunked(3).forEach { rowChunk ->
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowChunk.forEach { (code, label) ->
                            val selected = selectedContext == code
                            FilterChip(
                                selected = selected,
                                onClick = { selectedContext = code },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KindColors.Primary,
                                    selectedLabelColor = Color.White,
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Symptoms ──────────────────────────────────────────────────────
            Text("Alomatlar (ixtiyoriy)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(symptomOptions, selectedSymptoms) { s ->
                selectedSymptoms = if (s in selectedSymptoms) selectedSymptoms - s else selectedSymptoms + s
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Notes ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Izoh (ixtiyoriy)") },
                modifier = Modifier.fillMaxWidth(),
                shape = KindShapes.large,
                minLines = 2,
                maxLines = 3,
            )

            errorMsg?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.large,
                    colors = CardDefaults.cardColors(containerColor = KindColors.ErrorBg)
                ) {
                    Text(it, modifier = Modifier.padding(14.dp), color = KindColors.Error, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            KindPrimaryButton(
                text = if (isLoading) "Saqlanmoqda..." else "Saqlash",
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val val_ = glucoseText.toDoubleOrNull()
                    if (val_ == null || val_ <= 0) { errorMsg = "To'g'ri qand miqdorini kiriting (masalan: 6.5)"; return@KindPrimaryButton }
                    if (val_ > 55) { errorMsg = "Qand miqdori 55 mmol/L dan oshmasligi kerak"; return@KindPrimaryButton }
                    scope.launch {
                        isLoading = true
                        errorMsg = null
                        val now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        val result = safeApiCall {
                            apiService.addGlucoseRecord(
                                DiabetesRecordCreateRequest(
                                    glucoseValue = val_,
                                    measurementContext = selectedContext,
                                    measuredAt = now,
                                    symptoms = selectedSymptoms.joinToString(",").ifBlank { null },
                                    notes = notes.trim().ifBlank { null },
                                )
                            )
                        }
                        isLoading = false
                        when (result) {
                            is UiState.Success -> showAiDialog = true
                            is UiState.Error -> errorMsg = result.message
                            else -> {}
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FlowRow(
    items: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    val rows = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { s ->
                    FilterChip(
                        selected = s in selected,
                        onClick = { onToggle(s) },
                        label = { Text(s, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KindColors.AiPurple,
                            selectedLabelColor = Color.White,
                        )
                    )
                }
            }
        }
    }
}
