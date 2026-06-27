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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.diabetes.DiabetesSosRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiabetesSosScreen(
    apiService: ApiService,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    var glucoseValue by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            icon = { Icon(Icons.Default.Emergency, null, tint = KindColors.Error) },
            title = { Text("SOS yuborishni tasdiqlang", fontWeight = FontWeight.Bold) },
            text = { Text("Favqulodda yordamchi(lar)ingizga SOS xabari yuboriladi. Davom etasizmi?") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirm = false
                        scope.launch {
                            isLoading = true
                            errorMsg = null
                            val result = safeApiCall {
                                apiService.sendDiabetesSos(
                                    DiabetesSosRequest(
                                        message = message.trim().ifBlank { null },
                                        currentGlucose = glucoseValue.toDoubleOrNull()
                                    )
                                )
                            }
                            isLoading = false
                            when (result) {
                                is UiState.Success -> sent = true
                                is UiState.Error -> errorMsg = result.message
                                else -> {}
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KindColors.Error),
                    shape = KindShapes.buttonPill
                ) { Text("Ha, yuborish") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Bekor qilish") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SOS - Favqulodda yordam", fontWeight = FontWeight.Bold, color = KindColors.Error) },
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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            if (sent) {
                SosSentSuccess(onBack)
            } else {
                // Emergency icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(KindShapes.full)
                        .background(Brush.radialGradient(listOf(KindColors.Error.copy(0.2f), KindColors.Error.copy(0.05f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Emergency, null, tint = KindColors.Error, modifier = Modifier.size(52.dp))
                }

                Spacer(Modifier.height(16.dp))
                Text("Favqulodda yordam", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = KindColors.Error)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Ushbu tugma faqat haqiqiy favqulodda vaziyatlarda bosing. Yordamchi(lar)ingizga SOS xabari yuboriladi.",
                    fontSize = 14.sp, color = KindColors.TextSecondary, textAlign = TextAlign.Center, lineHeight = 20.sp
                )

                Spacer(Modifier.height(24.dp))

                // Current glucose input
                OutlinedTextField(
                    value = glucoseValue,
                    onValueChange = { if (it.matches(Regex("^\\d{0,3}(\\.\\d{0,2})?$"))) glucoseValue = it },
                    label = { Text("Hozirgi qand miqdori (ixtiyoriy)") },
                    trailingIcon = { Text("mmol/L", fontSize = 13.sp, modifier = Modifier.padding(end = 10.dp), color = KindColors.TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.large,
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Xabar (ixtiyoriy)") },
                    placeholder = { Text("Masalan: Qand pastlab ketdi, hushim boradi") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.large,
                    minLines = 2,
                    maxLines = 4,
                )

                errorMsg?.let {
                    Spacer(Modifier.height(10.dp))
                    Card(Modifier.fillMaxWidth(), shape = KindShapes.large, colors = CardDefaults.cardColors(containerColor = KindColors.ErrorBg)) {
                        Text(it, modifier = Modifier.padding(12.dp), color = KindColors.Error, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(28.dp))

                // SOS button
                Button(
                    onClick = { showConfirm = true },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = KindShapes.buttonPill,
                    colors = ButtonDefaults.buttonColors(containerColor = KindColors.Error)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(10.dp))
                        Text("Yuborilmoqda...", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Icon(Icons.Default.Emergency, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("SOS Yuborish", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Emergency number
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.xl,
                    colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, tint = KindColors.RiskHigh, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Tez yordam raqami", fontSize = 12.sp, color = KindColors.TextTertiary)
                            Text("103", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = KindColors.RiskHigh)
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SosSentSuccess(onBack: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(100.dp).clip(KindShapes.full).background(KindColors.RiskLow.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = KindColors.RiskLow, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("SOS Yuborildi!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = KindColors.RiskLow)
        Spacer(Modifier.height(10.dp))
        Text(
            "Yordamchi(lar)ingizga SOS xabari yuborildi. Ular siz bilan bog'lanadi. Joyingizdan ketmang.",
            fontSize = 14.sp, color = KindColors.TextSecondary, textAlign = TextAlign.Center, lineHeight = 20.sp
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onBack,
            shape = KindShapes.buttonPill,
            colors = ButtonDefaults.buttonColors(containerColor = KindColors.Primary),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Ortga qaytish", fontWeight = FontWeight.SemiBold) }
    }
}
