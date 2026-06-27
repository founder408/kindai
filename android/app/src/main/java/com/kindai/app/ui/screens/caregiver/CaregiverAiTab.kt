package com.kindai.app.ui.screens.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.elderly.ElderlyProfileDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.*
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverAiTab(
    apiService: ApiService,
    onNavigateToDashboard: (String) -> Unit
) {
    var profiles by remember { mutableStateOf<List<ElderlyProfileDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val result = safeApiCall { apiService.getElderlyList() }
        if (result is UiState.Success) profiles = result.data
        isLoading = false
    }

    Scaffold(
        topBar = { KindTopAppBar(title = "AI sog'liq yordamchisi") }
    ) { padding ->
        if (isLoading) {
            KindLoadingState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(KindColors.Background)
                    .padding(horizontal = KindSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                item {
                    KindAiInsightCard(
                        title = "AI oilaviy sog'liq yordamchisi",
                        message = "Oila a'zolari sog'liq holatini tahlil qilish va xavfni erta aniqlash uchun profilni tanlang.",
                        actionText = if (profiles.isNotEmpty()) "Tahlil boshlash" else null,
                        onClick = {
                            profiles.firstOrNull()?.let { onNavigateToDashboard(it.id) }
                        }
                    )
                }

                item {
                    KindSectionHeader(title = "Profil tanlang")
                }

                if (profiles.isEmpty()) {
                    item {
                        KindEmptyState(
                            title = "Profil topilmadi",
                            subtitle = "Avval oila a'zosi profilini yarating, keyin AI tahlil qilishingiz mumkin",
                            icon = Icons.Default.Psychology,
                            modifier = Modifier.height(220.dp)
                        )
                    }
                } else {
                    items(profiles) { profile ->
                        KindProfileCard(
                            name = profile.fullName,
                            relationship = profile.relationshipToElderly ?: "Oila a'zosi",
                            age = profile.age,
                            onClick = { onNavigateToDashboard(profile.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SafetyDisclaimerCard()
                }
            }
        }
    }
}
