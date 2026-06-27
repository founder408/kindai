package com.kindai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing

@Composable
fun KindHealthSourceCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    statusText: String,
    statusColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(KindSpacing.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(KindShapes.large)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = KindColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = KindColors.TextSecondary,
                        lineHeight = 16.sp
                    )
                }
                KindStatusChip(
                    text = statusText,
                    color = statusColor,
                    backgroundColor = statusColor.copy(alpha = 0.1f)
                )
            }
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = KindColors.Divider)
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = onAction,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = actionText,
                        fontSize = 13.sp,
                        color = KindColors.Primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KindDeviceCard(
    name: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    badge: String,
    badgeColor: Color,
    features: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = KindShapes.cardLarge,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(KindSpacing.cardPaddingXL)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(KindShapes.xl)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = KindColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    KindStatusChip(
                        text = badge,
                        color = badgeColor,
                        backgroundColor = badgeColor.copy(alpha = 0.1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                color = KindColors.TextSecondary,
                lineHeight = 20.sp
            )
            if (features.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = KindColors.Divider)
                Spacer(modifier = Modifier.height(12.dp))
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 3.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = feature,
                            fontSize = 13.sp,
                            color = KindColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}
