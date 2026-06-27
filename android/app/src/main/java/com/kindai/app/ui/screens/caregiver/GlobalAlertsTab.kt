package com.kindai.app.ui.screens.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.alert.AlertDto
import com.kindai.app.data.model.elderly.ElderlyProfileDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.*
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalAlertsTab(
    apiService: ApiService,
    onNavigateToDashboard: (String) -> Unit
) {
    var alerts by remember { mutableStateOf<List<AlertDto>>(emptyList()) }
    var profiles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val profileResult = safeApiCall { apiService.getElderlyList() }
        val profileList = if (profileResult is UiState.Success) profileResult.data else emptyList()
        profiles = profileList.associate { it.id to it.fullName }

        val allAlerts = mutableListOf<AlertDto>()
        for (p in profileList) {
            val result = safeApiCall { apiService.getElderlyAlerts(p.id) }
            if (result is UiState.Success) allAlerts.addAll(result.data)
        }
        alerts = allAlerts.sortedByDescending { it.createdAt }
        isLoading = false
    }

    Scaffold(
        topBar = { KindTopAppBar(title = "Barcha ogohlantirishlar") }
    ) { padding ->
        if (isLoading) {
            KindLoadingState(modifier = Modifier.padding(padding))
        } else if (alerts.isEmpty()) {
            KindEmptyState(
                title = "Ogohlantirishlar yo'q",
                subtitle = "Hozircha barcha ko'rsatkichlar me'yorda. Sog'liq monitoringi davom etmoqda.",
                icon = Icons.Default.NotificationsOff,
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(KindColors.Background)
                    .padding(horizontal = KindSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(alerts) { alert ->
                    KindAlertCard(
                        title = alert.title,
                        message = alert.message,
                        severity = alert.severity,
                        profileName = profiles[alert.elderlyId] ?: "",
                        time = alert.createdAt.take(16).replace("T", " "),
                        onClick = { onNavigateToDashboard(alert.elderlyId) }
                    )
                }
            }
        }
    }
}
