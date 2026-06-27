package com.kindai.app.data.model.elderly

import com.google.gson.annotations.SerializedName
import com.kindai.app.data.model.health.HealthRecordDto

data class ElderlyDashboardDto(
    val profile: ElderlyProfileDto,
    @SerializedName("latest_health") val latestHealth: HealthRecordDto?,
    @SerializedName("unread_alerts") val unreadAlerts: Int,
    @SerializedName("total_meds") val totalMeds: Int,
    @SerializedName("taken_today") val takenToday: Int,
    @SerializedName("latest_risk_level") val latestRiskLevel: String?,
)
