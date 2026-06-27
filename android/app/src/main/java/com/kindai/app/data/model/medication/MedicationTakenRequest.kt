package com.kindai.app.data.model.medication

import com.google.gson.annotations.SerializedName

data class MedicationTakenRequest(
    @SerializedName("scheduled_time") val scheduledTime: String
)
