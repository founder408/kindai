package com.kindai.app.ui.screens.health

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kindai.app.core.HealthConnectManager
import com.kindai.app.ui.components.*
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthConnectScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val sdkStatus = remember { HealthConnectManager.getSdkStatus(context) }
    val isAvailable = sdkStatus == HealthConnectClient.SDK_AVAILABLE

    var permissionsGranted by remember { mutableStateOf(false) }
    var permissionDenied by remember { mutableStateOf(false) }
    var checking by remember { mutableStateOf(true) }

    // Official HC permission launcher — uses PERMISSIONS from HealthConnectManager
    val permLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedSet ->
        val allGranted = HealthConnectManager.PERMISSIONS.all { it in grantedSet }
        permissionsGranted = allGranted
        permissionDenied = !allGranted
    }

    suspend fun check() {
        checking = true
        permissionsGranted = HealthConnectManager.hasAllPermissions(context)
        checking = false
    }

    LaunchedEffect(Unit) { check() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) scope.launch { check() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            snackbarHostState.showSnackbar("Salomatlik ma'lumotlariga ruxsat berildi ✓")
        }
    }

    Scaffold(
        topBar = { KindTopAppBar(title = "Health Connect", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

            when {
                checking -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = KindColors.Primary, modifier = Modifier.size(36.dp))
                    }
                }

                sdkStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    ActionCard(
                        icon = Icons.Default.SystemUpdate,
                        iconTint = KindColors.Warning,
                        iconBg = KindColors.Warning.copy(0.12f),
                        bg = KindColors.WarningBg,
                        title = "Health Connect yangilanishi kerak",
                        body = "Qurilmangizda Health Connect eski versiyasi bor. Play Market'dan yangilang.",
                        primaryText = "Yangilash",
                        primaryIcon = Icons.Default.Download,
                        onPrimary = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=com.google.android.apps.healthdata"))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                    )
                }

                !isAvailable -> {
                    ActionCard(
                        icon = Icons.Default.Info,
                        iconTint = KindColors.Warning,
                        iconBg = KindColors.Warning.copy(0.12f),
                        bg = KindColors.WarningBg,
                        title = "Telefoningizda Health Connect mavjud emas",
                        body = "Play Market'dan Health Connect ilovasini o'rnating.",
                        primaryText = "Health Connect o'rnatish",
                        primaryIcon = Icons.Default.Download,
                        onPrimary = {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=com.google.android.apps.healthdata"))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                            }
                        }
                    )
                }

                permissionsGranted -> {
                    // Connected state
                    Card(
                        shape = KindShapes.cardLarge,
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(KindShapes.cardLarge)
                                .background(KindGradients.SuccessGradient)
                                .padding(KindSpacing.cardPaddingXL)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.CheckCircle, null,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Ulangan!", fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = androidx.compose.ui.graphics.Color.White)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Sog'liq ma'lumotlari AI tahlili uchun o'qiladi",
                                    fontSize = 14.sp,
                                    color = androidx.compose.ui.graphics.Color.White.copy(0.9f),
                                    textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                permissionDenied -> {
                    ActionCard(
                        icon = Icons.Default.Block,
                        iconTint = KindColors.Error,
                        iconBg = KindColors.ErrorBg,
                        bg = KindColors.ErrorBg,
                        title = "Ruxsat berilmadi",
                        body = "Ruxsat berilmadi. Keyinroq Salomatlik manbalari bo'limidan qayta ulashingiz mumkin.",
                        primaryText = "Qayta ruxsat so'rash",
                        primaryIcon = Icons.Default.Refresh,
                        onPrimary = {
                            permissionDenied = false
                            permLauncher.launch(HealthConnectManager.PERMISSIONS)
                        }
                    )
                }

                else -> {
                    // Main permission request card
                    Card(
                        shape = KindShapes.cardLarge,
                        colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(KindSpacing.cardPaddingXL),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(64.dp).clip(KindShapes.full)
                                    .background(KindGradients.PrimaryGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MonitorHeart, null,
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(32.dp))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Salomatlik ma'lumotlarini ulash",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = KindColors.TextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Kind AI sizning roziligingiz bilan telefoningizdagi salomatlik ma'lumotlarini o'qishi mumkin: qadamlar, uyqu, yurak urishi, kislorod darajasi va faollik. Bu ma'lumotlar AI xavf tahlili uchun ishlatiladi.",
                                fontSize = 14.sp,
                                color = KindColors.TextSecondary,
                                lineHeight = 21.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = KindShapes.medium,
                                colors = CardDefaults.cardColors(containerColor = KindColors.WarningBg),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, null,
                                        tint = KindColors.Warning, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Bu tibbiy tashxis emas. Zarur holatda shifokorga murojaat qiling.",
                                        fontSize = 12.sp,
                                        color = KindColors.TextSecondary,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            KindPrimaryButton(
                                text = "Ruxsat berish",
                                icon = Icons.Default.Lock,
                                onClick = { permLauncher.launch(HealthConnectManager.PERMISSIONS) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = onBack) {
                                Text("Keyinroq", fontSize = 14.sp, color = KindColors.TextSecondary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data types list
            if (!checking) {
                KindSectionHeader(title = "O'qiladigan ma'lumotlar")
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = KindShapes.card,
                    colors = CardDefaults.cardColors(containerColor = KindColors.Surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(KindSpacing.cardPadding)) {
                        listOf(
                            Triple(Icons.Default.DirectionsWalk, "Qadamlar", "Kunlik qadamlar"),
                            Triple(Icons.Default.Bedtime, "Uyqu", "Uyqu davomiyligi"),
                            Triple(Icons.Default.MonitorHeart, "Yurak urishi", "BPM"),
                            Triple(Icons.Default.Air, "Kislorod (SpO2)", "Qon kislorod"),
                            Triple(Icons.Default.LocalFireDepartment, "Kaloriyalar", "Umumiy sarf"),
                            Triple(Icons.Default.FitnessCenter, "Mashqlar", "Faollik sessiyasi"),
                        ).forEachIndexed { idx, (icon, title, desc) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(36.dp).clip(KindShapes.medium)
                                    .background(KindColors.SecondaryLight),
                                    contentAlignment = Alignment.Center) {
                                    Icon(icon, null, tint = KindColors.Secondary,
                                        modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(title, fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp, color = KindColors.TextPrimary)
                                    Text(desc, fontSize = 12.sp, color = KindColors.TextTertiary)
                                }
                                if (permissionsGranted)
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint = KindColors.Success, modifier = Modifier.size(18.dp))
                            }
                            if (idx < 5) HorizontalDivider(color = KindColors.Divider,
                                modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SafetyDisclaimerCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Inline Color import needed for gradients above
private val Color = androidx.compose.ui.graphics.Color

@Composable
private fun ActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    iconBg: androidx.compose.ui.graphics.Color,
    bg: androidx.compose.ui.graphics.Color,
    title: String,
    body: String,
    primaryText: String,
    primaryIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onPrimary: () -> Unit,
) {
    Card(shape = KindShapes.card,
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(KindSpacing.cardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).clip(KindShapes.medium).background(iconBg),
                    contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    color = KindColors.TextPrimary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(body, fontSize = 13.sp, color = KindColors.TextSecondary, lineHeight = 19.sp)
            Spacer(modifier = Modifier.height(14.dp))
            KindPrimaryButton(text = primaryText, icon = primaryIcon, onClick = onPrimary)
        }
    }
}
