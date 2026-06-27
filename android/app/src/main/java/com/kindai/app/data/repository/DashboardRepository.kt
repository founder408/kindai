package com.kindai.app.data.repository

import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.dashboard.DashboardDto
import com.kindai.app.data.remote.ApiService

class DashboardRepository(private val api: ApiService) {

    suspend fun getDashboard(elderlyId: String): UiState<DashboardDto> {
        return safeApiCall { api.getDashboard(elderlyId) }
    }
}
