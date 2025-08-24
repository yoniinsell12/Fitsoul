package com.fitsoul.app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitsoul.app.data.ai.DeepSeekService
import com.fitsoul.app.ui.screens.workout.Exercise
import com.fitsoul.app.ui.screens.workout.WorkoutPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIWorkoutViewModel @Inject constructor(
    private val deepSeekService: DeepSeekService
) : ViewModel() {
    
    private val _workoutPlanState = MutableStateFlow<WorkoutPlanState>(WorkoutPlanState.Initial)
    val workoutPlanState: StateFlow<WorkoutPlanState> = _workoutPlanState
    
    fun generateWorkoutPlan(
        goals: List<String>,
        fitnessLevel: String,
        availableTime: Int,
        equipment: List<String> = emptyList()
    ) {
        android.util.Log.d("AIWorkoutViewModel", "🚀 Starting workout plan generation...")
        _workoutPlanState.value = WorkoutPlanState.Loading
        
        viewModelScope.launch {
            try {
                // First verify the service is configured properly
                if (!deepSeekService.isConfigured()) {
                    android.util.Log.e("AIWorkoutViewModel", "❌ DeepSeek service not properly configured")
                    _workoutPlanState.value = WorkoutPlanState.Error("AI service configuration error")
                    return@launch
                }
                
                android.util.Log.d("AIWorkoutViewModel", "📝 Generating plan: Goals=${goals.joinToString()}, Level=$fitnessLevel, Time=${availableTime}min")
                
                val result = deepSeekService.generateWorkoutPlan(
                    goals = goals,
                    fitnessLevel = fitnessLevel,
                    availableTime = availableTime,
                    equipment = equipment
                )
                
                result.fold(
                    onSuccess = { rawPlan ->
                        android.util.Log.d("AIWorkoutViewModel", "✅ Successfully generated workout plan (${rawPlan.length} chars)")
                        _workoutPlanState.value = WorkoutPlanState.Success(rawPlan)
                    },
                    onFailure = { error ->
                        android.util.Log.e("AIWorkoutViewModel", "❌ Workout generation failed: ${error.message}")
                        
                        // Provide user-friendly error messages
                        val userFriendlyMessage = when {
                            error.message?.contains("authentication", ignoreCase = true) == true -> 
                                "Authentication error. Please try again later."
                            error.message?.contains("rate limit", ignoreCase = true) == true -> 
                                "Too many requests. Please wait a moment and try again."
                            error.message?.contains("network", ignoreCase = true) == true || 
                            error.message?.contains("connection", ignoreCase = true) == true -> 
                                "Network connection issue. Please check your internet and try again."
                            error.message?.contains("timeout", ignoreCase = true) == true -> 
                                "Request timed out. Please try again."
                            else -> "Unable to generate workout plan. Please try again."
                        }
                        
                        _workoutPlanState.value = WorkoutPlanState.Error(userFriendlyMessage)
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AIWorkoutViewModel", "💥 Unexpected error during workout generation", e)
                _workoutPlanState.value = WorkoutPlanState.Error("An unexpected error occurred. Please try again.")
            }
        }
    }
    
    // Additional utility methods for better UX
    fun generateQuickWorkout(duration: Int = 15) {
        android.util.Log.d("AIWorkoutViewModel", "⚡ Generating quick $duration-minute workout...")
        _workoutPlanState.value = WorkoutPlanState.Loading
        
        viewModelScope.launch {
            try {
                val result = deepSeekService.generateQuickWorkout(duration)
                
                result.fold(
                    onSuccess = { plan ->
                        android.util.Log.d("AIWorkoutViewModel", "✅ Quick workout generated successfully")
                        _workoutPlanState.value = WorkoutPlanState.Success(plan)
                    },
                    onFailure = { error ->
                        android.util.Log.e("AIWorkoutViewModel", "❌ Quick workout generation failed: ${error.message}")
                        _workoutPlanState.value = WorkoutPlanState.Error("Unable to generate quick workout. Please try again.")
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AIWorkoutViewModel", "💥 Error generating quick workout", e)
                _workoutPlanState.value = WorkoutPlanState.Error("An error occurred. Please try again.")
            }
        }
    }
    
    fun resetState() {
        android.util.Log.d("AIWorkoutViewModel", "🔄 Resetting workout plan state")
        _workoutPlanState.value = WorkoutPlanState.Initial
    }
    
    // Test method for verifying DeepSeek connectivity
    fun testDeepSeekConnection() {
        android.util.Log.d("AIWorkoutViewModel", "🧪 Testing DeepSeek API connection...")
        _workoutPlanState.value = WorkoutPlanState.Loading
        
        viewModelScope.launch {
            try {
                // First test basic API connectivity
                android.util.Log.d("AIWorkoutViewModel", "🔌 Testing basic API connectivity...")
                val testResult = deepSeekService.testApiConnection()
                
                testResult.fold(
                    onSuccess = { message ->
                        android.util.Log.d("AIWorkoutViewModel", "✅ API Connection Test Passed: $message")
                        _workoutPlanState.value = WorkoutPlanState.Success("🎉 API Connection Test Successful!\n\n$message")
                    },
                    onFailure = { error ->
                        android.util.Log.e("AIWorkoutViewModel", "❌ API Connection Test Failed: ${error.message}")
                        _workoutPlanState.value = WorkoutPlanState.Error("API Connection Test Failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                android.util.Log.e("AIWorkoutViewModel", "🚨 DeepSeek connection test crashed", e)
                _workoutPlanState.value = WorkoutPlanState.Error("Connection test crashed: ${e.message}")
            }
        }
    }
}

sealed class WorkoutPlanState {
    object Initial : WorkoutPlanState()
    object Loading : WorkoutPlanState()
    data class Success(val plan: String) : WorkoutPlanState()
    data class Error(val message: String) : WorkoutPlanState()
}
