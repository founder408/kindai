package com.kindai.app.ui.screens.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.kindai.app.data.local.TokenManager
import com.kindai.app.data.model.auth.UserDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.*
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTab(
    apiService: ApiService,
    tokenManager: TokenManager,
    onSessionExpired: () -> Unit
) {
    var user by remember { mutableStateOf<UserDto?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = safeApiCall { apiService.getMe() }
        if (result is UiState.Success) user = result.data
    }

    Scaffold(
        topBar = { KindTopAppBar(title = "Sozlamalar") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KindColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(KindSpacing.screenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // User info card - premium style
            Card(
                shape = KindShapes.cardLarge,
                colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(KindSpacing.cardPaddingXL),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(KindShapes.full)
                            .background(KindColors.PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.fullName?.take(1) ?: "U").uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = KindColors.Primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user?.fullName ?: "Yuklanmoqda...",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = KindColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = user?.email ?: user?.phone ?: "",
                            fontSize = 13.sp,
                            color = KindColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        KindStatusChip(
                            text = when (user?.role) {
                                "CHILD" -> "Qarovchi"
                                "ELDERLY" -> "Kattalar"
                                "DOCTOR" -> "Shifokor"
                                else -> "Foydalanuvchi"
                            },
                            color = KindColors.Primary,
                            backgroundColor = KindColors.PrimaryLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Settings sections
            KindSectionHeader(title = "Ilova")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = KindShapes.card,
                colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Maxfiylik va rozilik",
                        onClick = {}
                    )
                    HorizontalDivider(
                        color = KindColors.Divider,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "Ilova haqida",
                        onClick = {}
                    )
                    HorizontalDivider(
                        color = KindColors.Divider,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    SettingsItem(
                        icon = Icons.Default.Help,
                        title = "Yordam",
                        onClick = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            KindDangerButton(
                text = "Chiqish",
                onClick = {
                    scope.launch {
                        tokenManager.clearToken()
                        onSessionExpired()
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Kind AI v1.0.0",
                fontSize = 12.sp,
                color = KindColors.TextTertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(KindShapes.medium)
                .background(KindColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KindColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            color = KindColors.TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = KindColors.TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}
