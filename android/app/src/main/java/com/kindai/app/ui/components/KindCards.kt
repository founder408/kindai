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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing

@Composable
fun KindMetricCard(
    title: String,
    value: String,
    unit: String = "",
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = KindColors.Primary,
    backgroundColor: Color = KindColors.PrimaryLight
) {
    Card(
        modifier = modifier,
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KindSpacing.cardPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(KindShapes.medium)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = KindColors.TextTertiary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = KindColors.TextPrimary
                )
                if (unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 12.sp,
                        color = KindColors.TextTertiary,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KindOverviewMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = KindShapes.large,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(KindShapes.medium)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = KindColors.TextPrimary
                )
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = KindColors.TextTertiary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun KindRiskCard(
    riskLevel: String,
    summary: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (color, bgColor, label) = when (riskLevel.uppercase()) {
        "LOW" -> Triple(KindColors.RiskLow, KindColors.RiskLowBg, "Past xavf")
        "MODERATE", "MEDIUM" -> Triple(KindColors.RiskMedium, KindColors.RiskMediumBg, "O'rtacha xavf")
        "HIGH" -> Triple(KindColors.RiskHigh, KindColors.RiskHighBg, "Yuqori xavf")
        "EMERGENCY" -> Triple(KindColors.RiskEmergency, KindColors.RiskEmergencyBg, "Favqulodda")
        else -> Triple(KindColors.TextSecondary, KindColors.SurfaceVariant, riskLevel)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(KindSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(KindShapes.full)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (riskLevel.uppercase()) {
                        "EMERGENCY" -> Icons.Default.Warning
                        "HIGH" -> Icons.Default.PriorityHigh
                        else -> Icons.Default.Shield
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = summary,
                    fontSize = 13.sp,
                    color = KindColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun KindAiInsightCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    actionText: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = KindShapes.card,
                ambientColor = KindColors.AiPurple.copy(alpha = 0.15f),
                spotColor = KindColors.AiPurple.copy(alpha = 0.15f)
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KindShapes.card)
                .background(KindGradients.AiGradient)
                .padding(KindSpacing.cardPaddingXL)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(KindShapes.medium)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.92f),
                    lineHeight = 21.sp
                )
                if (actionText != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .clip(KindShapes.buttonPill)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = actionText,
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KindProfileCard(
    name: String,
    relationship: String,
    age: Int?,
    riskLevel: String? = null,
    unreadAlerts: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KindSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(KindShapes.full)
                    .background(KindColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = KindColors.Primary
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = KindColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KindStatusChip(
                        text = relationship,
                        color = KindColors.Secondary,
                        backgroundColor = KindColors.SecondaryLight
                    )
                    if (age != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$age yosh",
                            fontSize = 12.sp,
                            color = KindColors.TextTertiary
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (riskLevel != null) {
                    val riskColor = when (riskLevel.uppercase()) {
                        "LOW" -> KindColors.RiskLow
                        "MODERATE", "MEDIUM" -> KindColors.RiskMedium
                        "HIGH" -> KindColors.RiskHigh
                        "EMERGENCY" -> KindColors.RiskEmergency
                        else -> KindColors.TextTertiary
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(KindShapes.full)
                            .background(riskColor)
                    )
                }
                if (unreadAlerts > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Badge(containerColor = KindColors.Error) {
                        Text(text = "$unreadAlerts", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = KindColors.TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun KindAlertCard(
    title: String,
    message: String,
    severity: String,
    profileName: String = "",
    time: String = "",
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val sevColor = when (severity.uppercase()) {
        "EMERGENCY" -> KindColors.RiskEmergency
        "HIGH" -> KindColors.RiskHigh
        "MEDIUM" -> KindColors.RiskMedium
        else -> KindColors.RiskLow
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(KindSpacing.cardPadding)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(KindShapes.full)
                    .background(sevColor)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = KindColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (time.isNotBlank()) {
                        Text(
                            text = time,
                            fontSize = 11.sp,
                            color = KindColors.TextTertiary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = KindColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                if (profileName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = KindColors.Primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = profileName,
                            fontSize = 12.sp,
                            color = KindColors.Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KindMedicationCard(
    name: String,
    dosage: String?,
    time: String,
    instruction: String? = null,
    isTaken: Boolean = false,
    onTake: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = KindShapes.card,
        colors = CardDefaults.cardColors(
            containerColor = if (isTaken) KindColors.SuccessBg else KindColors.Surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTaken) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(KindSpacing.cardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(KindShapes.medium)
                    .background(
                        if (isTaken) KindColors.Success.copy(alpha = 0.1f)
                        else KindColors.PrimaryLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isTaken) Icons.Default.CheckCircle else Icons.Default.Medication,
                    contentDescription = null,
                    tint = if (isTaken) KindColors.Success else KindColors.Primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = KindColors.TextPrimary
                )
                if (!dosage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dosage,
                        fontSize = 13.sp,
                        color = KindColors.TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = KindColors.TextTertiary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = time,
                        fontSize = 12.sp,
                        color = KindColors.TextTertiary
                    )
                }
            }
            if (onTake != null && !isTaken) {
                FilledTonalButton(
                    onClick = onTake,
                    shape = KindShapes.buttonPill,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = KindColors.Primary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Ichdim", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (isTaken) {
                KindStatusChip(
                    text = "Qabul qilindi",
                    color = KindColors.Success,
                    backgroundColor = KindColors.Success.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun KindAlertBanner(
    message: String,
    isUrgent: Boolean,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val bgColor = if (isUrgent) KindColors.RiskHighBg else KindColors.SuccessBg
    val textColor = if (isUrgent) KindColors.RiskHigh else KindColors.Success
    val icon = if (isUrgent) Icons.Default.Warning else Icons.Default.CheckCircle

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = KindShapes.large,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                fontSize = 13.sp,
                color = textColor,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                lineHeight = 18.sp
            )
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onAction) {
                    Text(
                        text = actionText,
                        fontSize = 13.sp,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
