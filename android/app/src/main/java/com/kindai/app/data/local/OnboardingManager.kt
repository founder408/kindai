package com.kindai.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

class OnboardingManager(private val context: Context) {
    private val onboardingKey = booleanPreferencesKey("onboarding_done")

    suspend fun isOnboardingDone(): Boolean {
        return context.dataStore.data.first()[onboardingKey] ?: false
    }

    suspend fun setOnboardingDone() {
        context.dataStore.edit { prefs ->
            prefs[onboardingKey] = true
        }
    }
}
