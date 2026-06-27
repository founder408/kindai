package com.kindai.app.data.model.ai

import com.google.gson.annotations.SerializedName

data class AiAssessmentDto(
    val id: String,
    @SerializedName("elderly_id") val elderlyId: String,
    val source: String?,
    @SerializedName("risk_level") val riskLevel: String,
    val summary: String,
    val recommendation: String,
    @SerializedName("caregiver_summary") val caregiverSummary: String?,
    @SerializedName("elderly_summary") val elderlySummary: String?,
    @SerializedName("created_at") val createdAt: String
)
