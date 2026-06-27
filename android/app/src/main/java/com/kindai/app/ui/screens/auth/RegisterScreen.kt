package com.kindai.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.auth.RegisterRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindPasswordField
import com.kindai.app.ui.components.KindPrimaryButton
import com.kindai.app.ui.components.KindTextField
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    apiService: ApiService,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KindColors.Background)
    ) {
        // Premium gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(KindGradients.HeaderGradientLarge),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(KindShapes.xl)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Kind AI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Yangi hisob yarating",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-20).dp)
                .background(KindColors.Background, KindShapes.topOnly)
                .padding(horizontal = KindSpacing.screenHorizontal)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(28.dp))

            KindTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "To'liq ism",
                leadingIcon = Icons.Default.Person
            )
            Spacer(modifier = Modifier.height(14.dp))

            KindTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email
            )
            Spacer(modifier = Modifier.height(14.dp))

            KindTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Telefon (ixtiyoriy)",
                leadingIcon = Icons.Default.Phone
            )
            Spacer(modifier = Modifier.height(14.dp))

            KindPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Parol (kamida 6 belgi)",
                leadingIcon = Icons.Default.Lock
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = KindColors.ErrorBg),
                    shape = KindShapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = KindColors.Error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage!!,
                            color = KindColors.Error,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = KindColors.SuccessBg),
                    shape = KindShapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = KindColors.Success,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = successMessage!!,
                            color = KindColors.Success,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Ro'yxatdan o'tish orqali siz maxfiylik va rozilik talablariga amal qilishga rozilik bildirasiz.",
                fontSize = 12.sp,
                color = KindColors.TextTertiary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            KindPrimaryButton(
                text = "Ro'yxatdan o'tish",
                isLoading = isLoading,
                enabled = fullName.isNotBlank() && email.isNotBlank() && password.length >= 6,
                icon = Icons.Default.PersonAdd,
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val result = safeApiCall {
                            apiService.register(
                                RegisterRequest(
                                    fullName = fullName.trim(),
                                    email = email.trim(),
                                    phone = phone.trim().ifBlank { null },
                                    password = password
                                )
                            )
                        }
                        when (result) {
                            is UiState.Success -> {
                                successMessage = "Muvaffaqiyatli! Endi kiring."
                                kotlinx.coroutines.delay(1000)
                                onRegisterSuccess()
                            }
                            is UiState.Error -> errorMessage = result.message
                            else -> {}
                        }
                        isLoading = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "Hisobingiz bormi? ",
                    fontSize = 14.sp,
                    color = KindColors.TextSecondary
                )
                Text(
                    text = "Kirish",
                    fontSize = 14.sp,
                    color = KindColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
