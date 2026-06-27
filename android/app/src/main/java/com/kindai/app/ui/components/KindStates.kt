package com.kindai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindShapes

@Composable
fun KindLoadingState(
    message: String = "Yuklanmoqda...",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = KindColors.Primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp).alpha(alpha)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = KindColors.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun KindEmptyState(
    title: String,
    subtitle: String = "",
    icon: ImageVector = Icons.Default.Inbox,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(KindColors.SurfaceVariant, KindShapes.full),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = KindColors.TextTertiary
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = KindColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = KindColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(20.dp))
                KindSecondaryButton(text = actionText, onClick = onAction, modifier = Modifier.width(200.dp))
            }
        }
    }
}

@Composable
fun KindErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(KindColors.ErrorBg, KindShapes.full),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = KindColors.Error
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Xatolik yuz berdi",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = KindColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = KindColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(20.dp))
                KindSecondaryButton(text = "Qayta urinish", onClick = onRetry, modifier = Modifier.width(200.dp))
            }
        }
    }
}
