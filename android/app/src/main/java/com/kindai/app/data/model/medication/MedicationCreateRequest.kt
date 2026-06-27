package com.kindai.app.data.model.medication

import com.google.gson.annotations.SerializedName

data class MedicationCreateRequest(
    val name: String,
    val dosage: String?,
    val instruction: String?,
    @SerializedName("time_to_take") val timeToTake: String,
    @SerializedName("before_or_after_meal") val beforeOrAfterMeal: String?
)
