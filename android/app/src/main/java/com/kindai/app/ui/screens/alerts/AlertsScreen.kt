package com.kindai.app.ui.screens.alerts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.alert.AlertDto
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.components.ErrorView
import com.kindai.app.ui.components.KindAiTopBar
import com.kindai.app.ui.components.LoadingView
import com.kindai.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AlertsScreen(
    elderlyId: String,
    apiService: ApiService,
    onBack: () -> Unit
) {
    var uiState by remember { mutableStateOf<UiState<List<AlertDto>>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            uiState = UiState.Loading
            uiState = safeApiCall { apiService.getElderlyAlerts(elderlyId) }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Scaffold(
        topBar = { KindAiTopBar(title = "Ogohlantirishlar", onBack = onBack) }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> LoadingView(modifier = Modifier.padding(padding))
            is UiState.Error -> ErrorView(state.message, onRetry = { loadData() }, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                        Text("Hozircha ogohlantirishlar yo'q.", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(state.data) { alert ->
                            AlertCard(alert = alert, onMarkRead = {
                                scope.launch {
                                    safeApiCall { apiService.markAlertRead(alert.id) }
                                    loadData()
                                }
                            })
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun AlertCard(alert: AlertDto, onMarkRead: () -> Unit) {
    val severityColor = when (alert.severity.uppercase()) {
        "EMERGENCY" -> KindRedDark
        "HIGH" -> KindRed
        "WARNING" -> KindOrange
        else -> KindBlue
    }
    val bgColor = when (alert.severity.uppercase()) {
        "EMERGENCY", "HIGH" -> KindRedLight
        "WARNING" -> KindOrangeLight
        else -> KindSurface
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alert.isRead) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = alert.severity,
                    color = severityColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
                if (!alert.isRead) {
                    TextButton(onClick = onMarkRead) {
                        Text("O'qildi", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
