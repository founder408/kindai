package com.kindai.app.ui.screens.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.elderly.ElderlyProfileDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.*
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyProfilesTab(
    apiService: ApiService,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToDashboard: (String) -> Unit
) {
    var profiles by remember { mutableStateOf<List<ElderlyProfileDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = safeApiCall { apiService.getElderlyList() }
        if (result is UiState.Success) profiles = result.data
        isLoading = false
    }

    Scaffold(
        topBar = {
            KindTopAppBar(title = "Oila a'zolari")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPerson,
                containerColor = KindColors.Primary,
                contentColor = KindColors.TextOnPrimary,
                shape = KindShapes.large,
                modifier = Modifier.shadow(
                    elevation = 8.dp,
                    shape = KindShapes.large,
                    ambientColor = KindColors.Primary.copy(alpha = 0.3f),
                    spotColor = KindColors.Primary.copy(alpha = 0.3f)
                )
            ) {
                Icon(Icons.Default.Add, "Qo'shish")
            }
        }
    ) { padding ->
        if (isLoading) {
            KindLoadingState(modifier = Modifier.padding(padding))
        } else if (profiles.isEmpty()) {
            KindEmptyState(
                title = "Oila a'zolari topilmadi",
                subtitle = "Birinchi oila a'zosi profilini yarating va sog'liq monitoringini boshlang",
                icon = Icons.Default.FamilyRestroom,
                actionText = "Profil qo'shish",
                onAction = onNavigateToAddPerson,
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
                items(profiles) { profile ->
                    KindProfileCard(
                        name = profile.fullName,
                        relationship = profile.relationshipToElderly ?: "Oila a'zosi",
                        age = profile.age,
                        onClick = { onNavigateToDashboard(profile.id) }
                    )
                }
            }
        }
    }
}
