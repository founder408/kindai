package com.kindai.app.data.model.diabetes

import com.google.gson.annotations.SerializedName

// ── Profile ───────────────────────────────────────────────────────────────────

data class DiabetesProfileDto(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("full_name") val fullName: String,
    val age: Int?,
    val gender: String?,
    @SerializedName("diabetes_type") val diabetesType: String?,
    @SerializedName("diagnosis_year") val diagnosisYear: Int?,
    @SerializedName("target_glucose_min") val targetGlucoseMin: String?,
    @SerializedName("target_glucose_max") val targetGlucoseMax: String?,
    @SerializedName("glucose_unit") val glucoseUnit: String = "mmol/L",
    @SerializedName("chronic_diseases") val chronicDiseases: String?,
    @SerializedName("emergency_phone") val emergencyPhone: String?,
    @SerializedName("caregiver_user_id") val caregiverUserId: String?,
    @SerializedName("consent_confirmed") val consentConfirmed: Boolean = false,
    @SerializedName("created_at") val createdAt: String,
)

data class DiabetesProfileCreateRequest(
    @SerializedName("full_name") val fullName: String,
    val age: Int? = null,
    val gender: String? = null,
    @SerializedName("diabetes_type") val diabetesType: String? = null,
    @SerializedName("diagnosis_year") val diagnosisYear: Int? = null,
    @SerializedName("target_glucose_min") val targetGlucoseMin: String? = null,
    @SerializedName("target_glucose_max") val targetGlucoseMax: String? = null,
    @SerializedName("glucose_unit") val glucoseUnit: String = "mmol/L",
    @SerializedName("chronic_diseases") val chronicDiseases: String? = null,
    @SerializedName("emergency_phone") val emergencyPhone: String? = null,
)

// ── Glucose Record ────────────────────────────────────────────────────────────

data class DiabetesRecordDto(
    val id: String,
    @SerializedName("profile_id") val profileId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("glucose_value") val glucoseValue: Double,
    val unit: String = "mmol/L",
    @SerializedName("measurement_context") val measurementContext: String = "RANDOM",
    @SerializedName("measured_at") val measuredAt: String,
    val symptoms: String?,
    val notes: String?,
    val source: String = "MANUAL",
    @SerializedName("created_at") val createdAt: String,
)

data class DiabetesRecordCreateRequest(
    @SerializedName("glucose_value") val glucoseValue: Double,
    val unit: String = "mmol/L",
    @SerializedName("measurement_context") val measurementContext: String = "RANDOM",
    @SerializedName("measured_at") val measuredAt: String,
    val symptoms: String? = null,
    val notes: String? = null,
    val source: String = "MANUAL",
)

// ── Glucose Trends ────────────────────────────────────────────────────────────

data class GlucoseTrendsDto(
    val period: String,
    @SerializedName("total_records") val totalRecords: Int,
    @SerializedName("average_glucose") val averageGlucose: Double?,
    @SerializedName("highest_glucose") val highestGlucose: Double?,
    @SerializedName("lowest_glucose") val lowestGlucose: Double?,
    @SerializedName("fasting_average") val fastingAverage: Double?,
    @SerializedName("after_meal_average") val afterMealAverage: Double?,
    @SerializedName("before_meal_average") val beforeMealAverage: Double?,
    @SerializedName("bedtime_average") val bedtimeAverage: Double?,
    @SerializedName("trend_direction") val trendDirection: String = "STABLE",
    @SerializedName("daily_data") val dailyData: List<DailyGlucoseData> = emptyList(),
)

data class DailyGlucoseData(
    val date: String,
    val avg: Double,
    val count: Int,
)

// ── Medication ────────────────────────────────────────────────────────────────

data class DiabetesMedicationDto(
    val id: String,
    @SerializedName("profile_id") val profileId: String,
    val name: String,
    @SerializedName("medication_type") val medicationType: String = "TABLET",
    val dosage: String?,
    @SerializedName("insulin_units") val insulinUnits: Double?,
    @SerializedName("schedule_time") val scheduleTime: String,
    val frequency: String = "DAILY",
    @SerializedName("before_or_after_meal") val beforeOrAfterMeal: String?,
    val instructions: String?,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String,
)

data class DiabetesMedicationCreateRequest(
    val name: String,
    @SerializedName("medication_type") val medicationType: String = "TABLET",
    val dosage: String? = null,
    @SerializedName("insulin_units") val insulinUnits: Double? = null,
    @SerializedName("schedule_time") val scheduleTime: String,
    val frequency: String = "DAILY",
    @SerializedName("before_or_after_meal") val beforeOrAfterMeal: String? = null,
    val instructions: String? = null,
)

data class DiabetesMedicationUpdateRequest(
    val name: String? = null,
    @SerializedName("medication_type") val medicationType: String? = null,
    val dosage: String? = null,
    @SerializedName("insulin_units") val insulinUnits: Double? = null,
    @SerializedName("schedule_time") val scheduleTime: String? = null,
    val frequency: String? = null,
    val instructions: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
)

data class DiabetesMedTakenRequest(
    @SerializedName("scheduled_time") val scheduledTime: String,
    val status: String = "TAKEN",
    val note: String? = null,
)

