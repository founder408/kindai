package com.kindai.app.data.model.auth

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    val email: String?,
    val phone: String?,
    val password: String,
    val role: String = "CHILD"
)
