package com.kindai.app.data.model.elderly

import com.google.gson.annotations.SerializedName
import com.kindai.app.data.model.health.HealthRecordDto

data class DoctorSummaryDto(
    @SerializedName("elderly_name") val elderlyName: String,
    val age: Int?,
    @SerializedName("chronic_diseases") val chronicDiseases: String?,
    @SerializedName("last_health_record") val lastHealthRecord: HealthRecordDto?,
    @SerializedName("recent_assessments_count") val recentAssessmentsCount: Int,
    @SerializedName("latest_risk_level") val latestRiskLevel: String?,
    @SerializedName("active_medications") val activeMedications: Int,
    @SerializedName("generated_at") val generatedAt: String,
)
