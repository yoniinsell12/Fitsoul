package com.fitsoul.app.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fitsoul.app.ui.screens.workout.AIWorkout
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("workouts")
private val SAVED_WORKOUTS_KEY = stringPreferencesKey("saved_workouts")

@Singleton
class WorkoutRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "WorkoutRepository"
    private val json = Json { ignoreUnknownKeys = true }
    
    // Get all saved workouts as Flow
    val savedWorkouts: Flow<List<AIWorkout>> = context.dataStore.data.map { preferences ->
        val workoutsJson = preferences[SAVED_WORKOUTS_KEY] ?: "[]"
        try {
            val workouts: List<AIWorkout> = json.decodeFromString(workoutsJson)
            Log.d(TAG, "✅ Loaded ${workouts.size} saved workouts")
            workouts
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error loading workouts: ${e.message}")
            // Return default sample workouts if loading fails
            getSampleWorkouts()
        }
    }
    
    // Save a workout from AI content
    suspend fun saveWorkoutFromAI(content: String): AIWorkout {
        Log.d(TAG, "💾 Saving workout from AI content: ${content.take(100)}...")
        
        val workout = parseWorkoutFromAI(content)
        return saveWorkout(workout)
    }
    
    // Save a workout
    suspend fun saveWorkout(workout: AIWorkout): AIWorkout {
        context.dataStore.edit { preferences ->
            val currentWorkoutsJson = preferences[SAVED_WORKOUTS_KEY] ?: "[]"
            val currentWorkouts: List<AIWorkout> = try {
                json.decodeFromString(currentWorkoutsJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedWorkouts = listOf(workout) + currentWorkouts // Add to beginning
            preferences[SAVED_WORKOUTS_KEY] = json.encodeToString(updatedWorkouts)
            
            Log.d(TAG, "✅ Saved workout: ${workout.name}")
        }
        
        return workout
    }
    
    // Delete a workout
    suspend fun deleteWorkout(workoutId: String) {
        context.dataStore.edit { preferences ->
            val currentWorkoutsJson = preferences[SAVED_WORKOUTS_KEY] ?: "[]"
            val currentWorkouts: List<AIWorkout> = try {
                json.decodeFromString(currentWorkoutsJson)
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedWorkouts = currentWorkouts.filter { it.id != workoutId }
            preferences[SAVED_WORKOUTS_KEY] = json.encodeToString(updatedWorkouts)
            
            Log.d(TAG, "🗑️ Deleted workout: $workoutId")
        }
    }
    
    // Parse AI content into a structured workout
    private fun parseWorkoutFromAI(content: String): AIWorkout {
        Log.d(TAG, "🔍 Parsing AI workout content...")
        
        // Extract workout name from content
        val titleRegex = """(?:^|\n)\s*(?:🔥|💪|🏋️|📋|Workout:\s*)?([A-Z][^.\n]{10,80})""".toRegex()
        val titleMatch = titleRegex.find(content)
        val name = titleMatch?.groupValues?.get(1)?.trim() ?: "AI Generated Workout"
        
        // Extract exercises
        val exerciseRegex = """(?:•|-|▪|[0-9]+\.)\s*([A-Za-z][^:\n]{5,50})""".toRegex()
        val exercises = exerciseRegex.findAll(content)
            .map { it.groupValues[1].trim() }
            .filter { it.length > 3 }
            .take(10)
            .toList()
        
        // Extract muscle groups
        val muscleRegex = """(chest|back|legs|arms|shoulders|core|abs|cardio|full body|glutes|biceps|triceps)""".toRegex(RegexOption.IGNORE_CASE)
        val targetMuscleGroups = muscleRegex.findAll(content)
            .map { it.value.lowercase().replaceFirstChar { char -> char.uppercaseChar() } }
            .distinct()
            .take(3)
            .toList()
            .ifEmpty { listOf("Full Body") }
        
        // Extract duration (look for numbers followed by "min" or "minutes")
        val durationRegex = """(\d+)\s*(?:min|minutes?)""".toRegex(RegexOption.IGNORE_CASE)
        val durationMatch = durationRegex.find(content)
        val duration = durationMatch?.groupValues?.get(1)?.toIntOrNull() ?: when {
            exercises.size <= 4 -> 20
            exercises.size <= 6 -> 30
            exercises.size <= 8 -> 45
            else -> 60
        }
        
        // Extract difficulty
        val difficultyRegex = """(beginner|intermediate|advanced)""".toRegex(RegexOption.IGNORE_CASE)
        val difficultyMatch = difficultyRegex.find(content)
        val difficulty = difficultyMatch?.value?.replaceFirstChar { it.uppercaseChar() } ?: "Intermediate"
        
        // Estimate calories
        val caloriesEstimate = when (duration) {
            in 0..20 -> 150
            in 21..35 -> 250
            in 36..50 -> 350
            else -> 450
        }
        
        // Create description from first few sentences
        val descriptionMatch = content.split("\n")
            .find { it.trim().length > 20 && !it.contains("🔥") && !it.contains("💪") }
        val description = descriptionMatch?.trim()?.take(120) ?: "AI-generated personalized workout plan"
        
        val workout = AIWorkout(
            id = UUID.randomUUID().toString(),
            name = name.take(60),
            description = description,
            duration = duration,
            difficulty = difficulty,
            exercises = exercises.ifEmpty { listOf("Push-ups", "Squats", "Plank", "Jumping Jacks") },
            targetMuscleGroups = targetMuscleGroups,
            caloriesEstimate = caloriesEstimate,
            dateGenerated = System.currentTimeMillis()
        )
        
        Log.d(TAG, "✅ Parsed workout: ${workout.name} (${workout.duration}min, ${workout.difficulty})")
        return workout
    }
    
    // Get sample workouts for initial state
    private fun getSampleWorkouts() = listOf(
        AIWorkout(
            id = "sample_1",
            name = "Morning Energy Boost",
            description = "Wake up your body with this energizing routine",
            duration = 20,
            difficulty = "Beginner",
            exercises = listOf("Jumping Jacks", "Push-ups", "Squats", "Plank"),
            targetMuscleGroups = listOf("Full Body", "Cardio"),
            caloriesEstimate = 150,
            completionCount = 5,
            lastCompleted = System.currentTimeMillis() - 86400000
        ),
        AIWorkout(
            id = "sample_2",
            name = "Strength Builder Pro",
            description = "Build serious strength with compound movements",
            duration = 45,
            difficulty = "Advanced",
            exercises = listOf("Deadlifts", "Squats", "Bench Press", "Pull-ups", "Overhead Press"),
            targetMuscleGroups = listOf("Chest", "Back", "Legs", "Arms"),
            caloriesEstimate = 400,
            completionCount = 3,
            lastCompleted = System.currentTimeMillis() - 172800000
        ),
        AIWorkout(
            id = "sample_3",
            name = "Cardio Blast HIIT",
            description = "High-intensity cardio for maximum burn",
            duration = 25,
            difficulty = "Intermediate",
            exercises = listOf("Burpees", "Mountain Climbers", "High Knees", "Jump Squats", "Sprint Intervals"),
            targetMuscleGroups = listOf("Cardio", "Legs", "Core"),
            caloriesEstimate = 300,
            completionCount = 8,
            lastCompleted = System.currentTimeMillis() - 259200000
        )
    )
}