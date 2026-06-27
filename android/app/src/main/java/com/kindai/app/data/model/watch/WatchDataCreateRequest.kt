package com.kindai.app.data.model.watch

import com.google.gson.annotations.SerializedName

data class WatchDataCreateRequest(
    @SerializedName("heart_rate") val heartRate: Int?,
    val steps: Int?,
    @SerializedName("sleep_hours") val sleepHours: Double?,
    val spo2: Int?,
    @SerializedName("fall_detected") val fallDetected: Boolean = false,
    val battery: Int?
)
