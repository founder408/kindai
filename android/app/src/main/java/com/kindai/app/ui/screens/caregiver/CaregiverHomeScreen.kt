package com.kindai.app.ui.screens.caregiver

import androidx.compose.runtime.Composable
import com.kindai.app.data.local.TokenManager
import com.kindai.app.data.remote.ApiService

@Composable
fun CaregiverHomeScreen(
    apiService: ApiService,
    tokenManager: TokenManager,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onSessionExpired: () -> Unit,
    onNavigateToHealthSources: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {}
) {
    CaregiverMainScreen(
        apiService = apiService,
        tokenManager = tokenManager,
        onNavigateToAddPerson = onNavigateToAddPerson,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToSettings = onNavigateToSettings,
        onSessionExpired = onSessionExpired,
        onNavigateToHealthSources = onNavigateToHealthSources,
        onNavigateToDevices = onNavigateToDevices
    )
}
