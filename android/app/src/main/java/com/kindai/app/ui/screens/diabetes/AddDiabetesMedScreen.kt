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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.diabetes.DiabetesMedicationCreateRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindPrimaryButton
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiabetesMedScreen(
    apiService: ApiService,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var medType by remember { mutableStateOf("TABLET") }
    var dosage by remember { mutableStateOf("") }
    var insulinUnits by remember { mutableStateOf("") }
    var scheduleTime by remember { mutableStateOf("08:00") }
    var frequency by remember { mutableStateOf("DAILY") }
    var mealContext by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val typeOptions = listOf("TABLET" to "Tabletka", "INSULIN" to "Insulin", "OTHER" to "Boshqa")
    val freqOptions = listOf("DAILY" to "Kuniga 1 marta", "TWICE_DAILY" to "Kuniga 2 marta", "THREE_TIMES" to "Kuniga 3 marta", "WEEKLY" to "Haftada 1 marta", "AS_NEEDED" to "Zarur bo'lganda")
    val mealOptions = listOf("" to "Belgilanmagan", "BEFORE" to "Ovqatdan oldin", "AFTER" to "Ovqatdan keyin", "WITH" to "Ovqat bilan")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dori qo'shish", fontWeight = FontWeight.Bold) },
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
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Dori nomi *") },
                leadingIcon = { Icon(Icons.Default.Medication, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = KindShapes.large,
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))

            Text("Dori turi", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                typeOptions.forEach { (code, label) ->
                    val sel = medType == code
                    FilterChip(
                        selected = sel,
                        onClick = { medType = code },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KindColors.Primary,
                            selectedLabelColor = Color.White,
                        )
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            if (medType != "INSULIN") {
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Miqdor (masalan: 500mg, 1 tb)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.large,
                    singleLine = true,
                )
                Spacer(Modifier.height(12.dp))
            } else {
                OutlinedTextField(
                    value = insulinUnits,
                    onValueChange = { if (it.matches(Regex("^\\d{0,3}(\\.\\d{0,1})?$"))) insulinUnits = it },
                    label = { Text("Insulin birligi (birlik)") },
                    leadingIcon = { Icon(Icons.Default.Vaccines, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.large,
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                )
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.large,
                    colors = CardDefaults.cardColors(containerColor = KindColors.WarningBg)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = KindColors.Warning, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Kind AI insulin dozasini belgilamaydi. Faqat shifokor tavsiyasiga amal qiling.", fontSize = 12.sp, color = KindColors.TextSecondary, lineHeight = 17.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = scheduleTime,
                onValueChange = { scheduleTime = it },
                label = { Text("Qabul vaqti (HH:MM) *") },
                leadingIcon = { Icon(Icons.Default.Schedule, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = KindShapes.large,
                singleLine = true,
                placeholder = { Text("08:00") },
            )
            Spacer(Modifier.height(16.dp))

            Text("Chastota", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                freqOptions.forEach { (code, label) ->
                    val sel = frequency == code
                    Card(
                        onClick = { frequency = code },
                        shape = KindShapes.large,
                        colors = CardDefaults.cardColors(containerColor = if (sel) KindColors.PrimaryContainer else KindColors.Surface),
                        elevation = CardDefaults.cardElevation(if (sel) 0.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = sel, onClick = { frequency = code }, colors = RadioButtonDefaults.colors(selectedColor = KindColors.Primary))
                            Spacer(Modifier.width(8.dp))
                            Text(label, fontSize = 14.sp, color = if (sel) KindColors.Primary else KindColors.TextPrimary)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Ovqat bilan munosabati", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                mealOptions.forEach { (code, label) ->
                    FilterChip(
                        selected = mealContext == code,
                        onClick = { mealContext = code },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = KindColors.Secondary, selectedLabelColor = Color.White)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Qo'shimcha ko'rsatma") },
                modifier = Modifier.fillMaxWidth(),
                shape = KindShapes.large,
                minLines = 2,
                maxLines = 3,
            )

            errorMsg?.let {
                Spacer(Modifier.height(12.dp))
                Card(Modifier.fillMaxWidth(), shape = KindShapes.large, colors = CardDefaults.cardColors(containerColor = KindColors.ErrorBg)) {
                    Text(it, modifier = Modifier.padding(14.dp), color = KindColors.Error, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            KindPrimaryButton(
                text = if (isLoading) "Saqlanmoqda..." else "Saqlash",
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (name.isBlank()) { errorMsg = "Dori nomi kiritilishi shart"; return@KindPrimaryButton }
                    if (scheduleTime.isBlank()) { errorMsg = "Vaqt kiritilishi shart"; return@KindPrimaryButton }
                    scope.launch {
                        isLoading = true
                        errorMsg = null
                        val timeFormatted = scheduleTime.trim().let { t ->
                            if (t.length == 5) "$t:00" else t
                        }
                        val result = safeApiCall {
                            apiService.addDiabetesMedication(
                                DiabetesMedicationCreateRequest(
                                    name = name.trim(),
                                    medicationType = medType,
                                    dosage = dosage.trim().ifBlank { null },
                                    insulinUnits = if (medType == "INSULIN") insulinUnits.toDoubleOrNull() else null,
                                    scheduleTime = timeFormatted,
                                    frequency = frequency,
                                    beforeOrAfterMeal = mealContext.ifBlank { null },
                                    instructions = instructions.trim().ifBlank { null },
                                )
                            )
                        }
                        isLoading = false
                        when (result) {
                            is UiState.Success -> onSaved()
                            is UiState.Error -> errorMsg = result.message
                            else -> {}
                        }
                    }
                }
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
