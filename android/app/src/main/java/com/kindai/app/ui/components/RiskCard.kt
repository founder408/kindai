package com.kindai.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.ui.theme.*

@Composable
fun RiskCard(
    riskLevel: String,
    summary: String?,
    recommendation: String?,
    modifier: Modifier = Modifier,
    simpleMode: Boolean = false
) {
    val (color, bgColor, label) = when (riskLevel.uppercase()) {
        "LOW" -> Triple(RiskLow, KindGreenLight, if (simpleMode) "Hozircha holat yaxshi ✓" else "PAST XAVF")
        "MEDIUM" -> Triple(RiskMedium, KindOrangeLight, if (simpleMode) "E'tibor kerak ⚠" else "O'RTA XAVF")
        "HIGH" -> Triple(RiskHigh, KindRedLight, if (simpleMode) "Farzandingizga xabar bering!" else "YUQORI XAVF")
        "EMERGENCY" -> Triple(RiskEmergency, KindRedLight, if (simpleMode) "Zudlik bilan yordam kerak!" else "FAVQULODDA")
        else -> Triple(KindGray, KindBackground, riskLevel)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = if (simpleMode) 22.sp else 16.sp
            )
            if (summary != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = if (simpleMode) 18.sp else 14.sp
                )
            }
            if (recommendation != null && !simpleMode) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = KindGray
                )
            }
        }
    }
}
