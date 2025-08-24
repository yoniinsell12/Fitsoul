package com.fitsoul.app.data.service

import android.util.Log
import com.fitsoul.app.data.ai.DeepSeekService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class WorkoutProgress(
    val currentExercise: String = "",
    val exerciseIndex: Int = 0,
    val totalExercises: Int = 0,
    val currentSet: Int = 0,
    val totalSets: Int = 0,
    val timeElapsed: Long = 0,
    val caloriesBurned: Int = 0,
    val heartRate: Int = 0,
    val isResting: Boolean = false,
    val restTimeRemaining: Int = 0
)

data class WorkoutStats(
    val totalWorkouts: Int = 0,
    val totalDuration: Long = 0,
    val averageCaloriesPerWorkout: Int = 0,
    val favoriteWorkoutType: String = "",
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCaloriesBurned: Int = 0,
    val strengthPR: Map<String, Float> = emptyMap(),
    val cardioRecords: Map<String, Float> = emptyMap()
)

data class FitnessMetrics(
    val steps: Int = 0,
    val distance: Float = 0f,
    val activeMinutes: Int = 0,
    val heartRate: Int = 0,
    val sleepHours: Float = 0f,
    val caloriesBurned: Int = 0,
    val waterIntake: Float = 0f,
    val mood: Int = 5 // 1-10 scale
)

