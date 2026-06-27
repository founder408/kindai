package com.kindai.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kindai.app.core.Constants
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.local.TokenManager
import com.kindai.app.data.model.auth.UserDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.KindAiTopBar
import com.kindai.app.ui.components.SafetyDisclaimerCard
import com.kindai.app.ui.theme.KindRed
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    apiService: ApiService,
    tokenManager: TokenManager,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var user by remember { mutableStateOf<UserDto?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val result = safeApiCall { apiService.getMe() }
        if (result is UiState.Success) user = result.data
    }

    Scaffold(
        topBar = { KindAiTopBar(title = "Sozlamalar", onBack = onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (user != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Foydalanuvchi", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ism: ${user!!.fullName}")
                        Text("Email: ${user!!.email ?: "-"}")
                        Text("Telefon: ${user!!.phone ?: "-"}")
                        Text("Rol: ${user!!.role}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Server", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("BASE_URL: ${Constants.BASE_URL}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            SafetyDisclaimerCard()

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Maxfiylik va xavfsizlik", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Kind AI foydalanuvchilarning sog'liq ma'lumotlarini maxfiy saqlaydi. " +
                        "Ilova shifokor emas va kasallik tashxisi qo'ymaydi. " +
                        "AI faqat xavf darajasini baholaydi.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        tokenManager.clearToken()
                        onLogout()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = KindRed)
            ) {
                Text("Chiqish", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
