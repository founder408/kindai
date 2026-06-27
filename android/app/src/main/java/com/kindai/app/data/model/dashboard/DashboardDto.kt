package com.kindai.app.data.model.dashboard

import com.google.gson.annotations.SerializedName
import com.kindai.app.data.model.ai.AiAssessmentDto
import com.kindai.app.data.model.elderly.ElderlyProfileDto
import com.kindai.app.data.model.health.HealthRecordDto
import com.kindai.app.data.model.watch.WatchDataDto

data class DashboardDto(
    val elderly: ElderlyProfileDto,
    @SerializedName("last_health_record") val lastHealthRecord: HealthRecordDto?,
    @SerializedName("last_watch_data") val lastWatchData: WatchDataDto?,
    @SerializedName("latest_ai_assessment") val latestAiAssessment: AiAssessmentDto?,
    @SerializedName("unread_alerts_count") val unreadAlertsCount: Int,
    @SerializedName("medication_adherence") val medicationAdherence: MedicationAdherenceDto
)

data class MedicationAdherenceDto(
    @SerializedName("total_scheduled") val totalScheduled: Int,
    @SerializedName("total_taken") val totalTaken: Int,
    @SerializedName("adherence_percent") val adherencePercent: Double
)
