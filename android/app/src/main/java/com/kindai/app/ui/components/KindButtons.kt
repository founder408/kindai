package com.kindai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing

@Composable
fun KindPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(KindSpacing.buttonHeight)
            .shadow(
                elevation = if (enabled && !isLoading) 6.dp else 0.dp,
                shape = KindShapes.button,
                ambientColor = KindColors.Primary.copy(alpha = 0.3f),
                spotColor = KindColors.Primary.copy(alpha = 0.3f)
            )
            .clip(KindShapes.button)
            .background(
                if (enabled && !isLoading) KindGradients.PrimaryHorizontal
                else Brush.linearGradient(listOf(KindColors.Border, KindColors.Border))
            )
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            if (icon != null && !isLoading) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun KindSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(KindSpacing.buttonHeight),
        enabled = enabled,
        shape = KindShapes.button,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = KindColors.Primary
        )
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun KindDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(KindSpacing.buttonHeight)
            .shadow(
                elevation = if (enabled && !isLoading) 4.dp else 0.dp,
                shape = KindShapes.button,
                ambientColor = KindColors.Error.copy(alpha = 0.3f),
                spotColor = KindColors.Error.copy(alpha = 0.3f)
            )
            .clip(KindShapes.button)
            .background(
                if (enabled && !isLoading) KindGradients.DangerGradient
                else Brush.linearGradient(listOf(KindColors.Border, KindColors.Border))
            )
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun KindIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = KindColors.Primary,
    backgroundColor: Color = KindColors.PrimaryLight
) {
    Column(
        modifier = modifier
            .clip(KindShapes.medium)
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(KindSpacing.quickActionSize)
                .clip(KindShapes.quickAction)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = KindColors.TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun KindQuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = KindColors.Primary,
    backgroundColor: Color = KindColors.PrimaryLight
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = KindShapes.large,
        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(KindShapes.medium)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = KindColors.TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}
