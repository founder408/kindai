package com.kindai.app.ui.screens.auth

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.auth.ChangePasswordRequest
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindPasswordField
import com.kindai.app.ui.components.KindPrimaryButton
import com.kindai.app.ui.design.KindColors
import com.kindai.app.ui.design.KindGradients
import com.kindai.app.ui.design.KindShapes
import com.kindai.app.ui.design.KindSpacing
import kotlinx.coroutines.launch

@Composable
fun ChangePasswordScreen(
    apiService: ApiService,
    onSuccess: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KindColors.Background)
    ) {
        // Gradient header
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
                        .size(52.dp)
                        .clip(KindShapes.xl)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Parolni yangilash",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Xavfsizlik uchun parolingizni o'zgartiring",
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
            Spacer(modifier = Modifier.height(32.dp))

            KindPasswordField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "Joriy parol",
                leadingIcon = Icons.Default.Lock
            )

            Spacer(modifier = Modifier.height(16.dp))

            KindPasswordField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Yangi parol",
                leadingIcon = Icons.Default.Lock
            )

            Spacer(modifier = Modifier.height(16.dp))

            KindPasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Yangi parolni tasdiqlang",
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
                text = "Parolni yangilash",
                isLoading = isLoading,
                enabled = currentPassword.isNotBlank() && newPassword.length >= 6 && newPassword == confirmPassword,
                icon = Icons.Default.Check,
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null

                        if (newPassword != confirmPassword) {
                            errorMessage = "Parollar mos kelmadi"
                            isLoading = false
                            return@launch
                        }

                        val result = safeApiCall {
                            apiService.changePassword(
                                ChangePasswordRequest(
                                    currentPassword = currentPassword,
                                    newPassword = newPassword
                                )
                            )
                        }
                        when (result) {
                            is UiState.Success -> onSuccess()
                            is UiState.Error -> errorMessage = result.message
                            else -> {}
                        }
                        isLoading = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
