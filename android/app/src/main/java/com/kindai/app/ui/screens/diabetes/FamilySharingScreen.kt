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
import com.kindai.app.data.model.diabetes.DiabetesProfileDto
import com.kindai.app.data.model.diabetes.DiabetesProfileUpdateRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindPrimaryButton
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilySharingScreen(
    apiService: ApiService,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<DiabetesProfileDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var caregiverEmail by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showRevokeDialog by remember { mutableStateOf(false) }

    fun load() {
        scope.launch {
            isLoading = true
            val result = safeApiCall { apiService.getDiabetesProfile() }
            if (result is UiState.Success) {
                profile = result.data
                caregiverEmail = result.data.caregiverUserId ?: ""
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { load() }

    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            icon = { Icon(Icons.Default.PersonRemove, null, tint = KindColors.Error) },
            title = { Text("Kirishni bekor qilish", fontWeight = FontWeight.Bold) },
            text = { Text("Hozirgi g'amxo'r/yaqin kishining kirishini bekor qilib, ularning ma'lumotlaringizga kirishi to'xtatiladi. Davom etasizmi?") },
            confirmButton = {
                Button(
                    onClick = {
                        showRevokeDialog = false
                        scope.launch {
                            isSaving = true
                            errorMsg = null
                            successMsg = null
                            val result = safeApiCall {
                                apiService.updateMyDiabetesProfile(
                                    DiabetesProfileUpdateRequest(caregiverUserId = null, consentConfirmed = false)
                                )
                            }
                            isSaving = false
                            when (result) {
                                is UiState.Success -> { profile = result.data; successMsg = "Kirish bekor qilindi" }
                                is UiState.Error -> errorMsg = result.message
                                else -> {}
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KindColors.Error),
                    shape = KindShapes.buttonPill
                ) { Text("Bekor qilish") }
            },
            dismissButton = { TextButton(onClick = { showRevokeDialog = false }) { Text("Ortga") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Oila ulashish", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = KindColors.Surface)
            )
        }
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(40.dp))
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(KindColors.Background)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(20.dp))

                // Info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.xl,
                    colors = CardDefaults.cardColors(containerColor = KindColors.PrimaryLight),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FamilyRestroom, null, tint = KindColors.Primary, modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Yaqin kishi bilan ulashish", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.Primary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Yaqin kishingiz (g'amxo'r) sizning qand ko'rsatkichlaringizni, dori jadvalingizni va ogohlantirishlarni ko'ra oladi. Faqat siz ruxsat bergan kishi ko'ra oladi.",
                            fontSize = 13.sp, color = KindColors.TextSecondary, lineHeight = 18.sp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Current caregiver status
                profile?.let { p ->
                    if (p.caregiverUserId != null && p.consentConfirmed == true) {
                        // Active caregiver
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = KindShapes.xl,
                            colors = CardDefaults.cardColors(containerColor = KindColors.RiskLowBg),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(44.dp).clip(KindShapes.full).background(KindColors.RiskLow.copy(0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) { Icon(Icons.Default.Person, null, tint = KindColors.RiskLow, modifier = Modifier.size(22.dp)) }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Aktiv g'amxo'r", fontSize = 12.sp, color = KindColors.TextTertiary)
                                        Text("ID: ${p.caregiverUserId ?: "-"}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextPrimary)
                                    }
                                    Box(
                                        modifier = Modifier.clip(KindShapes.chip).background(KindColors.RiskLow.copy(0.15f)).padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) { Text("Aktiv", fontSize = 11.sp, color = KindColors.RiskLow, fontWeight = FontWeight.Bold) }
                                }
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { showRevokeDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = KindShapes.buttonPill,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = KindColors.Error),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, KindColors.Error)
                                ) {
                                    Icon(Icons.Default.PersonRemove, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Kirishni bekor qilish", fontSize = 13.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }

                // Add caregiver section
                Text("G'amxo'r qo'shish", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
                Spacer(Modifier.height(10.dp))
                Text("G'amxo'ringizning Kind AI foydalanuvchi ID raqamini kiriting. Ular allaqachon Kind AI'da ro'yxatdan o'tgan bo'lishi kerak.", fontSize = 13.sp, color = KindColors.TextSecondary, lineHeight = 18.sp)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = caregiverEmail,
                    onValueChange = { caregiverEmail = it },
                    label = { Text("Foydalanuvchi ID") },
                    leadingIcon = { Icon(Icons.Default.PersonSearch, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.large,
                    singleLine = true,
                    placeholder = { Text("Masalan: 42") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                )

                successMsg?.let {
                    Spacer(Modifier.height(10.dp))
                    Card(Modifier.fillMaxWidth(), shape = KindShapes.large, colors = CardDefaults.cardColors(containerColor = KindColors.RiskLowBg)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = KindColors.RiskLow, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(it, fontSize = 13.sp, color = KindColors.RiskLow)
                        }
                    }
                }

                errorMsg?.let {
                    Spacer(Modifier.height(10.dp))
                    Card(Modifier.fillMaxWidth(), shape = KindShapes.large, colors = CardDefaults.cardColors(containerColor = KindColors.ErrorBg)) {
                        Text(it, modifier = Modifier.padding(12.dp), color = KindColors.Error, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(20.dp))

                KindPrimaryButton(
                    text = if (isSaving) "Saqlanmoqda..." else "G'amxo'r qo'shish",
                    enabled = !isSaving && caregiverEmail.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val caregiverId = caregiverEmail.trim().toLongOrNull()
                        if (caregiverId == null) { errorMsg = "Foydalanuvchi ID son bo'lishi kerak"; return@KindPrimaryButton }
                        scope.launch {
                            isSaving = true
                            errorMsg = null
                            successMsg = null
                            val result = safeApiCall {
                                apiService.updateMyDiabetesProfile(
                                    DiabetesProfileUpdateRequest(caregiverUserId = caregiverId, consentConfirmed = true)
                                )
                            }
                            isSaving = false
                            when (result) {
                                is UiState.Success -> { profile = result.data; successMsg = "G'amxo'r muvaffaqiyatli qo'shildi" }
                                is UiState.Error -> errorMsg = result.message
                                else -> {}
                            }
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                // Privacy notice
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KindShapes.xl,
                    colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = KindColors.TextTertiary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Maxfiylik", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = KindColors.TextSecondary)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Faqat siz ruxsat bergan kishi ma'lumotlaringizni ko'ra oladi. Ruxsatni istalgan vaqtda bekor qilishingiz mumkin.",
                            fontSize = 12.sp, color = KindColors.TextTertiary, lineHeight = 17.sp
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
