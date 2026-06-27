package com.kindai.app.data.model.auth

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: String,
    @SerializedName("full_name") val fullName: String,
    val phone: String?,
    val email: String?,
    val role: String,
    @SerializedName("must_change_password") val mustChangePassword: Boolean = false,
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("created_at") val createdAt: String
)
