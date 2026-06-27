package com.kindai.app.data.model.elderly

import com.google.gson.annotations.SerializedName

data class ElderlyProfileDto(
    val id: String,
    @SerializedName("child_user_id") val childUserId: String,
    @SerializedName("elderly_user_id") val elderlyUserId: String?,
    @SerializedName("full_name") val fullName: String,
    val age: Int?,
    val gender: String?,
    @SerializedName("chronic_diseases") val chronicDiseases: String?,
    @SerializedName("emergency_phone") val emergencyPhone: String?,
    val address: String?,
    @SerializedName("relationship_to_elderly") val relationshipToElderly: String?,
    @SerializedName("consent_confirmed") val consentConfirmed: Boolean = false,
    @SerializedName("consent_confirmed_at") val consentConfirmedAt: String?,
    @SerializedName("created_at") val createdAt: String
)
