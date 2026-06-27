package com.kindai.app.ui.screens.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.ui.components.*
import com.kindai.app.ui.components.HealthConnectPanel
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            KindTopAppBar(title = "Qurilmalar", onBack = onBack)
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

            // Phone Health Connect section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(30.dp).clip(KindShapes.medium).background(KindColors.Primary.copy(0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhoneAndroid, null, tint = KindColors.Primary, modifier = Modifier.size(17.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Telefon sog'lik ma'lumotlari", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = KindColors.TextPrimary)
            }
            Spacer(modifier = Modifier.height(10.dp))
            HealthConnectPanel(modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(28.dp))

            // Kind Band hero card
            Card(
                shape = KindShapes.cardLarge,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(KindShapes.cardLarge)
                        .background(KindGradients.CyanGradient)
                        .padding(KindSpacing.cardPaddingXL)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(KindShapes.xl)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Watch,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Kind Band",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(KindShapes.buttonPill)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Tez orada",
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Kind Band — kattalar uchun maxsus yaratilgan aqlli bilakuzuk. Ekran shart emas, faqat sensor va tugmalar. Har doim kuzatuvda.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            KindDeviceCard(
                name = "Kind Band",
                description = "Kattalar va kasallar uchun mo'ljallangan maxsus aqlli bilakuzuk. Ekran va murakkab interfeys yo'q — faqat qulay sensor va SOS tugma.",
                icon = Icons.Default.Watch,
                iconTint = KindColors.Accent,
                iconBg = KindColors.AccentLight,
                badge = "Tez orada",
                badgeColor = KindColors.Warning,
                features = listOf(
                    "24/7 yurak urishi monitoring",
                    "Qon kislorod (SpO2) o'lchash",
                    "Uyqu sifatini kuzatish",
                    "Harakat va faollik hisoblash",
                    "Yiqilish sensori — avtomatik signal",
                    "SOS tugma — darhol ogohlantirish",
                    "Bluetooth orqali telefonга ulanish",
                    "7 kunlik batareya",
                    "IP68 suv o'tkazmaslik"
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            KindSectionHeader(title = "Boshqa qurilmalar")
            Spacer(modifier = Modifier.height(12.dp))

            KindDeviceCard(
                name = "Bluetooth Tonometr",
                description = "Bluetooth orqali avtomatik qon bosimni o'lchash va Kind AI ga yuborish.",
                icon = Icons.Default.Favorite,
                iconTint = KindColors.Error,
                iconBg = KindColors.ErrorBg,
                badge = "Tez orada",
                badgeColor = KindColors.Warning,
                features = listOf(
                    "Avtomatik Bluetooth sync",
                    "Tarix saqlash",
                    "AI tahlilga integratsiya"
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            KindDeviceCard(
                name = "Bluetooth Glukometr",
                description = "Qandli diabet nazorati uchun qon shakar ko'rsatkichini avtomatik saqlab borish.",
                icon = Icons.Default.Opacity,
                iconTint = Color(0xFFFF6B35),
                iconBg = Color(0xFFFFF0EB),
                badge = "Tez orada",
                badgeColor = KindColors.Warning,
                features = listOf(
                    "HbA1c tahlil",
                    "Qand darajasi grafik",
                    "Shifokorga yuborish"
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Notify interest
            Card(
                shape = KindShapes.card,
                colors = CardDefaults.cardColors(containerColor = KindColors.PrimaryLight),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(KindSpacing.cardPaddingXL),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = KindColors.Primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Kind Band chiqishidan xabardor bo'ling",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = KindColors.TextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Kind Band mavjud bo'lganida sizga bildirishnoma yuboramiz",
                        fontSize = 13.sp,
                        color = KindColors.TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    KindPrimaryButton(
                        text = "Xabardor qiling",
                        icon = Icons.Default.Notifications,
                        onClick = { /* future: register interest */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
