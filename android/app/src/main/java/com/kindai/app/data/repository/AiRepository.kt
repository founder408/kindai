package com.kindai.app.data.repository

import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.ai.AiAssessmentDto
import com.kindai.app.data.remote.ApiService

class AiRepository(private val api: ApiService) {

    suspend fun runAiAssessment(elderlyId: String): UiState<AiAssessmentDto> {
        return safeApiCall { api.runAiAssessment(elderlyId) }
    }

    suspend fun getAiAssessments(elderlyId: String): UiState<List<AiAssessmentDto>> {
        return safeApiCall { api.getAiAssessments(elderlyId) }
    }

    suspend fun getLatestAiAssessment(elderlyId: String): UiState<AiAssessmentDto> {
        return safeApiCall { api.getLatestAiAssessment(elderlyId) }
    }
}
