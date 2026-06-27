package com.kindai.app.data.model.medication

import com.google.gson.annotations.SerializedName

data class MedicationDto(
    val id: String,
    @SerializedName("elderly_id") val elderlyId: String,
    val name: String,
    val dosage: String?,
    val instruction: String?,
    @SerializedName("time_to_take") val timeToTake: String,
    @SerializedName("before_or_after_meal") val beforeOrAfterMeal: String?,
    val active: Boolean,
    @SerializedName("created_at") val createdAt: String
)
