package com.fitsoul.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitsoul.app.ui.screens.onboarding.SplashScreen
import com.fitsoul.app.ui.screens.home.ModernDashboard
import com.fitsoul.app.ui.screens.auth.ModernLoginScreen
import com.fitsoul.app.ui.screens.auth.ModernSignUpScreen
import com.fitsoul.app.ui.screens.auth.ForgotPasswordScreen
import com.fitsoul.app.ui.screens.onboarding.ObjectivesScreen
import com.fitsoul.app.ui.screens.analysis.AnalysisScreen
import com.fitsoul.app.ui.screens.challenges.ChallengesScreen
import com.fitsoul.app.ui.screens.profile.ProfileScreen
import com.fitsoul.app.ui.screens.ai.AICoachScreen
import com.fitsoul.app.ui.screens.workout.MyWorkoutsScreen
import com.fitsoul.app.ui.screens.workout.AIWorkoutGeneratorScreen
import com.fitsoul.app.ui.screens.workout.WorkoutDetailScreen
import com.fitsoul.app.ui.screens.workout.AIWorkout
import com.fitsoul.app.ui.screens.progress.AIProgressScreen
import com.fitsoul.app.ui.viewmodel.AuthViewModel
import com.fitsoul.app.ui.viewmodel.WorkoutViewModel
import com.fitsoul.app.ui.components.FitsoulBottomNavigationBar
import androidx.navigation.navArgument
import androidx.navigation.NavType
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun FitsoulNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val authState by authViewModel.authState.observeAsState()
    
    // Force sign out on app start - always require login
    LaunchedEffect(Unit) {
        authViewModel.signOut()
    }
    
    // Always start with login screen to require sign-in
    val startDestination = "login"
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable("splash") {
            SplashScreen()
        }
        
        // Authentication Flow
        composable("login") {
            ModernLoginScreen(
                onLoginSuccess = {
                    navController.navigate("objectives") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate("signup")
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }

        composable("signup") {
            ModernSignUpScreen(
                onSignUpSuccess = {
                    navController.navigate("objectives") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }
        
        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Onboarding Flow
        composable("objectives") {
            ObjectivesScreen(
                onContinue = { _ ->
                    // Save selected objectives (implement later)
                    navController.navigate("main_app") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Main App with Bottom Navigation
        composable("main_app") {
            MainAppScreen(authViewModel = authViewModel)
        }
        
        // Individual Screens for Bottom Navigation
        composable("home") {
            ProtectedRoute(
                authState = authState,
                navController = navController,
                onAuthenticated = {
                    ModernDashboard(
                        onNavigateToWorkouts = {
                            navController.navigate("workouts")
                        },
                        onNavigateToProfile = {
                            navController.navigate("profile")
                        }
                    )
                }
            )
        }
        
        composable("analysis") {
            ProtectedRoute(
                authState = authState,
                navController = navController,
                onAuthenticated = {
                    AnalysisScreen()
                }
            )
        }
        
        composable("challenges") {
            ProtectedRoute(
                authState = authState,
                navController = navController,
                onAuthenticated = {
                    ChallengesScreen()
                }
            )
        }
        
        composable("profile") {
            ProtectedRoute(
                authState = authState,
                navController = navController,
                onAuthenticated = {
                    ProfileScreen(
                        onSignOut = {
                            authViewModel.signOut()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun MainAppScreen(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.observeAsState()
    
    Scaffold(
        bottomBar = {
            FitsoulBottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "ai_coach",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("ai_coach") {
                AICoachScreen()
            }
            
            composable("my_workouts") {
                MyWorkoutsScreen(
                    onWorkoutClick = { workout ->
                        // Serialize workout and navigate to detail screen
                        val workoutJson = Json.encodeToString(workout)
                        val encodedWorkout = URLEncoder.encode(workoutJson, StandardCharsets.UTF_8.toString())
                        navController.navigate("workout_detail/$encodedWorkout")
                    },
                    onGenerateNewWorkout = {
                        navController.navigate("ai_coach")  // Navigate to AI coach to generate new workout
                    }
                )
            }
            
            composable(
                "workout_detail/{workoutJson}",
                arguments = listOf(navArgument("workoutJson") { type = NavType.StringType })
            ) { backStackEntry ->
                val workoutJson = backStackEntry.arguments?.getString("workoutJson")
                val decodedWorkout = URLDecoder.decode(workoutJson, StandardCharsets.UTF_8.toString())
                val workout = Json.decodeFromString<AIWorkout>(decodedWorkout)
                val workoutViewModel: WorkoutViewModel = hiltViewModel()
                
                WorkoutDetailScreen(
                    workout = workout,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onStartWorkout = { workout ->
                        // Navigate to AI coach to start the workout
                        navController.navigate("ai_coach") {
                            // Clear the detail screen from stack
                            popUpTo("my_workouts") { inclusive = false }
                        }
                    },
                    onDeleteWorkout = { workoutId ->
                        workoutViewModel.deleteWorkout(workoutId)
                        // Navigate back to workouts list
                        navController.popBackStack()
                    }
                )
            }
            
            composable("generate_workout") {
                AIWorkoutGeneratorScreen(
                    onBackPressed = {
                        navController.popBackStack()
                    },
                    onStartWorkout = { workoutPlan ->
                        navController.navigate("ai_coach") // Navigate to AI coach to execute the workout
                    }
                )
            }
            
            composable("progress") {
                AIProgressScreen()
            }
            
            composable("profile") {
                ProfileScreen(
                    onSignOut = {
                        authViewModel.signOut()
                        // Navigate back to login - this will be handled by auth state change
                    }
                )
            }
            
            // Legacy routes for backward compatibility
            composable("home") {
                AICoachScreen() // Redirect home to AI Coach
            }
            
            composable("analysis") {
                AIProgressScreen() // Redirect analysis to AI Progress
            }
            
            composable("challenges") {
                ChallengesScreen()
            }
        }
    }
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        if (authState?.isUnauthenticated() == true) {
            // Navigate to login screen when signed out
            // This will be handled by the main navigation
        }
    }
}

/**
 * Composable that protects routes requiring authentication
 */
@Composable
private fun ProtectedRoute(
    authState: AuthViewModel.AuthState?,
    navController: androidx.navigation.NavController,
    onAuthenticated: @Composable () -> Unit
) {
    when {
        authState?.isAuthenticated() == true -> {
            onAuthenticated()
        }
        authState?.isUnauthenticated() == true -> {
            // Redirect to login if not authenticated
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        }
        else -> {
            // Show loading indicator while checking auth status
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}