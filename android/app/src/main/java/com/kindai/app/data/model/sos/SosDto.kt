package com.kindai.app.data.model.sos

import com.google.gson.annotations.SerializedName

data class SosRequest(
    val message: String = "Menga yordam kerak!",
    val location: String? = null,
)

data class SosResponse(
    @SerializedName("alert_id") val alertId: String,
    val message: String,
    @SerializedName("emergency_phone") val emergencyPhone: String?,
)
