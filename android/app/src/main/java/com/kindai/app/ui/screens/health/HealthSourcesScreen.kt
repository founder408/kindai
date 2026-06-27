package com.kindai.app.ui.screens.health

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.ui.components.*
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthSourcesScreen(
    onBack: () -> Unit,
    onNavigateToHealthConnect: () -> Unit
) {
    Scaffold(
        topBar = {
            KindTopAppBar(title = "Salomatlik manbalari", onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(KindColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KindSpacing.screenHorizontal)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header info card
            Card(
                shape = KindShapes.cardLarge,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KindShapes.cardLarge)
                        .background(KindGradients.PrimaryGradient)
                        .padding(KindSpacing.cardPaddingXL)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(KindShapes.large)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DeviceHub,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Salomatlik manbalari",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ko'p manbadan ma'lumot yig'ib, AI tahlilni yaxshilang",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            KindSectionHeader(title = "Mavjud manbalar")
            Spacer(modifier = Modifier.height(12.dp))

            // Manual entry — always available
            KindHealthSourceCard(
                title = "Qo'lda kiritish",
                description = "Qon bosim, puls, harorat va boshqa ko'rsatkichlarni qo'lda kiriting",
                icon = Icons.Default.Edit,
                iconTint = KindColors.Primary,
                iconBg = KindColors.PrimaryLight,
                statusText = "Faol",
                statusColor = KindColors.Success
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Health Connect
            KindHealthSourceCard(
                title = "Android Health Connect",
                description = "Telefon ilovalari (Samsung Health, Google Fit) dan qadamlar, uyqu, yurak urishi ma'lumotlarini ulang",
                icon = Icons.Default.MonitorHeart,
                iconTint = KindColors.Secondary,
                iconBg = KindColors.SecondaryLight,
                statusText = "Ulash mumkin",
                statusColor = KindColors.Primary,
                actionText = "Sozlamalarga o'tish",
                onAction = onNavigateToHealthConnect
            )

            Spacer(modifier = Modifier.height(24.dp))
            KindSectionHeader(title = "Kelajakdagi integratsiyalar")
            Spacer(modifier = Modifier.height(12.dp))

            // Kind Band — coming soon
            KindHealthSourceCard(
                title = "Kind Band",
                description = "Maxsus qurilma: 24/7 yurak urishi, uyqu, SpO2, yiqilish sensori va SOS tugma",
                icon = Icons.Default.Watch,
                iconTint = KindColors.Accent,
                iconBg = KindColors.AccentLight,
                statusText = "Tez orada",
                statusColor = KindColors.Warning
            )

            Spacer(modifier = Modifier.height(12.dp))

            KindHealthSourceCard(
                title = "Tonometr (Bluetooth)",
                description = "Bluetooth tonometr bilan avtomatik qon bosim o'lchash",
                icon = Icons.Default.Favorite,
                iconTint = KindColors.Error,
                iconBg = KindColors.ErrorBg,
                statusText = "Tez orada",
                statusColor = KindColors.Warning
            )

            Spacer(modifier = Modifier.height(12.dp))

            KindHealthSourceCard(
                title = "Glukometr (Bluetooth)",
                description = "Qandli diabet nazorati uchun Bluetooth glukometr",
                icon = Icons.Default.Opacity,
                iconTint = Color(0xFFFF6B35),
                iconBg = Color(0xFFFFF0EB),
                statusText = "Tez orada",
                statusColor = KindColors.Warning
            )

            Spacer(modifier = Modifier.height(24.dp))
            SafetyDisclaimerCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
