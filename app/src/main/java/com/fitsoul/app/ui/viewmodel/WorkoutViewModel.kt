package com.fitsoul.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitsoul.app.data.repository.WorkoutRepository
import com.fitsoul.app.ui.screens.workout.AIWorkout
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    
    private val TAG = "WorkoutViewModel"
    
    // Get all saved workouts
    val savedWorkouts = workoutRepository.savedWorkouts
    
    // Save success state
    private val _saveWorkoutResult = MutableStateFlow<String?>(null)
    val saveWorkoutResult = _saveWorkoutResult.asStateFlow()
    
    // Get a specific workout by ID
    fun getWorkoutById(workoutId: String): Flow<AIWorkout?> {
        return workoutRepository.savedWorkouts.map { workouts ->
            workouts.find { it.id == workoutId }
        }
    }
    
    // Save a workout from AI content
    fun saveWorkoutFromAI(content: String) {
        viewModelScope.launch {
            try {
                val savedWorkout = workoutRepository.saveWorkoutFromAI(content)
                _saveWorkoutResult.value = "✅ Saved: ${savedWorkout.name}"
                android.util.Log.d(TAG, "💾 Successfully saved workout: ${savedWorkout.name}")
                
                // Clear the message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _saveWorkoutResult.value = null
            } catch (e: Exception) {
                _saveWorkoutResult.value = "❌ Failed to save workout"
                android.util.Log.e(TAG, "💥 Error saving workout: ${e.message}")
                
                // Clear the message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _saveWorkoutResult.value = null
            }
        }
    }
    
    // Save a custom workout
    fun saveWorkout(workout: AIWorkout) {
        viewModelScope.launch {
            try {
                val savedWorkout = workoutRepository.saveWorkout(workout)
                _saveWorkoutResult.value = "✅ Saved: ${savedWorkout.name}"
                android.util.Log.d(TAG, "💾 Successfully saved custom workout: ${savedWorkout.name}")
                
                // Clear the message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _saveWorkoutResult.value = null
            } catch (e: Exception) {
                _saveWorkoutResult.value = "❌ Failed to save workout"
                android.util.Log.e(TAG, "💥 Error saving custom workout: ${e.message}")
                
                // Clear the message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _saveWorkoutResult.value = null
            }
        }
    }
    
    // Delete a workout
    fun deleteWorkout(workoutId: String) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workoutId)
                _saveWorkoutResult.value = "🗑️ Workout deleted"
                android.util.Log.d(TAG, "🗑️ Successfully deleted workout: $workoutId")
                
                // Clear the message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _saveWorkoutResult.value = null
            } catch (e: Exception) {
                _saveWorkoutResult.value = "❌ Failed to delete workout"
                android.util.Log.e(TAG, "💥 Error deleting workout: ${e.message}")
                
                // Clear the message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _saveWorkoutResult.value = null
            }
        }
    }
    
    // Clear save result message
    fun clearSaveResult() {
        _saveWorkoutResult.value = null
    }
}