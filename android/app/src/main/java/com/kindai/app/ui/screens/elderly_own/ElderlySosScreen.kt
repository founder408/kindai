package com.kindai.app.ui.screens.elderly_own

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.sos.SosRequest
import com.kindai.app.data.model.sos.SosResponse
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindTopAppBar
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElderlySosScreen(
    apiService: ApiService,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phase by remember { mutableStateOf<SosPhase>(SosPhase.Confirm) }
    var sosResponse by remember { mutableStateOf<SosResponse?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun sendSos() {
        scope.launch {
            phase = SosPhase.Sending
            val result = safeApiCall { apiService.sendSos(SosRequest()) }
            when (result) {
                is UiState.Success -> {
                    sosResponse = result.data
                    phase = SosPhase.Sent
                }
                is UiState.Error -> {
                    errorMsg = result.message
                    phase = SosPhase.Error
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = { KindTopAppBar(title = "SOS — Tez yordam", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KindColors.Background)
                .padding(horizontal = KindSpacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            when (phase) {
                SosPhase.Confirm -> {
                    // Big warning header
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(KindShapes.full)
                            .background(KindColors.Error.copy(0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocalHospital,
                            null,
                            tint = KindColors.Error,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Yordam kerakmi?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = KindColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "SOS tugmasini bossangiz:\n• Farzandingizga HOZIR xabar ketadi\n• 103 tez yordam raqamiga qo'ng'iroq qilishingiz mumkin",
                        fontSize = 17.sp,
                        color = KindColors.TextSecondary,
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // SOS send button
                    Button(
                        onClick = { sendSos() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = KindShapes.buttonPill,
                        colors = ButtonDefaults.buttonColors(containerColor = KindColors.Error)
                    ) {
                        Icon(Icons.Default.LocalHospital, null, modifier = Modifier.size(30.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("SOS — Yordam yuborish", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Direct 103 button
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:103"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = KindShapes.buttonPill,
                        border = androidx.compose.foundation.BorderStroke(2.dp, KindColors.Error)
                    ) {
                        Icon(Icons.Default.Phone, null, tint = KindColors.Error, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("103 ga qo'ng'iroq", fontSize = 18.sp, color = KindColors.Error, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = onBack) {
                        Text("Hojat yo'q, orqaga qaytish", fontSize = 16.sp, color = KindColors.TextSecondary)
                    }
                }

                SosPhase.Sending -> {
                    Spacer(modifier = Modifier.height(60.dp))
                    CircularProgressIndicator(color = KindColors.Error, modifier = Modifier.size(60.dp), strokeWidth = 4.dp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Xabar yuborilmoqda...", fontSize = 20.sp, color = KindColors.TextPrimary)
                }

                SosPhase.Sent -> {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(KindShapes.full)
                            .background(KindColors.RiskLow.copy(0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = KindColors.RiskLow, modifier = Modifier.size(52.dp))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Xabar yuborildi!",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = KindColors.RiskLow
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        sosResponse?.message ?: "Farzandingiz xabardor qilindi.",
                        fontSize = 17.sp,
                        color = KindColors.TextSecondary,
                        lineHeight = 26.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Still offer 103
                    Button(
                        onClick = {
                            val phone = sosResponse?.emergencyPhone ?: "103"
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = KindShapes.buttonPill,
                        colors = ButtonDefaults.buttonColors(containerColor = KindColors.Error)
                    ) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        val phone = sosResponse?.emergencyPhone ?: "103"
                        Text("$phone ga qo'ng'iroq", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onBack) {
                        Text("Asosiy sahifaga qaytish", fontSize = 16.sp, color = KindColors.TextSecondary)
                    }
                }

                SosPhase.Error -> {
                    Icon(Icons.Default.Warning, null, tint = KindColors.Error, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Xatolik yuz berdi", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = KindColors.Error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        errorMsg ?: "Internet bilan bog'laning",
                        fontSize = 16.sp,
                        color = KindColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    // Offer direct dial even on error
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:103"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        shape = KindShapes.buttonPill,
                        colors = ButtonDefaults.buttonColors(containerColor = KindColors.Error)
                    ) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(26.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("103 ga qo'ng'iroq", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { sendSos() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = KindShapes.buttonPill
                    ) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Qayta urinish", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

private enum class SosPhase { Confirm, Sending, Sent, Error }
