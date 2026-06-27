package com.kindai.app.ui.screens.caregiver

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kindai.app.data.local.TokenManager
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.CaregiverTab
import com.kindai.app.ui.components.KindBottomNavigation

@Composable
fun CaregiverMainScreen(
    apiService: ApiService,
    tokenManager: TokenManager,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToDashboard: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onSessionExpired: () -> Unit,
    onNavigateToHealthSources: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(CaregiverTab.HOME) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                CaregiverTab.HOME -> CaregiverHomeTab(
                    apiService = apiService,
                    tokenManager = tokenManager,
                    onNavigateToAddPerson = onNavigateToAddPerson,
                    onNavigateToDashboard = onNavigateToDashboard,
                    onSessionExpired = onSessionExpired,
                    onNavigateToHealthSources = onNavigateToHealthSources,
                    onNavigateToDevices = onNavigateToDevices
                )
                CaregiverTab.FAMILY -> FamilyProfilesTab(
                    apiService = apiService,
                    onNavigateToAddPerson = onNavigateToAddPerson,
                    onNavigateToDashboard = onNavigateToDashboard
                )
                CaregiverTab.AI -> CaregiverAiTab(
                    apiService = apiService,
                    onNavigateToDashboard = onNavigateToDashboard
                )
                CaregiverTab.ALERTS -> GlobalAlertsTab(
                    apiService = apiService,
                    onNavigateToDashboard = onNavigateToDashboard
                )
                CaregiverTab.SETTINGS -> SettingsTab(
                    apiService = apiService,
                    tokenManager = tokenManager,
                    onSessionExpired = onSessionExpired
                )
            }
        }
        KindBottomNavigation(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}
