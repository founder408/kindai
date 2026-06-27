package com.kindai.app.data.repository

import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.alert.AlertDto
import com.kindai.app.data.remote.ApiService

class AlertsRepository(private val api: ApiService) {

    suspend fun getMyAlerts(): UiState<List<AlertDto>> {
        return safeApiCall { api.getMyAlerts() }
    }

    suspend fun getElderlyAlerts(elderlyId: String): UiState<List<AlertDto>> {
        return safeApiCall { api.getElderlyAlerts(elderlyId) }
    }

    suspend fun markAlertRead(alertId: String): UiState<AlertDto> {
        return safeApiCall { api.markAlertRead(alertId) }
    }
}
