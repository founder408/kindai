package com.kindai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kindai.app.data.local.OnboardingManager
import com.kindai.app.data.local.TokenManager
import com.kindai.app.data.remote.RetrofitClient
import com.kindai.app.ui.navigation.AppNavigation
import com.kindai.app.ui.theme.KindAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(applicationContext)
        val onboardingManager = OnboardingManager(applicationContext)
        val apiService = RetrofitClient.getApiService(tokenManager)

        setContent {
            KindAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        tokenManager = tokenManager,
                        onboardingManager = onboardingManager,
                        apiService = apiService
                    )
                }
            }
        }
    }
}
