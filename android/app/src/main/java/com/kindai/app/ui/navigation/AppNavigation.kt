package com.kindai.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kindai.app.data.local.OnboardingManager
import com.kindai.app.data.local.TokenManager
import com.kindai.app.data.remote.ApiService
import com.kindai.app.ui.screens.ai.AiAssessmentScreen
import com.kindai.app.ui.screens.alerts.AlertsScreen
import com.kindai.app.ui.screens.auth.ChangePasswordScreen
import com.kindai.app.ui.screens.auth.LoginScreen
import com.kindai.app.ui.screens.auth.RegisterScreen
import com.kindai.app.ui.screens.caregiver.AddPersonScreen
import com.kindai.app.ui.screens.caregiver.CaregiverHomeScreen
import com.kindai.app.ui.screens.dashboard.DashboardScreen
import com.kindai.app.ui.screens.elderly_own.ElderlyDevicesScreen
import com.kindai.app.ui.screens.elderly_own.ElderlyDoctorSummaryScreen
import com.kindai.app.ui.screens.elderly_own.ElderlySosScreen
import com.kindai.app.ui.screens.elderly_own.ElderlyTrendsScreen
import com.kindai.app.ui.screens.elderly_own.ElderlyOwnAiScreen
import com.kindai.app.ui.screens.elderly_own.ElderlyOwnAlertsScreen
import com.kindai.app.ui.screens.elderly_own.ElderlyOwnHealthScreen
import com.kindai.app.ui.screens.elderly_own.ElderlyOwnHomeScreen
import com.kindai.app.ui.screens.elderly_own.ElderlyOwnMedicationsScreen
import com.kindai.app.ui.screens.elderlysimple.ElderlySimpleHomeScreen
import com.kindai.app.ui.screens.devices.DevicesScreen
import com.kindai.app.ui.screens.health.AddHealthRecordScreen
import com.kindai.app.ui.screens.health.HealthConnectScreen
import com.kindai.app.ui.screens.health.HealthSourcesScreen
import com.kindai.app.ui.screens.medications.MedicationsScreen
import com.kindai.app.ui.screens.onboarding.OnboardingScreen
import com.kindai.app.ui.screens.settings.SettingsScreen
import com.kindai.app.ui.screens.splash.SplashScreen
import com.kindai.app.ui.screens.diabetes.AddDiabetesMedScreen
import com.kindai.app.ui.screens.diabetes.AddGlucoseScreen
import com.kindai.app.ui.screens.diabetes.DiabetesAiScreen
import com.kindai.app.ui.screens.diabetes.DiabetesAlertsScreen
import com.kindai.app.ui.screens.diabetes.DiabetesHomeScreen
import com.kindai.app.ui.screens.diabetes.DiabetesMedicationScreen
import com.kindai.app.ui.screens.diabetes.DiabetesSetupScreen
import com.kindai.app.ui.screens.diabetes.DiabetesSosScreen
import com.kindai.app.ui.screens.diabetes.DiabetesTrendsScreen
import com.kindai.app.ui.screens.diabetes.DoctorSummaryScreen
import com.kindai.app.ui.screens.diabetes.FamilySharingScreen
import com.kindai.app.ui.screens.diabetes.GlucoseHistoryScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    tokenManager: TokenManager,
    onboardingManager: OnboardingManager,
    apiService: ApiService
) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                tokenManager = tokenManager,
                onboardingManager = onboardingManager,
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.CAREGIVER_HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onboardingManager = onboardingManager,
                onDone = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                apiService = apiService,
                tokenManager = tokenManager,
                onLoginSuccess = { role, mustChangePassword ->
                    if (mustChangePassword) {
                        navController.navigate(Routes.CHANGE_PASSWORD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else if (role == "ELDERLY") {
                        navController.navigate(Routes.ELDERLY_OWN_HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Routes.CAREGIVER_HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                apiService = apiService,
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CHANGE_PASSWORD) {
            ChangePasswordScreen(
                apiService = apiService,
                onSuccess = {
                    navController.navigate(Routes.ELDERLY_OWN_HOME) {
                        popUpTo(Routes.CHANGE_PASSWORD) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CAREGIVER_HOME) {
            CaregiverHomeScreen(
                apiService = apiService,
                tokenManager = tokenManager,
                onNavigateToAddPerson = {
                    navController.navigate(Routes.ADD_PERSON)
                },
                onNavigateToDashboard = { elderlyId ->
                    navController.navigate(Routes.dashboard(elderlyId))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onSessionExpired = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToHealthSources = {
                    navController.navigate(Routes.HEALTH_SOURCES)
                },
                onNavigateToDevices = {
                    navController.navigate(Routes.DEVICES)
                }
            )
        }

        composable(Routes.ELDERLY_OWN_HOME) {
            ElderlyOwnHomeScreen(
                apiService = apiService,
                tokenManager = tokenManager,
                onNavigateToHealth = { navController.navigate("elderly_own_health") },
                onNavigateToMedications = { navController.navigate("elderly_own_medications") },
                onNavigateToAi = { navController.navigate("elderly_own_ai") },
                onNavigateToAlerts = { navController.navigate("elderly_own_alerts") },
                onNavigateToTrends = { navController.navigate(Routes.ELDERLY_TRENDS) },
                onNavigateToDoctorSummary = { navController.navigate(Routes.ELDERLY_DOCTOR_SUMMARY) },
                onNavigateToDevices = { navController.navigate(Routes.ELDERLY_DEVICES) },
                onNavigateToSos = { navController.navigate(Routes.ELDERLY_SOS) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("elderly_own_health") {
            ElderlyOwnHealthScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onNavigateToAi = { navController.navigate("elderly_own_ai") }
            )
        }

        composable("elderly_own_medications") {
            ElderlyOwnMedicationsScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable("elderly_own_ai") {
            ElderlyOwnAiScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onNavigateToSos = { navController.navigate(Routes.ELDERLY_SOS) }
            )
        }

        composable("elderly_own_alerts") {
            ElderlyOwnAlertsScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ELDERLY_SOS) {
            ElderlySosScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ELDERLY_TRENDS) {
            ElderlyTrendsScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ELDERLY_DOCTOR_SUMMARY) {
            ElderlyDoctorSummaryScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ELDERLY_DEVICES) {
            ElderlyDevicesScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_PERSON) {
            AddPersonScreen(
                apiService = apiService,
                onSuccess = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.DASHBOARD,
            arguments = listOf(navArgument("elderlyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val elderlyId = backStackEntry.arguments?.getString("elderlyId") ?: return@composable
            DashboardScreen(
                elderlyId = elderlyId,
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onNavigateToHealth = { navController.navigate(Routes.addHealth(elderlyId)) },
                onNavigateToAi = { navController.navigate(Routes.aiAssessment(elderlyId)) },
                onNavigateToAlerts = { navController.navigate(Routes.alerts(elderlyId)) },
                onNavigateToMedications = { navController.navigate(Routes.medications(elderlyId)) },
                onNavigateToSimple = { navController.navigate(Routes.elderlySimple(elderlyId)) }
            )
        }

        composable(
            Routes.ELDERLY_SIMPLE,
            arguments = listOf(navArgument("elderlyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val elderlyId = backStackEntry.arguments?.getString("elderlyId") ?: return@composable
            ElderlySimpleHomeScreen(
                elderlyId = elderlyId,
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onNavigateToHealth = { navController.navigate(Routes.addHealth(elderlyId, "simple")) },
                onNavigateToMedications = { navController.navigate(Routes.medications(elderlyId)) },
                onNavigateToAi = { navController.navigate(Routes.aiAssessment(elderlyId, "simple")) }
            )
        }

        composable(
            Routes.ADD_HEALTH,
            arguments = listOf(
                navArgument("elderlyId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType; defaultValue = "detailed" }
            )
        ) { backStackEntry ->
            val elderlyId = backStackEntry.arguments?.getString("elderlyId") ?: return@composable
            val mode = backStackEntry.arguments?.getString("mode") ?: "detailed"
            AddHealthRecordScreen(
                elderlyId = elderlyId,
                mode = mode,
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onNavigateToAi = { navController.navigate(Routes.aiAssessment(elderlyId)) }
            )
        }

        composable(
            Routes.AI_ASSESSMENT,
            arguments = listOf(
                navArgument("elderlyId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType; defaultValue = "caregiver" }
            )
        ) { backStackEntry ->
            val elderlyId = backStackEntry.arguments?.getString("elderlyId") ?: return@composable
            val mode = backStackEntry.arguments?.getString("mode") ?: "caregiver"
            AiAssessmentScreen(
                elderlyId = elderlyId,
                mode = mode,
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onNavigateToAlerts = { navController.navigate(Routes.alerts(elderlyId)) }
            )
        }

        composable(
            Routes.ALERTS,
            arguments = listOf(navArgument("elderlyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val elderlyId = backStackEntry.arguments?.getString("elderlyId") ?: return@composable
            AlertsScreen(
                elderlyId = elderlyId,
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.MEDICATIONS,
            arguments = listOf(navArgument("elderlyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val elderlyId = backStackEntry.arguments?.getString("elderlyId") ?: return@composable
            MedicationsScreen(
                elderlyId = elderlyId,
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                apiService = apiService,
                tokenManager = tokenManager,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HEALTH_SOURCES) {
            HealthSourcesScreen(
                onBack = { navController.popBackStack() },
                onNavigateToHealthConnect = { navController.navigate(Routes.HEALTH_CONNECT) }
            )
        }

        composable(Routes.HEALTH_CONNECT) {
            HealthConnectScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DEVICES) {
            DevicesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Diabetes routes ───────────────────────────────────────────────────

        composable(Routes.DIABETES_SETUP) {
            DiabetesSetupScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onSetupComplete = {
                    navController.navigate(Routes.DIABETES_HOME) {
                        popUpTo(Routes.DIABETES_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DIABETES_HOME) {
            DiabetesHomeScreen(
                apiService = apiService,
                tokenManager = tokenManager,
                onNavigateToAddGlucose = { navController.navigate(Routes.ADD_GLUCOSE) },
                onNavigateToHistory = { navController.navigate(Routes.GLUCOSE_HISTORY) },
                onNavigateToMeds = { navController.navigate(Routes.DIABETES_MEDS) },
                onNavigateToAi = { navController.navigate(Routes.DIABETES_AI) },
                onNavigateToTrends = { navController.navigate(Routes.DIABETES_TRENDS) },
                onNavigateToDoctorSummary = { navController.navigate(Routes.DIABETES_DOCTOR_SUMMARY) },
                onNavigateToAlerts = { navController.navigate(Routes.DIABETES_ALERTS) },
                onNavigateToSos = { navController.navigate(Routes.DIABETES_SOS) },
                onNavigateToFamilySharing = { navController.navigate(Routes.FAMILY_SHARING) },
                onNavigateToSetup = { navController.navigate(Routes.DIABETES_SETUP) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADD_GLUCOSE) {
            AddGlucoseScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onSaved = { runAi ->
                    if (runAi) {
                        navController.navigate("${Routes.DIABETES_AI}?autoRun=true") {
                            popUpTo(Routes.ADD_GLUCOSE) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(Routes.GLUCOSE_HISTORY) {
            GlucoseHistoryScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onNavigateToAddGlucose = { navController.navigate(Routes.ADD_GLUCOSE) }
            )
        }

        composable(Routes.DIABETES_MEDS) {
            DiabetesMedicationScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onNavigateToAddMed = { navController.navigate(Routes.ADD_DIABETES_MED) }
            )
        }

        composable(Routes.ADD_DIABETES_MED) {
            AddDiabetesMedScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            "${Routes.DIABETES_AI}?autoRun={autoRun}",
            arguments = listOf(navArgument("autoRun") { type = NavType.BoolType; defaultValue = false })
        ) { backStackEntry ->
            val autoRun = backStackEntry.arguments?.getBoolean("autoRun") ?: false
            DiabetesAiScreen(
                apiService = apiService,
                autoRun = autoRun,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DIABETES_TRENDS) {
            DiabetesTrendsScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DIABETES_DOCTOR_SUMMARY) {
            DoctorSummaryScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DIABETES_ALERTS) {
            DiabetesAlertsScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DIABETES_SOS) {
            DiabetesSosScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FAMILY_SHARING) {
            FamilySharingScreen(
                apiService = apiService,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
