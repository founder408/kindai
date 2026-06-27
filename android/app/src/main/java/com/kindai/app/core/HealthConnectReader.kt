package com.kindai.app.core

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

data class DaySteps(val date: String, val steps: Long)

data class HcHealthData(
    // availability
    val sdkAvailable: Boolean = false,
    val anyPermissionGranted: Boolean = false,
    val grantedCount: Int = 0,
    val totalPermissions: Int = 6,
    // today / latest values
    val todaySteps: Long? = null,
    val latestHeartRate: Int? = null,
    val latestSpo2: Int? = null,
    val lastNightSleepHours: Double? = null,
    val todayCalories: Int? = null,
    // weekly
    val weeklySteps: List<DaySteps> = emptyList(),
    val avgWeeklySteps: Long? = null,
    val weeklyAvgHr: Int? = null,
    val weeklyAvgSleep: Double? = null,
    // loading/error
    val isLoading: Boolean = true,
    val errorMsg: String? = null,
)

suspend fun readHcHealthData(context: Context): HcHealthData {
    val sdkStatus = HealthConnectClient.getSdkStatus(context)
    if (sdkStatus != HealthConnectClient.SDK_AVAILABLE) {
        return HcHealthData(sdkAvailable = false, isLoading = false)
    }

    return try {
        val client = HealthConnectClient.getOrCreate(context)
        val granted = client.permissionController.getGrantedPermissions()
        Log.d("HcReader", "Granted permissions: ${granted.size} → $granted")

        if (granted.isEmpty()) {
            return HcHealthData(
                sdkAvailable = true,
                anyPermissionGranted = false,
                isLoading = false
            )
        }

        val now = Instant.now()
        val sevenDaysAgo = now.minus(7, ChronoUnit.DAYS)
        val oneDayAgo = now.minus(1, ChronoUnit.DAYS)
        val timeRange7 = TimeRangeFilter.between(sevenDaysAgo, now)
        val timeRange1 = TimeRangeFilter.between(oneDayAgo, now)
        val zone = ZoneId.systemDefault()

        // ── Steps (7 days + today) ──────────────────────────────────────────
        var todaySteps: Long? = null
        val weeklySteps = mutableListOf<DaySteps>()
        if (granted.any { "STEPS" in it }) {
            try {
                val stepsReq = ReadRecordsRequest(StepsRecord::class, timeRange7)
                val allSteps = client.readRecords(stepsReq).records
                Log.d("HcReader", "Steps records: ${allSteps.size}")

                // Group by day
                val byDay = allSteps.groupBy { record ->
                    record.startTime.atZone(zone).toLocalDate().toString()
                }
                byDay.entries.sortedBy { it.key }.forEach { (date, records) ->
                    val total = records.sumOf { it.count }
                    weeklySteps.add(DaySteps(date = date, steps = total))
                }

                // Today steps
                val todayDate = now.atZone(zone).toLocalDate().toString()
                todaySteps = byDay[todayDate]?.sumOf { it.count }
                    ?: allSteps.filter {
                        it.startTime.atZone(zone).toLocalDate().toString() == todayDate
                    }.sumOf { it.count }.takeIf { it > 0L }
            } catch (e: Exception) {
                Log.w("HcReader", "Steps read failed: ${e.message}")
            }
        }

        // ── Heart rate ───────────────────────────────────────────────────────
        var latestHr: Int? = null
        var weeklyAvgHr: Int? = null
        if (granted.any { "HEART_RATE" in it }) {
            try {
                val hrReq = ReadRecordsRequest(HeartRateRecord::class, timeRange7)
                val hrRecords = client.readRecords(hrReq).records
                Log.d("HcReader", "HR records: ${hrRecords.size}")
                val allSamples = hrRecords.flatMap { it.samples }
                latestHr = allSamples.lastOrNull()?.beatsPerMinute?.toInt()
                if (allSamples.isNotEmpty()) {
                    weeklyAvgHr = (allSamples.map { it.beatsPerMinute }.average()).roundToInt()
                }
            } catch (e: Exception) {
                Log.w("HcReader", "HR read failed: ${e.message}")
            }
        }

        // ── Sleep ────────────────────────────────────────────────────────────
        var lastNightSleep: Double? = null
        var weeklyAvgSleep: Double? = null
        if (granted.any { "SLEEP" in it }) {
            try {
                val sleepReq = ReadRecordsRequest(SleepSessionRecord::class, timeRange7)
                val sleepRecords = client.readRecords(sleepReq).records
                Log.d("HcReader", "Sleep records: ${sleepRecords.size}")
                if (sleepRecords.isNotEmpty()) {
                    val last = sleepRecords.last()
                    lastNightSleep = (last.endTime.epochSecond - last.startTime.epochSecond) / 3600.0
                    val totalHours = sleepRecords.sumOf { s ->
                        (s.endTime.epochSecond - s.startTime.epochSecond) / 3600.0
                    }
                    weeklyAvgSleep = totalHours / sleepRecords.size
                }
            } catch (e: Exception) {
                Log.w("HcReader", "Sleep read failed: ${e.message}")
            }
        }

        // ── SpO2 ─────────────────────────────────────────────────────────────
        var latestSpo2: Int? = null
        if (granted.any { "OXYGEN_SATURATION" in it }) {
            try {
                val spo2Req = ReadRecordsRequest(OxygenSaturationRecord::class, timeRange7)
                val spo2Records = client.readRecords(spo2Req).records
                Log.d("HcReader", "SpO2 records: ${spo2Records.size}")
                latestSpo2 = spo2Records.lastOrNull()?.percentage?.value?.roundToInt()
            } catch (e: Exception) {
                Log.w("HcReader", "SpO2 read failed: ${e.message}")
            }
        }

        // ── Calories ─────────────────────────────────────────────────────────
        var todayCal: Int? = null
        if (granted.any { "TOTAL_CALORIES" in it }) {
            try {
                val calReq = ReadRecordsRequest(TotalCaloriesBurnedRecord::class, timeRange1)
                val calRecords = client.readRecords(calReq).records
                val total = calRecords.sumOf { it.energy.inKilocalories }.roundToInt()
                todayCal = if (total > 0) total else null
                Log.d("HcReader", "Calories 1d: $total from ${calRecords.size} records")
            } catch (e: Exception) {
                Log.w("HcReader", "Calories read failed: ${e.message}")
            }
        }

        val avgSteps = if (weeklySteps.isNotEmpty())
            weeklySteps.map { it.steps }.average().toLong() else null

        HcHealthData(
            sdkAvailable = true,
            anyPermissionGranted = true,
            grantedCount = granted.size,
            totalPermissions = HealthConnectManager.PERMISSIONS.size,
            todaySteps = todaySteps,
            latestHeartRate = latestHr,
            latestSpo2 = latestSpo2,
            lastNightSleepHours = lastNightSleep,
            todayCalories = todayCal,
            weeklySteps = weeklySteps.takeLast(7),
            avgWeeklySteps = avgSteps,
            weeklyAvgHr = weeklyAvgHr,
            weeklyAvgSleep = weeklyAvgSleep,
            isLoading = false,
        )
    } catch (e: Exception) {
        Log.e("HcReader", "Fatal HC read error: ${e.message}", e)
        HcHealthData(sdkAvailable = true, anyPermissionGranted = true, isLoading = false, errorMsg = e.message)
    }
}
