package com.kindai.app.data.model.health

import com.google.gson.annotations.SerializedName

data class HealthRecordDto(
    val id: String,
    @SerializedName("elderly_id") val elderlyId: String,
    @SerializedName("systolic_bp") val systolicBp: Int?,
    @SerializedName("diastolic_bp") val diastolicBp: Int?,
    @SerializedName("blood_sugar") val bloodSugar: Double?,
    @SerializedName("heart_rate") val heartRate: Int?,
    val spo2: Int?,
    val temperature: Double?,
    val symptoms: String?,
    val mood: String?,
    @SerializedName("created_at") val createdAt: String
)
