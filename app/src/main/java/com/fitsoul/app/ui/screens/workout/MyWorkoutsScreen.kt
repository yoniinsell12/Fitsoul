package com.fitsoul.app.ui.screens.workout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.data.service.FitnessTrackingService
import com.fitsoul.app.ui.components.*
import com.fitsoul.app.ui.viewmodel.AuthViewModel
import com.fitsoul.app.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class AIWorkout(
    val id: String,
    val name: String,
    val description: String,
    val duration: Int, // minutes
    val difficulty: String,
    val exercises: List<String>,
    val targetMuscleGroups: List<String>,
    val caloriesEstimate: Int,
    val isGenerating: Boolean = false,
    val dateGenerated: Long = System.currentTimeMillis(),
    val completionCount: Int = 0,
    val lastCompleted: Long? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyWorkoutsScreen(
    onWorkoutClick: (AIWorkout) -> Unit = {},
    onGenerateNewWorkout: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    workoutViewModel: WorkoutViewModel = hiltViewModel()
) {
    val savedWorkouts by workoutViewModel.savedWorkouts.collectAsState(initial = emptyList())
    var isGeneratingWorkout by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Use saved workouts from ViewModel, fallback to sample data if empty
    val workouts = if (savedWorkouts.isNotEmpty()) savedWorkouts else getSampleWorkouts()
    
    // Mock states (replace with actual service when available)
    val workoutProgress by remember { mutableStateOf(com.fitsoul.app.data.service.WorkoutProgress()) }
    val workoutStats by remember { mutableStateOf(com.fitsoul.app.data.service.WorkoutStats()) }
    
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background)
    ) {
        // Header with stats
        MyWorkoutsHeader(
            totalWorkouts = workouts.size,
            completedWorkouts = workouts.sumOf { it.completionCount },
            currentStreak = workoutStats.currentStreak,
            onGenerateClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                generateNewWorkoutWithViewModel(
                    isGeneratingWorkout = isGeneratingWorkout,
                    onIsGeneratingUpdate = { isGeneratingWorkout = it },
                    coroutineScope = coroutineScope,
                    workoutViewModel = workoutViewModel
                )
            }
        )
        
        // Filter Tabs
        FilterTabs(
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )
        
        // Active Workout Banner
        AnimatedVisibility(
            visible = workoutProgress.currentExercise.isNotEmpty(),
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            ActiveWorkoutBanner(
                workoutProgress = workoutProgress,
                onResumeClick = { /* Navigate to workout detail */ }
            )
        }
        
        // Generating Workout Indicator
        AnimatedVisibility(
            visible = isGeneratingWorkout,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            GeneratingWorkoutCard()
        }
        
        // Workouts List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp) // Account for bottom nav
        ) {
            val filteredWorkouts = when (selectedFilter) {
                "Recent" -> workouts.sortedByDescending { it.dateGenerated }.take(10)
                "Favorites" -> workouts.filter { it.completionCount > 2 }
                "Strength" -> workouts.filter { it.targetMuscleGroups.any { muscle -> 
                    muscle.contains("arms", ignoreCase = true) || 
                    muscle.contains("chest", ignoreCase = true) || 
                    muscle.contains("back", ignoreCase = true) 
                }}
                "Cardio" -> workouts.filter { it.targetMuscleGroups.any { muscle ->
                    muscle.contains("cardio", ignoreCase = true) ||
                    muscle.contains("legs", ignoreCase = true)
                }}
                else -> workouts
            }
            
            items(filteredWorkouts) { workout ->
                WorkoutCard(
                    workout = workout,
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onWorkoutClick(workout) 
                    }
                )
            }
            
            if (filteredWorkouts.isEmpty()) {
                item {
                    EmptyStateCard(
                        filter = selectedFilter,
                        onGenerateClick = {
                            generateNewWorkoutWithViewModel(
                                isGeneratingWorkout = isGeneratingWorkout,
                                onIsGeneratingUpdate = { isGeneratingWorkout = it },
                                coroutineScope = coroutineScope,
                                workoutViewModel = workoutViewModel
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MyWorkoutsHeader(
    totalWorkouts: Int,
    completedWorkouts: Int,
    currentStreak: Int,
    onGenerateClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Workouts",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = FitsoulColors.TextPrimary
                    )
                    Text(
                        text = "AI-Generated & Personalized",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary
                    )
                }
                
                PremiumButton(
                    text = "Generate",
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onGenerateClick() // This calls the onGenerateClick parameter
                    },
                    icon = Icons.Default.AutoAwesome,
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Small
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickStatCard(
                    title = "Total",
                    value = totalWorkouts.toString(),
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Completed",
                    value = completedWorkouts.toString(),
                    icon = Icons.Default.Check,
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    title = "Streak",
                    value = "${currentStreak}d",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FitsoulColors.Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FitsoulColors.Primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = FitsoulColors.TextSecondary
            )
        }
    }
}

@Composable
fun FilterTabs(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val filters = listOf("All", "Recent", "Favorites", "Strength", "Cardio")
    
    ScrollableTabRow(
        selectedTabIndex = filters.indexOf(selectedFilter),
        modifier = Modifier.padding(horizontal = 16.dp),
        containerColor = Color.Transparent,
        contentColor = FitsoulColors.Primary,
        indicator = { },
        divider = { }
    ) {
        filters.forEach { filter ->
            Tab(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedFilter == filter) 
                            FitsoulColors.Primary 
                        else 
                            FitsoulColors.Surface
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = filter,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (selectedFilter == filter) 
                            Color.White 
                        else 
                            FitsoulColors.TextPrimary,
                        fontWeight = if (selectedFilter == filter) 
                            FontWeight.SemiBold 
                        else 
                            FontWeight.Normal
                    )
                }
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun ActiveWorkoutBanner(
    workoutProgress: com.fitsoul.app.data.service.WorkoutProgress,
    onResumeClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Primary.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = FitsoulColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Workout in Progress",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FitsoulColors.Primary
                    )
                }
                Text(
                    text = workoutProgress.currentExercise,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Set ${workoutProgress.currentSet} of ${workoutProgress.totalSets} • ${(workoutProgress.timeElapsed / 1000 / 60).toInt()}min",
                    style = MaterialTheme.typography.bodySmall,
                    color = FitsoulColors.TextSecondary
                )
            }
            
            PremiumButton(
                text = "Resume",
                onClick = onResumeClick,
                icon = Icons.Default.PlayArrow,
                variant = ButtonVariant.Secondary,
                size = ButtonSize.Small
            )
        }
    }
}

