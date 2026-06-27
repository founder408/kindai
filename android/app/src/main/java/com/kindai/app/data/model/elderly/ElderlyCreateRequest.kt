package com.kindai.app.data.model.elderly

import com.google.gson.annotations.SerializedName

data class ElderlyCreateRequest(
    @SerializedName("full_name") val fullName: String,
    val age: Int?,
    val gender: String?,
    @SerializedName("chronic_diseases") val chronicDiseases: String?,
    @SerializedName("emergency_phone") val emergencyPhone: String?,
    val address: String?,
    @SerializedName("relationship_to_elderly") val relationshipToElderly: String?
)
