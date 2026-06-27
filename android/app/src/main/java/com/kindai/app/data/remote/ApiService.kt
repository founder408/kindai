package com.kindai.app.data.remote

import com.kindai.app.data.model.ai.AiAssessmentDto
import com.kindai.app.data.model.diabetes.DiabetesAdherenceDto
import com.kindai.app.data.model.diabetes.DiabetesAiAssessmentDto
import com.kindai.app.data.model.diabetes.DiabetesAlertDto
import com.kindai.app.data.model.diabetes.DiabetesDashboardDto
import com.kindai.app.data.model.diabetes.DiabetesDoctorSummaryDto
import com.kindai.app.data.model.diabetes.DiabetesMedLogDto
import com.kindai.app.data.model.diabetes.DiabetesMedTakenRequest
import com.kindai.app.data.model.diabetes.DiabetesMedicationCreateRequest
import com.kindai.app.data.model.diabetes.DiabetesMedicationDto
import com.kindai.app.data.model.diabetes.DiabetesMedicationUpdateRequest
import com.kindai.app.data.model.diabetes.DiabetesProfileCreateRequest
import com.kindai.app.data.model.diabetes.DiabetesProfileDto
import com.kindai.app.data.model.diabetes.DiabetesProfileUpdateRequest
import com.kindai.app.data.model.diabetes.DiabetesRecordCreateRequest
import com.kindai.app.data.model.diabetes.DiabetesRecordDto
import com.kindai.app.data.model.diabetes.DiabetesSosRequest
import com.kindai.app.data.model.diabetes.DiabetesSosResponse
import com.kindai.app.data.model.diabetes.GlucoseTrendsDto
import com.kindai.app.data.model.alert.AlertDto
import com.kindai.app.data.model.auth.ChangePasswordRequest
import com.kindai.app.data.model.auth.LoginRequest
import com.kindai.app.data.model.auth.LoginResponse
import com.kindai.app.data.model.auth.RegisterRequest
import com.kindai.app.data.model.auth.UserDto
import com.kindai.app.data.model.dashboard.DashboardDto
import com.kindai.app.data.model.devices.DevicesDto
import com.kindai.app.data.model.elderly.DoctorSummaryDto
import com.kindai.app.data.model.elderly.ElderlyCreateRequest
import com.kindai.app.data.model.elderly.ElderlyCreateWithAccountRequest
import com.kindai.app.data.model.elderly.ElderlyDashboardDto
import com.kindai.app.data.model.elderly.ElderlyProfileDto
import com.kindai.app.data.model.health.HealthRecordCreateRequest
import com.kindai.app.data.model.health.HealthRecordDto
import com.kindai.app.data.model.health.HealthTrendsDto
import com.kindai.app.data.model.medication.AdherenceDto
import com.kindai.app.data.model.medication.MedicationCreateRequest
import com.kindai.app.data.model.medication.MedicationDto
import com.kindai.app.data.model.medication.MedicationLogDto
import com.kindai.app.data.model.medication.MedicationTakenRequest
import com.kindai.app.data.model.medication.MedicationUpdateRequest
import com.kindai.app.data.model.sos.SosRequest
import com.kindai.app.data.model.sos.SosResponse
import com.kindai.app.data.model.watch.WatchDataCreateRequest
import com.kindai.app.data.model.watch.WatchDataDto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<UserDto>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<UserDto>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Map<String, String>>

    // ── Elderly (caregiver manages) ───────────────────────────────────────────
    @POST("api/elderly")
    suspend fun createElderly(@Body request: ElderlyCreateRequest): Response<ElderlyProfileDto>

    @POST("api/elderly/with-account")
    suspend fun createElderlyWithAccount(@Body request: ElderlyCreateWithAccountRequest): Response<ElderlyProfileDto>

    @GET("api/elderly")
    suspend fun getElderlyList(): Response<List<ElderlyProfileDto>>

    @GET("api/elderly/{elderlyId}")
    suspend fun getElderly(@Path("elderlyId") elderlyId: String): Response<ElderlyProfileDto>

    @DELETE("api/elderly/{elderlyId}")
    suspend fun deleteElderly(@Path("elderlyId") elderlyId: String): Response<Unit>

    // ── Elderly Self-Service (prefix: /api/elderly/me) ───────────────────────
    @GET("api/elderly/me/profile")
    suspend fun getMyElderlyProfile(): Response<ElderlyProfileDto>

    @POST("api/elderly/me/consent")
    suspend fun confirmConsent(): Response<Map<String, String>>

    @GET("api/elderly/me/dashboard")
    suspend fun getMyDashboard(): Response<ElderlyDashboardDto>

    @POST("api/elderly/me/health-records")
    suspend fun createMyHealthRecord(@Body request: HealthRecordCreateRequest): Response<HealthRecordDto>

    @GET("api/elderly/me/health-records")
    suspend fun getMyHealthRecords(): Response<List<HealthRecordDto>>

    @GET("api/elderly/me/health-trends")
    suspend fun getMyHealthTrends(@Query("period") period: String = "weekly"): Response<HealthTrendsDto>

    @GET("api/elderly/me/medications")
    suspend fun getMyMedications(): Response<List<MedicationDto>>

    @POST("api/elderly/me/medications")
    suspend fun addMyMedication(@Body request: MedicationCreateRequest): Response<MedicationDto>

    @PUT("api/elderly/me/medications/{medicationId}")
    suspend fun updateMyMedication(
        @Path("medicationId") medicationId: String,
        @Body request: MedicationUpdateRequest
    ): Response<MedicationDto>

    @DELETE("api/elderly/me/medications/{medicationId}")
    suspend fun deleteMyMedication(@Path("medicationId") medicationId: String): Response<Unit>

    @POST("api/elderly/me/medications/{medicationId}/taken")
    suspend fun markMyMedicationTaken(
        @Path("medicationId") medicationId: String,
        @Body request: MedicationTakenRequest
    ): Response<MedicationLogDto>

    @GET("api/elderly/me/medication-adherence")
    suspend fun getMyMedicationAdherence(@Query("days") days: Int = 7): Response<AdherenceDto>

    @POST("api/elderly/me/ai-assess")
    suspend fun runMyAiAssessment(): Response<AiAssessmentDto>

    @GET("api/elderly/me/ai-assessments")
    suspend fun getMyAiAssessments(): Response<List<AiAssessmentDto>>

    @GET("api/elderly/me/alerts")
    suspend fun getMyElderlyAlerts(): Response<List<AlertDto>>

    @POST("api/elderly/me/sos")
    suspend fun sendSos(@Body request: SosRequest = SosRequest()): Response<SosResponse>

    @GET("api/elderly/me/devices")
    suspend fun getMyDevices(): Response<DevicesDto>

    @GET("api/elderly/me/doctor-summary")
    suspend fun getDoctorSummary(): Response<DoctorSummaryDto>

    // ── Health Records (caregiver) ────────────────────────────────────────────
    @POST("api/elderly/{elderlyId}/health-records")
    suspend fun createHealthRecord(
        @Path("elderlyId") elderlyId: String,
        @Body request: HealthRecordCreateRequest
    ): Response<HealthRecordDto>

    @GET("api/elderly/{elderlyId}/health-records")
    suspend fun getHealthRecords(@Path("elderlyId") elderlyId: String): Response<List<HealthRecordDto>>

    @GET("api/elderly/{elderlyId}/health-records/latest")
    suspend fun getLatestHealthRecord(@Path("elderlyId") elderlyId: String): Response<HealthRecordDto>

    // ── Watch Data ────────────────────────────────────────────────────────────
    @POST("api/elderly/{elderlyId}/watch-data")
    suspend fun createWatchData(
        @Path("elderlyId") elderlyId: String,
        @Body request: WatchDataCreateRequest
    ): Response<WatchDataDto>

    @GET("api/elderly/{elderlyId}/watch-data")
    suspend fun getWatchDataList(@Path("elderlyId") elderlyId: String): Response<List<WatchDataDto>>

    @GET("api/elderly/{elderlyId}/watch-data/latest")
    suspend fun getLatestWatchData(@Path("elderlyId") elderlyId: String): Response<WatchDataDto>

    // ── AI (caregiver) ────────────────────────────────────────────────────────
    @POST("api/elderly/{elderlyId}/ai-assess")
    suspend fun runAiAssessment(@Path("elderlyId") elderlyId: String): Response<AiAssessmentDto>

    @GET("api/elderly/{elderlyId}/ai-assessments")
    suspend fun getAiAssessments(@Path("elderlyId") elderlyId: String): Response<List<AiAssessmentDto>>

    @GET("api/elderly/{elderlyId}/ai-assessments/latest")
    suspend fun getLatestAiAssessment(@Path("elderlyId") elderlyId: String): Response<AiAssessmentDto>

    // ── Alerts (caregiver) ────────────────────────────────────────────────────
    @GET("api/alerts")
    suspend fun getMyAlerts(): Response<List<AlertDto>>

    @GET("api/elderly/{elderlyId}/alerts")
    suspend fun getElderlyAlerts(@Path("elderlyId") elderlyId: String): Response<List<AlertDto>>

    @PUT("api/alerts/{alertId}/read")
    suspend fun markAlertRead(@Path("alertId") alertId: String): Response<AlertDto>

    // ── Medications (caregiver) ───────────────────────────────────────────────
    @POST("api/elderly/{elderlyId}/medications")
    suspend fun createMedication(
        @Path("elderlyId") elderlyId: String,
        @Body request: MedicationCreateRequest
    ): Response<MedicationDto>

    @GET("api/elderly/{elderlyId}/medications")
    suspend fun getMedications(@Path("elderlyId") elderlyId: String): Response<List<MedicationDto>>

    @DELETE("api/medications/{medicationId}")
    suspend fun deleteMedication(@Path("medicationId") medicationId: String): Response<Unit>

    @POST("api/medications/{medicationId}/taken")
    suspend fun markMedicationTaken(
        @Path("medicationId") medicationId: String,
        @Body request: MedicationTakenRequest
    ): Response<MedicationLogDto>

    // ── Dashboard (caregiver) ─────────────────────────────────────────────────
    @GET("api/elderly/{elderlyId}/dashboard")
    suspend fun getDashboard(@Path("elderlyId") elderlyId: String): Response<DashboardDto>

    // ════════════════════════════════════════════════════════════════════════
    // DIABETES SELF-SERVICE  /api/diabetes/me/*
    // ════════════════════════════════════════════════════════════════════════

    @POST("api/diabetes/me/profile")
    suspend fun createDiabetesProfile(@Body request: DiabetesProfileCreateRequest): Response<DiabetesProfileDto>

    @GET("api/diabetes/me/profile")
    suspend fun getDiabetesProfile(): Response<DiabetesProfileDto>

    @PUT("api/diabetes/me/profile")
    suspend fun updateDiabetesProfile(@Body request: DiabetesProfileCreateRequest): Response<DiabetesProfileDto>

    @PATCH("api/diabetes/me/profile")
    suspend fun updateMyDiabetesProfile(@Body request: DiabetesProfileUpdateRequest): Response<DiabetesProfileDto>

    @GET("api/diabetes/me/dashboard")
    suspend fun getDiabetesDashboard(): Response<DiabetesDashboardDto>

    // Glucose
    @POST("api/diabetes/me/glucose")
    suspend fun addGlucoseRecord(@Body request: DiabetesRecordCreateRequest): Response<DiabetesRecordDto>

    @GET("api/diabetes/me/glucose")
    suspend fun getGlucoseRecords(
        @Query("period") period: String = "7d",
        @Query("context") context: String? = null,
        @Query("limit") limit: Int = 50,
    ): Response<List<DiabetesRecordDto>>

    @GET("api/diabetes/me/glucose/trends")
    suspend fun getGlucoseTrends(@Query("period") period: String = "7d"): Response<GlucoseTrendsDto>

    // Medications
    @GET("api/diabetes/me/medications")
    suspend fun getDiabetesMedications(): Response<List<DiabetesMedicationDto>>

    @POST("api/diabetes/me/medications")
    suspend fun addDiabetesMedication(@Body request: DiabetesMedicationCreateRequest): Response<DiabetesMedicationDto>

    @PUT("api/diabetes/me/medications/{id}")
    suspend fun updateDiabetesMedication(
        @Path("id") id: String,
        @Body request: DiabetesMedicationUpdateRequest,
    ): Response<DiabetesMedicationDto>

    @DELETE("api/diabetes/me/medications/{id}")
    suspend fun deleteDiabetesMedication(@Path("id") id: String): Response<Unit>

    @POST("api/diabetes/me/medications/{id}/taken")
    suspend fun markDiabetesMedTaken(
        @Path("id") id: String,
        @Body request: DiabetesMedTakenRequest,
    ): Response<DiabetesMedLogDto>

    @GET("api/diabetes/me/medication-adherence")
    suspend fun getDiabetesAdherence(@Query("days") days: Int = 7): Response<DiabetesAdherenceDto>

    // AI
    @POST("api/diabetes/me/ai-assess")
    suspend fun runDiabetesAiAssessment(): Response<DiabetesAiAssessmentDto>

    @GET("api/diabetes/me/ai-assessments")
    suspend fun getDiabetesAiAssessments(@Query("limit") limit: Int = 10): Response<List<DiabetesAiAssessmentDto>>

    // Alerts
    @GET("api/diabetes/me/alerts")
    suspend fun getDiabetesAlerts(
        @Query("unread_only") unreadOnly: Boolean = false,
    ): Response<List<DiabetesAlertDto>>

    @POST("api/diabetes/me/alerts/{alertId}/read")
    suspend fun markDiabetesAlertRead(@Path("alertId") alertId: String): Response<Map<String, String>>

    @POST("api/diabetes/me/sos")
    suspend fun sendDiabetesSos(@Body request: DiabetesSosRequest = DiabetesSosRequest()): Response<DiabetesSosResponse>

    // Doctor Summary
    @GET("api/diabetes/me/doctor-summary")
    suspend fun getDiabetesDoctorSummary(@Query("period") period: String = "30d"): Response<DiabetesDoctorSummaryDto>

    // ════════════════════════════════════════════════════════════════════════
    // DIABETES CAREGIVER  /api/diabetes/profiles/{profileId}/*
    // ════════════════════════════════════════════════════════════════════════

    @GET("api/diabetes/profiles/{profileId}/dashboard")
    suspend fun getCaregiverDiabetesDashboard(@Path("profileId") profileId: String): Response<Map<String, Any?>>

    @GET("api/diabetes/profiles/{profileId}/glucose")
    suspend fun getCaregiverGlucoseRecords(
        @Path("profileId") profileId: String,
        @Query("period") period: String = "7d",
    ): Response<List<DiabetesRecordDto>>

    @GET("api/diabetes/profiles/{profileId}/alerts")
    suspend fun getCaregiverDiabetesAlerts(@Path("profileId") profileId: String): Response<List<DiabetesAlertDto>>

    @GET("api/diabetes/profiles/{profileId}/doctor-summary")
    suspend fun getCaregiverDoctorSummary(
        @Path("profileId") profileId: String,
        @Query("period") period: String = "30d",
    ): Response<DiabetesDoctorSummaryDto>
}