@Singleton
class FitnessTrackingService @Inject constructor(
    private val deepSeekService: DeepSeekService
) {
    private val TAG = "FitnessTrackingService"
    
    private val _workoutProgress = MutableStateFlow(WorkoutProgress())
    val workoutProgress: StateFlow<WorkoutProgress> = _workoutProgress.asStateFlow()
    
    private val _workoutStats = MutableStateFlow(WorkoutStats())
    val workoutStats: StateFlow<WorkoutStats> = _workoutStats.asStateFlow()
    
    private val _dailyMetrics = MutableStateFlow(FitnessMetrics())
    val dailyMetrics: StateFlow<FitnessMetrics> = _dailyMetrics.asStateFlow()
    
    private var workoutStartTime: Long = 0
    private var isWorkoutActive = false
    
    // Premium AI-powered features
    
    suspend fun generatePersonalizedWorkout(
        fitnessGoals: List<String>,
        fitnessLevel: String,
        availableTime: Int,
        preferredEquipment: List<String> = emptyList(),
        targetMuscleGroups: List<String> = emptyList()
    ): String {
        return try {
            Log.d(TAG, "🚀 Generating personalized workout with DeepSeek AI")
            
            // Try DeepSeek first (our primary AI service)
            if (deepSeekService.isConfigured()) {
                try {
                    Log.d(TAG, "💪 Using DeepSeek for workout generation...")
                    val result = deepSeekService.generateWorkoutPlan(
                        goals = fitnessGoals,
                        fitnessLevel = fitnessLevel,
                        availableTime = availableTime,
                        equipment = preferredEquipment
                    )
                    
                    result.fold(
                        onSuccess = { plan ->
                            Log.d(TAG, "✅ DeepSeek generated successful workout plan")
                            return plan
                        },
                        onFailure = { error ->
                            Log.w(TAG, "⚠️ DeepSeek failed, using enhanced static fallback: ${error.message}")
                            return@fold generateEnhancedFallbackWorkout(fitnessGoals, fitnessLevel, availableTime, preferredEquipment)
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ DeepSeek error, using enhanced static fallback", e)
                    return generateEnhancedFallbackWorkout(fitnessGoals, fitnessLevel, availableTime, preferredEquipment)
                }
            } else {
                Log.w(TAG, "⚠️ DeepSeek not configured, using enhanced static workout")
                return generateEnhancedFallbackWorkout(fitnessGoals, fitnessLevel, availableTime, preferredEquipment)
            }
            
            // This should not be reached, but just in case
            return generateEnhancedFallbackWorkout(fitnessGoals, fitnessLevel, availableTime, preferredEquipment)
        } catch (e: Exception) {
            Log.e(TAG, "💥 Complete failure generating workout", e)
            generateEnhancedFallbackWorkout(fitnessGoals, fitnessLevel, availableTime, preferredEquipment)
        }
    }
    
    suspend fun getAIFormFeedback(exerciseName: String): String {
        return try {
            Log.d(TAG, "🎯 Getting form feedback for: $exerciseName")
            
            // Try DeepSeek first
            if (deepSeekService.isConfigured()) {
                try {
                    Log.d(TAG, "💪 Using DeepSeek for form tips...")
                    val result = deepSeekService.generateFormTips(exerciseName)
                    
                    result.fold(
                        onSuccess = { tips ->
                            Log.d(TAG, "✅ DeepSeek generated form tips successfully")
                            return tips
                        },
                        onFailure = { error ->
                            Log.w(TAG, "⚠️ DeepSeek form tips failed, using enhanced static tips: ${error.message}")
                            return@fold generateEnhancedFormTips(exerciseName)
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ DeepSeek form tips error, using enhanced static fallback", e)
                    return generateEnhancedFormTips(exerciseName)
                }
            }
            
            // If DeepSeek is not configured, use enhanced static form tips
            Log.d(TAG, "🛡️ DeepSeek not available, using enhanced static form tips")
            return generateEnhancedFormTips(exerciseName)
        } catch (e: Exception) {
            Log.e(TAG, "💥 Complete failure getting form feedback", e)
            generateEnhancedFormTips(exerciseName)
        }
    }
    
    suspend fun getWorkoutRecommendations(currentStats: WorkoutStats): String {
        return try {
            Log.d(TAG, "🎯 Generating workout recommendations based on stats")
            
            // Try DeepSeek for intelligent recommendations if configured
            if (deepSeekService.isConfigured()) {
                // Note: DeepSeek service doesn't have a specific recommendations method yet,
                // so we'll use the enhanced static recommendations for now
                Log.d(TAG, "🛡️ Using enhanced static recommendations")
                generateFallbackRecommendations(currentStats)
            } else {
                Log.d(TAG, "🛡️ DeepSeek not configured, using enhanced static recommendations")
                generateFallbackRecommendations(currentStats)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get workout recommendations", e)
            generateFallbackRecommendations(currentStats)
        }
    }
    
    // Workout tracking functions
    
    fun startWorkout(workoutName: String, totalExercises: Int) {
        workoutStartTime = System.currentTimeMillis()
        isWorkoutActive = true
        
        _workoutProgress.value = WorkoutProgress(
            currentExercise = workoutName,
            exerciseIndex = 0,
            totalExercises = totalExercises,
            currentSet = 1,
            totalSets = 3, // Default
            timeElapsed = 0
        )
        
        Log.d(TAG, "Started workout: $workoutName")
    }
    
    fun updateWorkoutProgress(
        exerciseIndex: Int,
        currentSet: Int,
        heartRate: Int = 0,
        isResting: Boolean = false,
        restTime: Int = 0
    ) {
        if (!isWorkoutActive) return
        
        val timeElapsed = System.currentTimeMillis() - workoutStartTime
        val estimatedCalories = calculateCaloriesBurned(timeElapsed, heartRate)
        
        _workoutProgress.value = _workoutProgress.value.copy(
            exerciseIndex = exerciseIndex,
            currentSet = currentSet,
            timeElapsed = timeElapsed,
            caloriesBurned = estimatedCalories,
            heartRate = heartRate,
            isResting = isResting,
            restTimeRemaining = restTime
        )
        
        // Update daily metrics
        _dailyMetrics.value = _dailyMetrics.value.copy(
            caloriesBurned = _dailyMetrics.value.caloriesBurned + (estimatedCalories - _workoutProgress.value.caloriesBurned),
            activeMinutes = _dailyMetrics.value.activeMinutes + ((timeElapsed / 1000 / 60).toInt()),
            heartRate = if (heartRate > 0) heartRate else _dailyMetrics.value.heartRate
        )
    }
    
    fun completeWorkout() {
        if (!isWorkoutActive) return
        
        val totalTime = System.currentTimeMillis() - workoutStartTime
        val currentStats = _workoutStats.value
        
        // Update workout statistics
        _workoutStats.value = currentStats.copy(
            totalWorkouts = currentStats.totalWorkouts + 1,
            totalDuration = currentStats.totalDuration + totalTime,
            totalCaloriesBurned = currentStats.totalCaloriesBurned + _workoutProgress.value.caloriesBurned,
            currentStreak = currentStats.currentStreak + 1,
            longestStreak = maxOf(currentStats.longestStreak, currentStats.currentStreak + 1),
            averageCaloriesPerWorkout = ((currentStats.totalCaloriesBurned + _workoutProgress.value.caloriesBurned) / (currentStats.totalWorkouts + 1))
        )
        
        isWorkoutActive = false
        Log.d(TAG, "Completed workout. Total time: ${totalTime / 1000} seconds")
    }
    
    // Fitness metrics tracking
    
    fun updateDailyMetrics(
        steps: Int? = null,
        distance: Float? = null,
        heartRate: Int? = null,
        sleepHours: Float? = null,
        waterIntake: Float? = null,
        mood: Int? = null
    ) {
        _dailyMetrics.value = _dailyMetrics.value.copy(
            steps = steps ?: _dailyMetrics.value.steps,
            distance = distance ?: _dailyMetrics.value.distance,
            heartRate = heartRate ?: _dailyMetrics.value.heartRate,
            sleepHours = sleepHours ?: _dailyMetrics.value.sleepHours,
            waterIntake = waterIntake ?: _dailyMetrics.value.waterIntake,
            mood = mood ?: _dailyMetrics.value.mood
        )
        
        Log.d(TAG, "Updated daily metrics: ${_dailyMetrics.value}")
    }
    
    fun addStrengthRecord(exercise: String, weight: Float) {
        val currentStats = _workoutStats.value
        val updatedPRs = currentStats.strengthPR.toMutableMap()
        
        if (weight > (updatedPRs[exercise] ?: 0f)) {
            updatedPRs[exercise] = weight
            _workoutStats.value = currentStats.copy(strengthPR = updatedPRs)
            Log.d(TAG, "New PR for $exercise: ${weight}lbs")
        }
    }
    
    fun addCardioRecord(activity: String, performance: Float) {
        val currentStats = _workoutStats.value
        val updatedRecords = currentStats.cardioRecords.toMutableMap()
        
        if (performance > (updatedRecords[activity] ?: 0f)) {
            updatedRecords[activity] = performance
            _workoutStats.value = currentStats.copy(cardioRecords = updatedRecords)
            Log.d(TAG, "New cardio record for $activity: $performance")
        }
    }
    
    // Helper functions
    
    private fun calculateCaloriesBurned(timeElapsed: Long, heartRate: Int): Int {
        // Simplified calorie calculation
        val minutes = timeElapsed / 1000 / 60
        val baseRate = when {
            heartRate > 150 -> 12 // High intensity
            heartRate > 120 -> 8  // Moderate intensity
            heartRate > 90 -> 6   // Low intensity
            else -> 4             // Very light
        }
        return (minutes * baseRate).toInt()
    }
    
    private fun generateFallbackWorkout(goals: List<String>, level: String, time: Int): String {
        return generateEnhancedFallbackWorkout(goals, level, time, emptyList())
    }
    
    private fun generateEnhancedFallbackWorkout(
        goals: List<String>, 
        level: String, 
        time: Int,
        equipment: List<String>
    ): String {
        val equipmentAvailable = equipment.isNotEmpty()
        val mainWorkoutTime = time - 10
        
        val warmupExercises = listOf(
            "Arm circles: 30 seconds",
            "Leg swings: 30 seconds each leg", 
            "Jumping jacks: 1 minute",
            "Dynamic stretching: 2 minutes"
        )
        
        val exercises = when {
            goals.any { it.contains("strength", ignoreCase = true) } && equipmentAvailable -> 
                getStrengthExercises(level, equipment)
            goals.any { it.contains("cardio", ignoreCase = true) || it.contains("endurance", ignoreCase = true) } -> 
                getCardioExercises(level)
            goals.any { it.contains("weight", ignoreCase = true) || it.contains("fat", ignoreCase = true) } -> 
                getWeightLossExercises(level)
            else -> getGeneralFitnessExercises(level)
        }
        
        val cooldownExercises = listOf(
            "Forward fold stretch: 30 seconds",
            "Quad stretch: 30 seconds each leg",
            "Shoulder stretch: 30 seconds each arm",
            "Deep breathing: 2 minutes"
        )
        
        return """
            🏋️‍♀️ FitSoul Personalized Workout
            
            🎯 YOUR PROFILE:
            • Goals: ${goals.joinToString(", ")}
            • Level: $level
            • Duration: $time minutes
            • Equipment: ${if (equipmentAvailable) equipment.joinToString(", ") else "Bodyweight only"}
            
            🔥 WARM-UP (5 minutes)
            ${warmupExercises.joinToString("\n") { "• $it" }}
            
            💪 MAIN WORKOUT ($mainWorkoutTime minutes)
            Complete ${if (level == "Beginner") 2 else if (level == "Intermediate") 3 else 4} rounds:
            
            ${exercises.joinToString("\n") { "• $it" }}
            
            Rest: ${if (level == "Beginner") "60-90" else if (level == "Intermediate") "45-60" else "30-45"} seconds between exercises
            Rest: ${if (level == "Beginner") "2-3" else if (level == "Intermediate") "1.5-2" else "1-1.5"} minutes between rounds
            
            🧘‍♀️ COOL-DOWN (5 minutes)
            ${cooldownExercises.joinToString("\n") { "• $it" }}
            
            💡 EXPERT TIPS:
            • Focus on controlled movements and proper form
            • Breathe consistently - exhale on exertion
            • Stay hydrated throughout your workout
            • ${getGoalSpecificTip(goals)}
            • Track your reps and sets for progression
            
            🌟 You've got this! Every rep counts toward your goals!
        """.trimIndent()
    }
    
    private fun getStrengthExercises(level: String, equipment: List<String>): List<String> {
        val hasWeights = equipment.any { it.contains("dumbbell", true) || it.contains("barbell", true) }
        val hasBands = equipment.any { it.contains("band", true) }
        
        return when {
            hasWeights -> listOf(
                "Dumbbell squats: ${getRepsForLevel(level, "squats")}",
                "Dumbbell chest press: ${getRepsForLevel(level, "press")}",
                "Dumbbell rows: ${getRepsForLevel(level, "rows")}",
                "Dumbbell overhead press: ${getRepsForLevel(level, "press")}"
            )
            hasBands -> listOf(
                "Band squats: ${getRepsForLevel(level, "squats")}",
                "Band chest press: ${getRepsForLevel(level, "press")}",
                "Band rows: ${getRepsForLevel(level, "rows")}",
                "Band shoulder press: ${getRepsForLevel(level, "press")}"
            )
            else -> listOf(
                "Push-ups: ${getRepsForLevel(level, "push-ups")}",
                "Squats: ${getRepsForLevel(level, "squats")}",
                "Pike push-ups: ${getRepsForLevel(level, "push-ups")}",
                "Single-leg glute bridges: ${getRepsForLevel(level, "bridges")} per leg"
            )
        }
    }
    
    private fun getCardioExercises(level: String): List<String> {
        return listOf(
            "Burpees: ${if (level == "Beginner") "5-8" else if (level == "Intermediate") "8-12" else "12-15"}",
            "Mountain climbers: ${getTimeForLevel(level)}",
            "Jump squats: ${getRepsForLevel(level, "squats")}",
            "High knees: ${getTimeForLevel(level)}"
        )
    }
    
    private fun getWeightLossExercises(level: String): List<String> {
        return listOf(
            "Burpees: ${if (level == "Beginner") "5-8" else if (level == "Intermediate") "8-10" else "10-15"}",
            "Squat to calf raise: ${getRepsForLevel(level, "squats")}",
            "Push-up to T: ${getRepsForLevel(level, "push-ups")}",
            "Plank to downward dog: ${if (level == "Beginner") "8-10" else if (level == "Intermediate") "10-12" else "12-15"}"
        )
    }
    
    private fun getGeneralFitnessExercises(level: String): List<String> {
        return listOf(
            "Push-ups: ${getRepsForLevel(level, "push-ups")}",
            "Squats: ${getRepsForLevel(level, "squats")}",
            "Plank: ${getTimeForLevel(level)}",
            "Lunges: ${getRepsForLevel(level, "lunges")} per leg"
        )
    }
    
    private fun getGoalSpecificTip(goals: List<String>): String {
        return when {
            goals.any { it.contains("strength", ignoreCase = true) } -> 
                "Focus on progressive overload - gradually increase weight or reps"
            goals.any { it.contains("weight", ignoreCase = true) } -> 
                "Maintain a slight calorie deficit and combine with cardio"
            goals.any { it.contains("endurance", ignoreCase = true) } -> 
                "Gradually increase workout duration and intensity"
            goals.any { it.contains("muscle", ignoreCase = true) } -> 
                "Eat adequate protein and allow proper rest between sessions"
            else -> "Consistency is key - aim for 3-4 workouts per week"
        }
    }
    
    private fun generateFallbackFormTips(exercise: String): String {
        return generateEnhancedFormTips(exercise)
    }
    
    private fun generateEnhancedFormTips(exercise: String): String {
        val exerciseLower = exercise.lowercase()
        
        return when {
            exerciseLower.contains("push-up") -> """
                🏋️ Perfect Form: Push-ups
                
                ✅ SETUP:
                • Start in plank position, hands slightly wider than shoulders
                • Keep body in straight line from head to heels
                • Engage core and glutes throughout movement
                
                🎯 EXECUTION:
                • Lower chest toward ground with control (2-3 seconds)
                • Push up explosively while maintaining form (1 second)
                • Keep elbows at 45-degree angle to body
                • Full range of motion - chest touches ground
                
                ⚠️ COMMON MISTAKES:
                • Sagging hips or piking up
                • Flaring elbows out too wide
                • Partial range of motion
                
                💡 PRO TIPS:
                • Squeeze shoulder blades at bottom
                • Breathe in going down, out going up
                • Modify on knees if needed
            """.trimIndent()
            
            exerciseLower.contains("squat") -> """
                🏋️ Perfect Form: Squats
                
                ✅ SETUP:
                • Feet shoulder-width apart, toes slightly turned out
                • Chest up, shoulders back, core braced
                • Weight evenly distributed across feet
                
                🎯 EXECUTION:
                • Initiate by pushing hips back (like sitting in chair)
                • Lower until thighs parallel to ground
                • Drive through heels to return to standing
                • Keep knees tracking over toes
                
                ⚠️ COMMON MISTAKES:
                • Knees caving inward
                • Rising on toes/heels coming up
                • Rounding back or looking down
                
                💡 PRO TIPS:
                • Keep weight in heels and mid-foot
                • Pretend you're sitting back into a chair
                • Go as deep as mobility allows with good form
            """.trimIndent()
            
            exerciseLower.contains("plank") -> """
                🏋️ Perfect Form: Plank
                
                ✅ SETUP:
                • Forearms on ground, elbows directly under shoulders
                • Legs extended, balancing on toes
                • Body forms straight line from head to heels
                
                🎯 EXECUTION:
                • Engage core by pulling belly button to spine
                • Squeeze glutes and keep legs straight
                • Maintain neutral spine - don't look up or down
                • Breathe normally throughout hold
                
                ⚠️ COMMON MISTAKES:
                • Sagging hips below straight line
                • Piking hips up too high
                • Holding breath
                
                💡 PRO TIPS:
                • Focus on quality over duration
                • Start with shorter holds (15-30 seconds)
                • Imagine balancing a glass of water on your back
            """.trimIndent()
            
            exerciseLower.contains("lunge") -> """
                🏋️ Perfect Form: Lunges
                
                ✅ SETUP:
                • Stand tall with feet hip-width apart
                • Hands on hips or at sides
                • Engage core for stability
                
                🎯 EXECUTION:
                • Step forward with one leg (large step)
                • Lower hips until both knees at 90 degrees
                • Front thigh parallel to ground, back knee nearly touches floor
                • Push through front heel to return to start
                
                ⚠️ COMMON MISTAKES:
                • Step too short or too long
                • Leaning forward over front leg
                • Pushing off back foot instead of front
                
                💡 PRO TIPS:
                • Keep most weight on front leg
                • Step straight down, not forward on return
                • Control the descent for maximum benefit
            """.trimIndent()
            
            else -> """
                🏋️ Perfect Form: $exercise
                
                ✅ FUNDAMENTAL PRINCIPLES:
                • Maintain proper posture and alignment
                • Control the movement in both directions
                • Engage your core throughout
                • Use full range of motion when possible
                
                🎯 BREATHING PATTERN:
                • Exhale during the exertion phase
                • Inhale during the lowering/easier phase
                • Never hold your breath during exercise
                
                ⚠️ SAFETY REMINDERS:
                • Quality always trumps quantity
                • Stop if you feel sharp pain
                • Warm up before and stretch after
                
                💡 PROGRESSION TIPS:
                • Master bodyweight before adding resistance
                • Gradually increase difficulty over time
                • Focus on consistency rather than perfection
            """.trimIndent()
        }
    }
    
    private fun generateFallbackRecommendations(stats: WorkoutStats): String {
        return """
            📊 Your Fitness Analysis
            
            🎯 Current Progress:
            • Workouts completed: ${stats.totalWorkouts}
            • Current streak: ${stats.currentStreak} days
            • Average calories: ${stats.averageCaloriesPerWorkout} per workout
            
            💡 AI Recommendations:
            • Try increasing workout intensity for better results
            • Consider adding variety to prevent plateaus
            • Focus on consistency over perfection
            • Set progressive goals to stay motivated
            
            Keep up the great work! 🌟
        """.trimIndent()
    }
    
    private fun getRepsForLevel(level: String, exercise: String): String {
        return when (level.lowercase()) {
            "beginner" -> when (exercise) {
                "push-ups" -> "5-10"
                "squats" -> "10-15"
                "lunges" -> "8-12"
                "bridges" -> "10-15"
                "press" -> "8-12"
                "rows" -> "8-12"
                else -> "8-12"
            }
            "intermediate" -> when (exercise) {
                "push-ups" -> "10-18"
                "squats" -> "15-25"
                "lunges" -> "12-18"
                "bridges" -> "15-20"
                "press" -> "12-18"
                "rows" -> "12-18"
                else -> "12-18"
            }
            "advanced" -> when (exercise) {
                "push-ups" -> "18-25"
                "squats" -> "25-35"
                "lunges" -> "18-25"
                "bridges" -> "20-30"
                "press" -> "15-25"
                "rows" -> "15-25"
                else -> "18-25"
            }
            else -> "10-15"
        }
    }
    
    private fun getTimeForLevel(level: String): String {
        return when (level.lowercase()) {
            "beginner" -> "20-30 seconds"
            "intermediate" -> "30-45 seconds"
            "advanced" -> "45-60 seconds"
            else -> "30 seconds"
        }
    }
}