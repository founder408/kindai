package com.kindai.app.core

object Constants {
    // For emulator: http://10.0.2.2:8000/
    // For real device local: http://192.168.x.x:8000/
    // For deployed backend: https://your-backend.onrender.com/
    const val BASE_URL = "http://172.16.8.134:8001/"

    const val DATASTORE_NAME = "kind_ai_prefs"
    const val TOKEN_KEY = "jwt_token"
    const val ONBOARDING_KEY = "onboarding_done"
}
