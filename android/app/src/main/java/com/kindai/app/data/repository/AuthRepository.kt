package com.kindai.app.data.repository

import com.kindai.app.core.UiState
import com.kindai.app.core.safeApiCall
import com.kindai.app.data.model.auth.LoginRequest
import com.kindai.app.data.model.auth.LoginResponse
import com.kindai.app.data.model.auth.RegisterRequest
import com.kindai.app.data.model.auth.UserDto
import com.kindai.app.data.remote.ApiService

class AuthRepository(private val api: ApiService) {

    suspend fun register(request: RegisterRequest): UiState<UserDto> {
        return safeApiCall { api.register(request) }
    }

    suspend fun login(request: LoginRequest): UiState<LoginResponse> {
        return safeApiCall { api.login(request) }
    }

    suspend fun getMe(): UiState<UserDto> {
        return safeApiCall { api.getMe() }
    }
}
