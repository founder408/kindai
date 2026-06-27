package com.kindai.app.ui.screens.elderlysimple

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.ai.AiAssessmentDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.*
import com.kindai.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ElderlySimpleHomeScreen(
    elderlyId: String,
    apiService: ApiService,
    onBack: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToMedications: () -> Unit,
    onNavigateToAi: () -> Unit
) {
    var latestAssessment by remember { mutableStateOf<AiAssessmentDto?>(null) }
    var showSosDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = safeApiCall { apiService.getLatestAiAssessment(elderlyId) }
        if (result is UiState.Success) {
            latestAssessment = result.data
        }
    }

    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            title = { Text("SOS Yordam", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Favqulodda holat yuzaga keldimi?\n\n" +
                    "Iltimos tez yordam xizmatini chaqiring: 103\n\n" +
                    "Yoki yaqinlaringizga qo'ng'iroq qiling.",
                    fontSize = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSosDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = KindRedDark)
                ) {
                    Text("Tushundim")
                }
            }
        )
    }

    if (showMessageDialog) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("Farzandingizga xabar") },
            text = { Text("Farzandingiz Kind AI orqali sizning holatingizni kuzatib turadi. Agar biror muammo bo'lsa, ular ogohlantiriladi.", fontSize = 16.sp) },
            confirmButton = {
                Button(onClick = { showMessageDialog = false }) {
                    Text("Yaxshi")
                }
            }
        )
    }

    Scaffold(
        topBar = { KindAiTopBar(title = "Oddiy rejim", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Bugun ahvolingiz qanday?",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Latest AI status
            if (latestAssessment != null) {
                RiskCard(
                    riskLevel = latestAssessment!!.riskLevel,
                    summary = null,
                    recommendation = null,
                    simpleMode = true
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Big buttons
            ElderlyBigButton(
                text = "Dorini ichdim",
                icon = Icons.Default.Medication,
                onClick = onNavigateToMedications,
                containerColor = KindGreen
            )

            Spacer(modifier = Modifier.height(16.dp))

            ElderlyBigButton(
                text = "Sog'lig'imni kiritish",
                icon = Icons.Default.FavoriteBorder,
                onClick = onNavigateToHealth,
                containerColor = KindBlue
            )

            Spacer(modifier = Modifier.height(16.dp))

            EmergencyButton(onClick = { showSosDialog = true })

            Spacer(modifier = Modifier.height(16.dp))

            ElderlyBigButton(
                text = "Farzandimga xabar",
                icon = Icons.Default.Message,
                onClick = { showMessageDialog = true },
                containerColor = KindOrange
            )

            Spacer(modifier = Modifier.height(24.dp))
            SafetyDisclaimerCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
