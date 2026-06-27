package com.kindai.app.data.model.medication

import com.google.gson.annotations.SerializedName

data class MedicationLogDto(
    val id: String,
    @SerializedName("medication_id") val medicationId: String,
    @SerializedName("elderly_id") val elderlyId: String,
    @SerializedName("scheduled_time") val scheduledTime: String,
    val taken: Boolean,
    @SerializedName("taken_at") val takenAt: String?,
    @SerializedName("created_at") val createdAt: String
)
