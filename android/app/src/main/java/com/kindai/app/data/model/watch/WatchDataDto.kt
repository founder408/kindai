package com.kindai.app.data.model.watch

import com.google.gson.annotations.SerializedName

data class WatchDataDto(
    val id: String,
    @SerializedName("elderly_id") val elderlyId: String,
    @SerializedName("heart_rate") val heartRate: Int?,
    val steps: Int?,
    @SerializedName("sleep_hours") val sleepHours: Double?,
    val spo2: Int?,
    @SerializedName("fall_detected") val fallDetected: Boolean,
    val battery: Int?,
    @SerializedName("recorded_at") val recordedAt: String
)
