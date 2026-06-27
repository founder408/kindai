package com.kindai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing

enum class CaregiverTab(
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    HOME("Bosh sahifa", Icons.Filled.Home, Icons.Outlined.Home),
    FAMILY("Oila", Icons.Filled.People, Icons.Outlined.People),
    AI("AI", Icons.Filled.Psychology, Icons.Outlined.Psychology),
    ALERTS("Xabarlar", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    SETTINGS("Sozlamalar", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun KindBottomNavigation(
    selectedTab: CaregiverTab,
    onTabSelected: (CaregiverTab) -> Unit,
    alertCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = KindShapes.topOnly,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            ),
        color = KindColors.NavBackground,
        shape = KindShapes.topOnly
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CaregiverTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val iconColor = if (isSelected) KindColors.Primary else KindColors.NavInactive
                val textColor = if (isSelected) KindColors.Primary else KindColors.NavInactive

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onTabSelected(tab) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(52.dp)
                            .clip(KindShapes.buttonPill)
                            .background(
                                if (isSelected) KindColors.PrimaryLight else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        BadgedBox(
                            badge = {
                                if (tab == CaregiverTab.ALERTS && alertCount > 0) {
                                    Badge(containerColor = KindColors.Error) {
                                        Text("$alertCount", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp),
                                tint = iconColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = textColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KindTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Orqaga",
                        tint = KindColors.TextPrimary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = KindColors.Surface,
            titleContentColor = KindColors.TextPrimary
        )
    )
}

@Composable
fun KindGradientHeader(
    title: String,
    subtitle: String = "",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(KindGradients.HeaderGradient)
            .padding(horizontal = KindSpacing.screenHorizontal, vertical = KindSpacing.xxl)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
