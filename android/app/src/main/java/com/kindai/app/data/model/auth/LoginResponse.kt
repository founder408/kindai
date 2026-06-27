package com.kindai.app.data.model.auth

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("must_change_password") val mustChangePassword: Boolean = false,
    val role: String = "CHILD"
)
