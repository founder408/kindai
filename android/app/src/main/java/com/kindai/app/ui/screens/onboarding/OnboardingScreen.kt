package com.kindai.app.ui.screens.onboarding

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.data.local.OnboardingManager
import com.kindai.app.ui.components.KindPrimaryButton
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onboardingManager: OnboardingManager,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KindColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KindSpacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(KindShapes.xxl)
                .background(KindGradients.PrimaryGradient),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Kind AI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = KindColors.Primary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Oilaviy sog'liq monitoringi va AI yordamchi",
            fontSize = 14.sp,
            color = KindColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Feature cards
        OnboardingFeature(
            icon = Icons.Default.FamilyRestroom,
            title = "Oilaviy sog'liq nazorati",
            description = "Ota-ona, farzand va barcha oila a'zolari sog'lig'ini bir joydan kuzating.",
            accentColor = KindColors.Primary,
            bgColor = KindColors.PrimaryLight
        )
        Spacer(modifier = Modifier.height(14.dp))
        OnboardingFeature(
            icon = Icons.Default.Psychology,
            title = "AI xavfni erta sezadi",
            description = "Sun'iy intellekt sog'liq ko'rsatkichlarini tahlil qilib, xavfni oldindan aniqlaydi.",
            accentColor = KindColors.AiPurple,
            bgColor = KindColors.AiPurpleLight
        )
        Spacer(modifier = Modifier.height(14.dp))
        OnboardingFeature(
            icon = Icons.Default.Security,
            title = "Maxfiylik va rozilik muhim",
            description = "Boshqa odamni kuzatish uchun rozilik yoki qonuniy asosga ega bo'lishingiz kerak.",
            accentColor = KindColors.Secondary,
            bgColor = KindColors.SecondaryLight
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Medical disclaimer
        Card(
            shape = KindShapes.large,
            colors = CardDefaults.cardColors(containerColor = KindColors.WarningBg),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(KindSpacing.cardPadding),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(KindShapes.full)
                        .background(KindColors.Warning.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = KindColors.Warning,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Bu tibbiy diagnoz emas. AI faqat ma'lumot maqsadida ishlaydi. Favqulodda holatlarda tez yordamga murojaat qiling.",
                    fontSize = 13.sp,
                    color = KindColors.TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        KindPrimaryButton(
            text = "Boshlash",
            icon = Icons.Default.ArrowForward,
            onClick = {
                scope.launch {
                    onboardingManager.setOnboardingDone()
                    onDone()
                }
            }
        )

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun OnboardingFeature(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color,
    bgColor: Color
) {
    Card(
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(KindSpacing.cardPadding),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(KindShapes.large)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = KindColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = KindColors.TextSecondary,
                    lineHeight = 19.sp
                )
            }
        }
    }
}