@Composable
fun GeneratingWorkoutCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = FitsoulColors.Primary,
                strokeWidth = 3.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "🤖 AI Coach is creating your workout...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FitsoulColors.TextPrimary
                )
                Text(
                    text = "Analyzing your progress and preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutCard(
    workout: AIWorkout,
    onClick: () -> Unit
) {
    val cardColors = when (workout.difficulty) {
        "Beginner" -> FitsoulColors.Success
        "Intermediate" -> FitsoulColors.Primary
        "Advanced" -> FitsoulColors.Warning
        else -> FitsoulColors.Primary
    }
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FitsoulColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = workout.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = cardColors.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = workout.difficulty,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = cardColors,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Workout Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WorkoutStatChip(
                    icon = Icons.Default.Schedule,
                    text = "${workout.duration}min"
                )
                WorkoutStatChip(
                    icon = Icons.Default.FitnessCenter,
                    text = "${workout.exercises.size} exercises"
                )
                WorkoutStatChip(
                    icon = Icons.Default.LocalFireDepartment,
                    text = "${workout.caloriesEstimate} cal"
                )
            }
            
            // Target Muscle Groups
            if (workout.targetMuscleGroups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(workout.targetMuscleGroups.take(3)) { muscle ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = FitsoulColors.Primary.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = muscle,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = FitsoulColors.Primary
                            )
                        }
                    }
                }
            }
            
            // Bottom Row - Completion & Actions
            if (workout.completionCount > 0 || workout.lastCompleted != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = FitsoulColors.Surface)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = FitsoulColors.Success,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Completed ${workout.completionCount} times",
                            style = MaterialTheme.typography.bodySmall,
                            color = FitsoulColors.TextSecondary
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = FitsoulColors.TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutStatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FitsoulColors.TextSecondary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = FitsoulColors.TextSecondary
        )
    }
}

@Composable
fun EmptyStateCard(
    filter: String,
    onGenerateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (filter) {
                    "Favorites" -> Icons.Default.Favorite
                    "Strength" -> Icons.Default.FitnessCenter
                    "Cardio" -> Icons.Default.DirectionsRun
                    else -> Icons.Default.AutoAwesome
                },
                contentDescription = null,
                tint = FitsoulColors.TextTertiary,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when (filter) {
                    "Favorites" -> "No favorite workouts yet"
                    "Recent" -> "No recent workouts"
                    "Strength" -> "No strength workouts available"
                    "Cardio" -> "No cardio workouts available"
                    else -> "No workouts yet"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitsoulColors.TextSecondary
            )
            
            Text(
                text = "Let AI create the perfect workout for you!",
                style = MaterialTheme.typography.bodyMedium,
                color = FitsoulColors.TextTertiary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            PremiumButton(
                text = "Generate AI Workout",
                onClick = onGenerateClick,
                icon = Icons.Default.AutoAwesome,
                variant = ButtonVariant.Primary
            )
        }
    }
}

