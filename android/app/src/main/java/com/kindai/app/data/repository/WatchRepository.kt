package com.kindai.app.data.repository

import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.watch.WatchDataCreateRequest
import com.kindai.app.data.model.watch.WatchDataDto
import com.kindai.app.data.remote.ApiService

class WatchRepository(private val api: ApiService) {

    suspend fun createWatchData(elderlyId: String, request: WatchDataCreateRequest): UiState<WatchDataDto> {
        return safeApiCall { api.createWatchData(elderlyId, request) }
    }

    suspend fun getWatchDataList(elderlyId: String): UiState<List<WatchDataDto>> {
        return safeApiCall { api.getWatchDataList(elderlyId) }
    }

    suspend fun getLatestWatchData(elderlyId: String): UiState<WatchDataDto> {
        return safeApiCall { api.getLatestWatchData(elderlyId) }
    }
}
