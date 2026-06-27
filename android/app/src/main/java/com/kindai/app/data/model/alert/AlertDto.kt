package com.kindai.app.data.model.alert

import com.google.gson.annotations.SerializedName

data class AlertDto(
    val id: String,
    @SerializedName("elderly_id") val elderlyId: String,
    @SerializedName("child_user_id") val childUserId: String,
    val type: String,
    val title: String,
    val message: String,
    val severity: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String
)
