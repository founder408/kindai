package com.kindai.app.data.model.medication

import com.google.gson.annotations.SerializedName

data class AdherenceDto(
    @SerializedName("period_days") val periodDays: Int,
    @SerializedName("total_scheduled") val totalScheduled: Int,
    @SerializedName("total_taken") val totalTaken: Int,
    @SerializedName("adherence_percent") val adherencePercent: Double,
)
