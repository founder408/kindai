package com.kindai.app.core

import retrofit2.Response

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): UiState<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            response.body()?.let { UiState.Success(it) }
                ?: UiState.Error("Bo'sh javob qaytdi")
        } else {
            when (response.code()) {
                401 -> UiState.Error("SESSION_EXPIRED")
                403 -> UiState.Error("Ruxsat yo'q")
                404 -> UiState.Error("Ma'lumot topilmadi")
                409 -> UiState.Error("Bu ma'lumot allaqachon mavjud")
                else -> UiState.Error("Xatolik: ${response.code()}")
            }
        }
    } catch (e: java.net.UnknownHostException) {
        UiState.Error("Server bilan bog'lanib bo'lmadi. Internet yoki backend URL'ni tekshiring.")
    } catch (e: java.net.ConnectException) {
        UiState.Error("Server bilan bog'lanib bo'lmadi. Internet yoki backend URL'ni tekshiring.")
    } catch (e: Exception) {
        UiState.Error("Kutilmagan xatolik: ${e.localizedMessage}")
    }
}
