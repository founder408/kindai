package com.kindai.app.data.model.health

import com.google.gson.annotations.SerializedName

data class TrendPointDto(
    val date: String,
    @SerializedName("systolic_bp") val systolicBp: Double?,
    @SerializedName("diastolic_bp") val diastolicBp: Double?,
    @SerializedName("heart_rate") val heartRate: Double?,
    @SerializedName("blood_sugar") val bloodSugar: Double?,
    val spo2: Double?,
)

data class HealthTrendsDto(
    val period: String,
    val points: List<TrendPointDto>,
)
