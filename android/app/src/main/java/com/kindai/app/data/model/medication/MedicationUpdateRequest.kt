package com.kindai.app.data.model.medication

import com.google.gson.annotations.SerializedName

data class MedicationUpdateRequest(
    val name: String? = null,
    val dosage: String? = null,
    val instruction: String? = null,
    @SerializedName("time_to_take") val timeToTake: String? = null,
    @SerializedName("before_or_after_meal") val beforeOrAfterMeal: String? = null,
    val active: Boolean? = null,
)
