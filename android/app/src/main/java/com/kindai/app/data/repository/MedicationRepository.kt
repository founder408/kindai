package com.kindai.app.data.repository

import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.medication.MedicationCreateRequest
import com.kindai.app.data.model.medication.MedicationDto
import com.kindai.app.data.model.medication.MedicationLogDto
import com.kindai.app.data.model.medication.MedicationTakenRequest
import com.kindai.app.data.remote.ApiService

class MedicationRepository(private val api: ApiService) {

    suspend fun createMedication(elderlyId: String, request: MedicationCreateRequest): UiState<MedicationDto> {
        return safeApiCall { api.createMedication(elderlyId, request) }
    }

    suspend fun getMedications(elderlyId: String): UiState<List<MedicationDto>> {
        return safeApiCall { api.getMedications(elderlyId) }
    }

    suspend fun deleteMedication(medicationId: String): UiState<Unit> {
        return safeApiCall { api.deleteMedication(medicationId) }
    }

    suspend fun markMedicationTaken(medicationId: String, request: MedicationTakenRequest): UiState<MedicationLogDto> {
        return safeApiCall { api.markMedicationTaken(medicationId, request) }
    }
}
