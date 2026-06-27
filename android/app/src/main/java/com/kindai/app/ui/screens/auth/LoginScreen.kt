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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.local.TokenManager
import com.kindai.app.data.model.auth.LoginRequest
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
fun LoginScreen(
    apiService: ApiService,
    tokenManager: TokenManager,
    onLoginSuccess: (role: String, mustChangePassword: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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
                .height(220.dp)
                .background(KindGradients.HeaderGradientLarge),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(KindShapes.xl)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Kind AI",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Oilaviy sog'liq monitoringi",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Form card overlapping header
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-24).dp)
                .background(KindColors.Background, KindShapes.topOnly)
                .padding(horizontal = KindSpacing.screenHorizontal)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Tizimga kirish",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = KindColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Davom etish uchun ma'lumotlaringizni kiriting",
                fontSize = 14.sp,
                color = KindColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            KindTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email yoki telefon",
                leadingIcon = Icons.Default.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            KindPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Parol",
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

            Spacer(modifier = Modifier.height(32.dp))

            KindPrimaryButton(
                text = "Kirish",
                isLoading = isLoading,
                enabled = email.isNotBlank() && password.isNotBlank(),
                icon = Icons.Default.Login,
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val result = safeApiCall {
                            apiService.login(LoginRequest(login = email.trim(), password = password))
                        }
                        when (result) {
                            is UiState.Success -> {
                                tokenManager.saveToken(result.data.accessToken)
                                onLoginSuccess(result.data.role, result.data.mustChangePassword)
                            }
                            is UiState.Error -> {
                                errorMessage = if (result.message == "SESSION_EXPIRED") {
                                    "Email yoki parol noto'g'ri"
                                } else result.message
                            }
                            else -> {}
                        }
                        isLoading = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.Center) {
                Text(
                    text = "Hisobingiz yo'qmi? ",
                    fontSize = 14.sp,
                    color = KindColors.TextSecondary
                )
                Text(
                    text = "Ro'yxatdan o'ting",
                    fontSize = 14.sp,
                    color = KindColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
