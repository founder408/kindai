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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.diabetes.DiabetesProfileCreateRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindPrimaryButton
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiabetesSetupScreen(
    apiService: ApiService,
    onSetupComplete: () -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var diabetesType by remember { mutableStateOf("TYPE_2") }
    var emergencyPhone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val typeOptions = listOf(
        "TYPE_1" to "1-tur qand kasalligi",
        "TYPE_2" to "2-tur qand kasalligi",
        "PREDIABETES" to "Prediabet",
        "GESTATIONAL" to "Homiladorlik diabeti",
        "UNKNOWN" to "Aniqlanmagan",
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilni sozlash", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
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

            // Header card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KindShapes.cardLarge)
                    .background(KindGradients.PrimaryGradient)
                    .padding(24.dp)
            ) {
                Column {
                    Icon(Icons.Default.MonitorHeart, null, tint = androidx.compose.ui.graphics.Color.White.copy(0.8f), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Kind AI — Qand nazorati", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Profilingizni to'ldirib, qand ko'rsatkichlarini kuzatishni boshlang.", fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.White.copy(0.85f), lineHeight = 20.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Asosiy ma'lumotlar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Ism-familiya *") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = KindShapes.large,
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) age = it },
                label = { Text("Yosh") },
                leadingIcon = { Icon(Icons.Default.Cake, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = KindShapes.large,
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = emergencyPhone,
                onValueChange = { emergencyPhone = it },
                label = { Text("Favqulodda aloqa raqami") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = KindShapes.large,
                singleLine = true,
                placeholder = { Text("+998 XX XXX XX XX") },
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Qand kasalligi turi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            typeOptions.forEach { (code, label) ->
                val selected = diabetesType == code
                Card(
                    onClick = { diabetesType = code },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = KindShapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected) KindColors.PrimaryContainer else KindColors.Surface
                    ),
                    elevation = CardDefaults.cardElevation(if (selected) 0.dp else 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = { diabetesType = code },
                            colors = RadioButtonDefaults.colors(selectedColor = KindColors.Primary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(label, fontSize = 15.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, color = if (selected) KindColors.Primary else KindColors.TextPrimary)
                    }
                }
            }

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
                text = if (isLoading) "Saqlanmoqda..." else "Boshlash",
                onClick = {
                    if (fullName.isBlank()) { errorMsg = "Ism-familiya kiritilishi shart"; return@KindPrimaryButton }
                    scope.launch {
                        isLoading = true
                        errorMsg = null
                        val result = safeApiCall {
                            apiService.createDiabetesProfile(
                                DiabetesProfileCreateRequest(
                                    fullName = fullName.trim(),
                                    age = age.toIntOrNull(),
                                    diabetesType = diabetesType,
                                    emergencyPhone = emergencyPhone.trim().ifBlank { null },
                                )
                            )
                        }
                        isLoading = false
                        when (result) {
                            is UiState.Success -> onSetupComplete()
                            is UiState.Error -> errorMsg = result.message
                            else -> {}
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
