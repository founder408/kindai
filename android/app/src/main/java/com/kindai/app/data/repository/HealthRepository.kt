package com.kindai.app.data.repository

import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.health.HealthRecordCreateRequest
import com.kindai.app.data.model.health.HealthRecordDto
import com.kindai.app.data.remote.ApiService

class HealthRepository(private val api: ApiService) {

    suspend fun createHealthRecord(elderlyId: String, request: HealthRecordCreateRequest): UiState<HealthRecordDto> {
        return safeApiCall { api.createHealthRecord(elderlyId, request) }
    }

    suspend fun getHealthRecords(elderlyId: String): UiState<List<HealthRecordDto>> {
        return safeApiCall { api.getHealthRecords(elderlyId) }
    }

    suspend fun getLatestHealthRecord(elderlyId: String): UiState<HealthRecordDto> {
        return safeApiCall { api.getLatestHealthRecord(elderlyId) }
    }
}
