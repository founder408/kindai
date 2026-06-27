package com.kindai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing

@Composable
fun SafetyDisclaimerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    .size(32.dp)
                    .clip(KindShapes.full)
                    .background(KindColors.Warning.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = KindColors.Warning,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Muhim eslatma",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = KindColors.Warning
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bu tibbiy diagnoz emas. AI tavsiyalari faqat ma'lumot uchun. Zarur holatda shifokorga murojaat qiling.",
                    fontSize = 12.sp,
                    color = KindColors.TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun EmergencyDisclaimerCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = KindShapes.large,
        colors = CardDefaults.cardColors(containerColor = KindColors.ErrorBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(KindSpacing.cardPadding),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(KindShapes.full)
                    .background(KindColors.Error.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = KindColors.Error,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Favqulodda holat",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = KindColors.Error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Favqulodda holatda ilovaga tayanib qolmang, darhol tez yordamga murojaat qiling.",
                    fontSize = 12.sp,
                    color = KindColors.TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
