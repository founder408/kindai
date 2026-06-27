package com.kindai.app.core

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*

object HealthConnectManager {

    private const val TAG = "KindHC"

    // These MUST match AndroidManifest uses-permission exactly
    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
    )

    fun getSdkStatus(context: Context): Int {
        val status = HealthConnectClient.getSdkStatus(context)
        Log.d(TAG, "SDK status: $status (AVAILABLE=${HealthConnectClient.SDK_AVAILABLE})")
        return status
    }

    fun isAvailable(context: Context) =
        getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    suspend fun getGrantedPermissions(context: Context): Set<String> {
        return try {
            val client = HealthConnectClient.getOrCreate(context)
            val granted = client.permissionController.getGrantedPermissions()
            Log.d(TAG, "Granted ${granted.size}/${PERMISSIONS.size}: $granted")
            granted
        } catch (e: Exception) {
            Log.e(TAG, "getGrantedPermissions error: ${e.message}")
            emptySet()
        }
    }

    suspend fun hasAllPermissions(context: Context): Boolean {
        val granted = getGrantedPermissions(context)
        val required = PERMISSIONS.map { it }
        val hasAll = required.all { it in granted }
        Log.d(TAG, "hasAllPermissions=$hasAll")
        return hasAll
    }
}
