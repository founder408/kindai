package com.kindai.app.data.repository

import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.elderly.ElderlyCreateRequest
import com.kindai.app.data.model.elderly.ElderlyProfileDto
import com.kindai.app.data.remote.ApiService

class ElderlyRepository(private val api: ApiService) {

    suspend fun createElderly(request: ElderlyCreateRequest): UiState<ElderlyProfileDto> {
        return safeApiCall { api.createElderly(request) }
    }

    suspend fun getElderlyList(): UiState<List<ElderlyProfileDto>> {
        return safeApiCall { api.getElderlyList() }
    }

    suspend fun getElderly(elderlyId: String): UiState<ElderlyProfileDto> {
        return safeApiCall { api.getElderly(elderlyId) }
    }

    suspend fun deleteElderly(elderlyId: String): UiState<Unit> {
        return safeApiCall { api.deleteElderly(elderlyId) }
    }
}
