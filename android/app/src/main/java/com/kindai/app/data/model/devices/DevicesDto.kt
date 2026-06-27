package com.kindai.app.data.model.devices

import com.google.gson.annotations.SerializedName

data class DeviceInfoDto(
    val type: String,
    val name: String,
    val connected: Boolean,
    @SerializedName("last_sync") val lastSync: String?,
    val battery: Int?,
)

data class DevicesDto(
    val devices: List<DeviceInfoDto>,
)