private fun generateNewWorkoutWithViewModel(
    isGeneratingWorkout: Boolean,
    onIsGeneratingUpdate: (Boolean) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    workoutViewModel: WorkoutViewModel
) {
    if (isGeneratingWorkout) return
    
    onIsGeneratingUpdate(true)
    
    coroutineScope.launch {
        try {
            delay(3000) // Simulate AI generation time
            
            // Create new workout (mock AI generation)
            val newWorkout = AIWorkout(
                id = "ai_workout_${System.currentTimeMillis()}",
                name = "AI Custom Workout",
                description = "Personalized by AI based on your goals and preferences",
                duration = 45,
                difficulty = "Intermediate",
                exercises = listOf(
                    "Push-ups", "Squats", "Plank", "Lunges", "Burpees", 
                    "Mountain Climbers", "Jumping Jacks", "Russian Twists"
                ),
                targetMuscleGroups = listOf("Full Body", "Core", "Cardio"),
                caloriesEstimate = 320
            )
            
            workoutViewModel.saveWorkout(newWorkout)
            
        } catch (e: Exception) {
            // Fallback workout creation
            val fallbackWorkout = AIWorkout(
                id = "fallback_workout_${System.currentTimeMillis()}",
                name = "Quick Full Body Workout",
                description = "A balanced workout to get you started",
                duration = 30,
                difficulty = "Beginner",
                exercises = listOf("Push-ups", "Squats", "Plank", "Jumping Jacks"),
                targetMuscleGroups = listOf("Full Body"),
                caloriesEstimate = 200
            )
            
            workoutViewModel.saveWorkout(fallbackWorkout)
        } finally {
            onIsGeneratingUpdate(false)
        }
    }
}

private fun getSampleWorkouts() = listOf(
    AIWorkout(
        id = "1",
        name = "Morning Energy Boost",
        description = "Wake up your body with this energizing routine",
        duration = 20,
        difficulty = "Beginner",
        exercises = listOf("Jumping Jacks", "Push-ups", "Squats", "Plank"),
        targetMuscleGroups = listOf("Full Body", "Cardio"),
        caloriesEstimate = 150,
        completionCount = 5,
        lastCompleted = System.currentTimeMillis() - 86400000 // Yesterday
    ),
    AIWorkout(
        id = "2",
        name = "Strength Builder Pro",
        description = "Build serious strength with compound movements",
        duration = 45,
        difficulty = "Advanced",
        exercises = listOf("Deadlifts", "Squats", "Bench Press", "Pull-ups", "Overhead Press"),
        targetMuscleGroups = listOf("Chest", "Back", "Legs", "Arms"),
        caloriesEstimate = 400,
        completionCount = 3,
        lastCompleted = System.currentTimeMillis() - 172800000 // 2 days ago
    ),
    AIWorkout(
        id = "3",
        name = "Cardio Blast HIIT",
        description = "High-intensity cardio for maximum burn",
        duration = 25,
        difficulty = "Intermediate",
        exercises = listOf("Burpees", "Mountain Climbers", "High Knees", "Jump Squats", "Sprint Intervals"),
        targetMuscleGroups = listOf("Cardio", "Legs", "Core"),
        caloriesEstimate = 300,
        completionCount = 8,
        lastCompleted = System.currentTimeMillis() - 259200000 // 3 days ago
    ),
    AIWorkout(
        id = "4",
        name = "Yoga Flow & Stretch",
        description = "Gentle flow for flexibility and mindfulness",
        duration = 35,
        difficulty = "Beginner",
        exercises = listOf("Sun Salutation", "Warrior Poses", "Downward Dog", "Child's Pose", "Savasana"),
        targetMuscleGroups = listOf("Flexibility", "Balance", "Core"),
        caloriesEstimate = 120,
        completionCount = 2
    ),
    AIWorkout(
        id = "5",
        name = "Core Crusher",
        description = "Targeted ab and core strengthening",
        duration = 15,
        difficulty = "Intermediate",
        exercises = listOf("Plank", "Russian Twists", "Bicycle Crunches", "Dead Bug", "Leg Raises"),
        targetMuscleGroups = listOf("Core", "Abs"),
        caloriesEstimate = 100,
        completionCount = 6
    )
)