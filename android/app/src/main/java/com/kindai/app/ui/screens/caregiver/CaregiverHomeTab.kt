package com.kindai.app.ui.screens.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.local.TokenManager
import com.kindai.app.data.model.elderly.ElderlyProfileDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.*
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@Composable
fun CaregiverHomeTab(
    apiService: ApiService,
    tokenManager: TokenManager,
    onNavigateToAddPerson: () -> Unit,
    onNavigateToDashboard: (String) -> Unit,
    onSessionExpired: () -> Unit,
    onNavigateToHealthSources: () -> Unit = {},
    onNavigateToDevices: () -> Unit = {}
) {
    var profiles by remember { mutableStateOf<List<ElderlyProfileDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val meResult = safeApiCall { apiService.getMe() }
        if (meResult is UiState.Success) {
            userName = meResult.data.fullName
        } else if (meResult is UiState.Error && meResult.message == "SESSION_EXPIRED") {
            tokenManager.clearToken()
            onSessionExpired()
            return@LaunchedEffect
        }

        val result = safeApiCall { apiService.getElderlyList() }
        if (result is UiState.Success) profiles = result.data
        isLoading = false
    }

    if (isLoading) {
        KindLoadingState()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(KindColors.Background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Premium gradient header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KindGradients.HeaderGradientLarge)
                    .padding(horizontal = KindSpacing.screenHorizontal)
                    .padding(top = 52.dp, bottom = 28.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Assalomu alaykum,",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userName.ifBlank { "Foydalanuvchi" },
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Bugun oilangiz sog'ligi nazoratda",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(KindShapes.full)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Overview metrics row - glass morphism style
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HeaderMetricChip(
                            value = "${profiles.size}",
                            label = "Profillar",
                            icon = Icons.Default.People,
                            modifier = Modifier.weight(1f)
                        )
                        HeaderMetricChip(
                            value = "${profiles.size}",
                            label = "Kuzatuvda",
                            icon = Icons.Default.Visibility,
                            modifier = Modifier.weight(1f)
                        )
                        HeaderMetricChip(
                            value = "AI",
                            label = "Tayyor",
                            icon = Icons.Default.AutoAwesome,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Alert banner
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = KindSpacing.screenHorizontal)) {
                KindAlertBanner(
                    message = if (profiles.isEmpty())
                        "Profil qo'shing va sog'liq monitoringini boshlang"
                    else
                        "Hozircha jiddiy xavf aniqlanmadi",
                    isUrgent = false
                )
            }
        }

        // AI Insight card
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Column(modifier = Modifier.padding(horizontal = KindSpacing.screenHorizontal)) {
                KindAiInsightCard(
                    title = "AI sog'liq xulosasi",
                    message = if (profiles.isEmpty()) {
                        "AI tahlil uchun oila a'zosi profilini qo'shing va sog'liq ma'lumotlarini kiriting."
                    } else {
                        "${profiles.size} ta oila a'zosi kuzatuvda. Sog'liq ma'lumotlarini kiritib, AI tahlilni boshlang."
                    },
                    actionText = "AI tahlil",
                    onClick = {
                        profiles.firstOrNull()?.let { onNavigateToDashboard(it.id) }
                    }
                )
            }
        }

        // Quick actions section
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = KindSpacing.screenHorizontal)) {
                KindSectionHeader(title = "Tezkor amallar")
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KindQuickActionCard(
                        icon = Icons.Default.PersonAdd,
                        label = "Profil qo'shish",
                        onClick = onNavigateToAddPerson,
                        tint = KindColors.Primary,
                        backgroundColor = KindColors.PrimaryLight,
                        modifier = Modifier.weight(1f)
                    )
                    KindQuickActionCard(
                        icon = Icons.Default.Favorite,
                        label = "Sog'lik kiritish",
                        onClick = {
                            profiles.firstOrNull()?.let { onNavigateToDashboard(it.id) }
                        },
                        tint = KindColors.Secondary,
                        backgroundColor = KindColors.SecondaryLight,
                        modifier = Modifier.weight(1f)
                    )
                    KindQuickActionCard(
                        icon = Icons.Default.Psychology,
                        label = "AI tahlil",
                        onClick = {
                            profiles.firstOrNull()?.let { onNavigateToDashboard(it.id) }
                        },
                        tint = KindColors.AiPurple,
                        backgroundColor = KindColors.AiPurpleLight,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KindQuickActionCard(
                        icon = Icons.Default.DeviceHub,
                        label = "Manbalar",
                        onClick = onNavigateToHealthSources,
                        tint = KindColors.Accent,
                        backgroundColor = KindColors.AccentLight,
                        modifier = Modifier.weight(1f)
                    )
                    KindQuickActionCard(
                        icon = Icons.Default.Watch,
                        label = "Qurilmalar",
                        onClick = onNavigateToDevices,
                        tint = Color(0xFF8B5CF6),
                        backgroundColor = Color(0xFFF5F3FF),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Family members section
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Column(modifier = Modifier.padding(horizontal = KindSpacing.screenHorizontal)) {
                KindSectionHeader(
                    title = "Oila a'zolari",
                    actionText = if (profiles.isNotEmpty()) "Barchasi" else null,
                    onAction = {}
                )
            }
        }

        if (profiles.isEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = KindSpacing.screenHorizontal)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = KindShapes.cardLarge,
                        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(KindShapes.full)
                                    .background(KindColors.PrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.FamilyRestroom,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = KindColors.Primary
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Hali profil qo'shilmagan",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = KindColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Ota-onangiz, farzandingiz yoki o'zingiz uchun\nsog'liq profilini yarating.",
                                fontSize = 14.sp,
                                color = KindColors.TextSecondary,
                                lineHeight = 20.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            KindPrimaryButton(
                                text = "Profil qo'shish",
                                icon = Icons.Default.Add,
                                onClick = onNavigateToAddPerson,
                                modifier = Modifier.width(220.dp)
                            )
                        }
                    }
                }
            }
        } else {
            items(profiles) { profile ->
                Column(modifier = Modifier.padding(horizontal = KindSpacing.screenHorizontal)) {
                    Spacer(modifier = Modifier.height(10.dp))
                    KindProfileCard(
                        name = profile.fullName,
                        relationship = profile.relationshipToElderly ?: "Oila a'zosi",
                        age = profile.age,
                        onClick = { onNavigateToDashboard(profile.id) }
                    )
                }
            }
        }

        // Safety disclaimer
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = KindSpacing.screenHorizontal)) {
                SafetyDisclaimerCard()
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeaderMetricChip(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = KindShapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