data class DiabetesMedLogDto(
    val id: String,
    @SerializedName("medication_id") val medicationId: String,
    @SerializedName("profile_id") val profileId: String,
    @SerializedName("scheduled_time") val scheduledTime: String,
    val status: String,
    @SerializedName("taken_at") val takenAt: String?,
    val note: String?,
    @SerializedName("created_at") val createdAt: String,
)

data class DiabetesAdherenceDto(
    @SerializedName("period_days") val periodDays: Int,
    @SerializedName("total_scheduled") val totalScheduled: Int,
    @SerializedName("total_taken") val totalTaken: Int,
    @SerializedName("total_missed") val totalMissed: Int,
    @SerializedName("total_skipped") val totalSkipped: Int,
    @SerializedName("adherence_percent") val adherencePercent: Double,
    @SerializedName("streak_days") val streakDays: Int,
)

// ── AI Assessment ─────────────────────────────────────────────────────────────

data class DiabetesAiAssessmentDto(
    val id: String,
    @SerializedName("profile_id") val profileId: String,
    val source: String?,
    @SerializedName("risk_level") val riskLevel: String,
    val summary: String,
    @SerializedName("key_concerns") val keyConcerns: List<String>?,
    @SerializedName("recommended_next_steps") val recommendedNextSteps: List<String>?,
    @SerializedName("what_to_monitor") val whatToMonitor: List<String>?,
    @SerializedName("doctor_note") val doctorNote: String?,
    val disclaimer: String,
    @SerializedName("created_at") val createdAt: String,
)

// ── Alerts ────────────────────────────────────────────────────────────────────

data class DiabetesAlertDto(
    val id: String,
    @SerializedName("profile_id") val profileId: String,
    val type: String,
    val title: String,
    val message: String,
    val severity: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String,
)

data class DiabetesProfileUpdateRequest(
    @SerializedName("full_name") val fullName: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    @SerializedName("diabetes_type") val diabetesType: String? = null,
    @SerializedName("emergency_phone") val emergencyPhone: String? = null,
    @SerializedName("caregiver_user_id") val caregiverUserId: Long? = null,
    @SerializedName("consent_confirmed") val consentConfirmed: Boolean? = null,
)

data class DiabetesSosRequest(
    val message: String? = null,
    @SerializedName("current_glucose") val currentGlucose: Double? = null,
)

data class DiabetesSosResponse(
    @SerializedName("alert_id") val alertId: String,
    val message: String,
    @SerializedName("emergency_phone") val emergencyPhone: String?,
)

// ── Dashboard ─────────────────────────────────────────────────────────────────

data class MedAdherenceSummaryDto(
    @SerializedName("total_scheduled") val totalScheduled: Int,
    @SerializedName("total_taken") val totalTaken: Int,
    @SerializedName("adherence_percent") val adherencePercent: Double,
)

data class WeeklySummaryDto(
    @SerializedName("average_glucose") val averageGlucose: Double?,
    @SerializedName("highest_glucose") val highestGlucose: Double?,
    @SerializedName("lowest_glucose") val lowestGlucose: Double?,
    @SerializedName("trend_direction") val trendDirection: String = "STABLE",
    @SerializedName("total_records") val totalRecords: Int = 0,
)

data class DiabetesDashboardDto(
    val profile: DiabetesProfileDto,
    @SerializedName("today_glucose") val todayGlucose: DiabetesRecordDto?,
    @SerializedName("latest_glucose") val latestGlucose: DiabetesRecordDto?,
    @SerializedName("today_medications") val todayMedications: List<DiabetesMedicationDto>,
    @SerializedName("medication_adherence") val medicationAdherence: MedAdherenceSummaryDto?,
    @SerializedName("latest_ai_assessment") val latestAiAssessment: DiabetesAiAssessmentDto?,
    @SerializedName("unread_alerts") val unreadAlerts: Int = 0,
    @SerializedName("weekly_summary") val weeklySummary: WeeklySummaryDto?,
)

// ── Doctor Summary ────────────────────────────────────────────────────────────

data class GlucoseSummaryDto(
    val period: String,
    @SerializedName("total_records") val totalRecords: Int,
    @SerializedName("average_glucose") val averageGlucose: Double?,
    @SerializedName("highest_glucose") val highestGlucose: Double?,
    @SerializedName("lowest_glucose") val lowestGlucose: Double?,
    @SerializedName("fasting_average") val fastingAverage: Double?,
    @SerializedName("after_meal_average") val afterMealAverage: Double?,
    @SerializedName("in_range_percent") val inRangePercent: Double?,
)

data class DiabetesDoctorSummaryDto(
    val period: String,
    val profile: DiabetesProfileDto,
    @SerializedName("glucose_summary") val glucoseSummary: GlucoseSummaryDto,
    @SerializedName("medication_adherence") val medicationAdherence: DiabetesAdherenceDto,
    @SerializedName("symptoms_summary") val symptomsSummary: String?,
    @SerializedName("latest_ai_summary") val latestAiSummary: String?,
    @SerializedName("latest_risk_level") val latestRiskLevel: String?,
    @SerializedName("doctor_note") val doctorNote: String?,
    val disclaimer: String,
)
