package com.fitsoul.app.ui.screens.workout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.data.service.FitnessTrackingService
import com.fitsoul.app.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class WorkoutExercise(
    val name: String,
    val sets: Int,
    val reps: String, // Can be "8-12" or "30s" for time-based
    val restTime: Int, // seconds
    val instructions: String,
    val targetMuscles: List<String>,
    val difficulty: String,
    val equipment: List<String> = emptyList(),
    val isCompleted: Boolean = false,
    val completedSets: Int = 0
)

enum class WorkoutState {
    NOT_STARTED, ACTIVE, RESTING, COMPLETED, PAUSED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIWorkoutDetailScreen(
    workout: AIWorkout,
    onNavigateBack: () -> Unit,
    onWorkoutCompleted: () -> Unit
) {
    var workoutState by remember { mutableStateOf(WorkoutState.NOT_STARTED) }
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var currentSet by remember { mutableIntStateOf(1) }
    var restTimeRemaining by remember { mutableIntStateOf(0) }
    var workoutTimeElapsed by remember { mutableIntStateOf(0) }
    var aiCoachMessage by remember { mutableStateOf("") }
    var isAICoachVisible by remember { mutableStateOf(false) }
    
    val exercises = remember { getWorkoutExercises(workout) }
    val currentExercise = if (currentExerciseIndex < exercises.size) exercises[currentExerciseIndex] else null
    
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    // Timer effects
    LaunchedEffect(workoutState, restTimeRemaining) {
        if (workoutState == WorkoutState.RESTING && restTimeRemaining > 0) {
            delay(1000)
            restTimeRemaining--
            if (restTimeRemaining == 0) {
                workoutState = WorkoutState.ACTIVE
                showAICoachMessage(
                    message = "Rest complete! Let's keep the momentum going. You're doing great! 💪",
                    isVisible = isAICoachVisible,
                    onVisibilityChange = { isAICoachVisible = it },
                    coroutineScope = coroutineScope
                )
            }
        }
    }
    
    LaunchedEffect(workoutState) {
        if (workoutState == WorkoutState.ACTIVE) {
            while (workoutState == WorkoutState.ACTIVE) {
                delay(1000)
                workoutTimeElapsed++
            }
        }
    }
    
    // Mock start workout tracking (replace with actual service when available)
    LaunchedEffect(workoutState) {
        if (workoutState == WorkoutState.ACTIVE && currentExerciseIndex == 0 && currentSet == 1) {
            // Mock workout started
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background)
    ) {
        // Header
        WorkoutHeader(
            workoutName = workout.name,
            workoutState = workoutState,
            timeElapsed = workoutTimeElapsed,
            onNavigateBack = onNavigateBack,
            onPauseResume = {
                workoutState = if (workoutState == WorkoutState.PAUSED) {
                    WorkoutState.ACTIVE
                } else {
                    WorkoutState.PAUSED
                }
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        )
        
        // AI Coach Message
        AnimatedVisibility(
            visible = isAICoachVisible,
            enter = slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { -it }
            ) + fadeIn(),
            exit = slideOutVertically(
                animationSpec = tween(300),
                targetOffsetY = { -it }
            ) + fadeOut()
        ) {
            AICoachMessage(
                message = aiCoachMessage,
                onDismiss = { isAICoachVisible = false }
            )
        }
        
        if (workoutState == WorkoutState.NOT_STARTED) {
            // Workout Overview
            WorkoutOverview(
                workout = workout,
                exercises = exercises,
                onStartWorkout = {
                    workoutState = WorkoutState.ACTIVE
                    showAICoachMessage(
                        message = "Let's crush this workout! 🔥 Remember, I'm here to guide you through every rep. You've got this!",
                        isVisible = isAICoachVisible,
                        onVisibilityChange = { isAICoachVisible = it },
                        coroutineScope = coroutineScope
                    )
                }
            )
        } else if (workoutState == WorkoutState.COMPLETED) {
            // Workout Completed
            WorkoutCompletedScreen(
                workout = workout,
                totalTime = workoutTimeElapsed,
                onFinish = {
                    // Mock complete workout
                    onWorkoutCompleted()
                }
            )
        } else {
            // Active Workout
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Progress Indicator
                item {
                    WorkoutProgressCard(
                        currentExercise = currentExerciseIndex + 1,
                        totalExercises = exercises.size,
                        currentSet = currentSet,
                        totalSets = currentExercise?.sets ?: 0
                    )
                }
                
                // Rest Timer
                if (workoutState == WorkoutState.RESTING) {
                    item {
                        RestTimerCard(
                            timeRemaining = restTimeRemaining,
                            totalRestTime = currentExercise?.restTime ?: 60,
                            onSkipRest = {
                                restTimeRemaining = 0
                                workoutState = WorkoutState.ACTIVE
                            }
                        )
                    }
                }
                
                // Current Exercise
                currentExercise?.let { exercise ->
                    item {
                        CurrentExerciseCard(
                            exercise = exercise,
                            currentSet = currentSet,
                            workoutState = workoutState,
                            onSetCompleted = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                
                                if (currentSet < exercise.sets) {
                                    // Move to next set
                                    currentSet++
                                    restTimeRemaining = exercise.restTime
                                    workoutState = WorkoutState.RESTING
                                    
                                    // Mock update workout progress
                                    
                                    showAICoachMessage(
                                        message = "Great set! 💪 Take your rest and prepare for the next one.",
                                        isVisible = isAICoachVisible,
                                        onVisibilityChange = { isAICoachVisible = it },
                                        coroutineScope = coroutineScope
                                    )
                                } else {
                                    // Move to next exercise or complete workout
                                    if (currentExerciseIndex < exercises.size - 1) {
                                        currentExerciseIndex++
                                        currentSet = 1
                                        restTimeRemaining = exercises[currentExerciseIndex].restTime
                                        workoutState = WorkoutState.RESTING
                                        
                                        // Mock update workout progress
                                        
                                        showAICoachMessage(
                                            message = "Excellent! Moving to the next exercise: ${exercises[currentExerciseIndex].name}",
                                            isVisible = isAICoachVisible,
                                            onVisibilityChange = { isAICoachVisible = it },
                                            coroutineScope = coroutineScope
                                        )
                                    } else {
                                        // Workout completed
                                        workoutState = WorkoutState.COMPLETED
                                        showAICoachMessage(
                                            message = "🎉 WORKOUT COMPLETE! You absolutely crushed it! That's what champions are made of!",
                                            isVisible = isAICoachVisible,
                                            onVisibilityChange = { isAICoachVisible = it },
                                            coroutineScope = coroutineScope
                                        )
                                    }
                                }
                            },
                            onGetFormTips = {
                                // Mock form tips
                                val mockTips = "✅ Perfect Form for ${exercise.name}\n\n" +
                                    "Key Points:\n" +
                                    "• Keep your core engaged\n" +
                                    "• Maintain proper alignment\n" +
                                    "• Control the movement\n" +
                                    "• Breathe consistently\n\n" +
                                    "Quality over quantity! 🎯"
                                    
                                showAICoachMessage(
                                    message = mockTips,
                                    isVisible = isAICoachVisible,
                                    onVisibilityChange = { isAICoachVisible = it },
                                    coroutineScope = coroutineScope
                                )
                            }
                        )
                    }
                }
                
                // Upcoming Exercises
                if (currentExerciseIndex < exercises.size - 1) {
                    item {
                        Text(
                            text = "Up Next",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FitsoulColors.TextPrimary
                        )
                    }
                    
                    items(exercises.drop(currentExerciseIndex + 1).take(2)) { exercise ->
                        UpcomingExerciseCard(exercise = exercise)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHeader(
    workoutName: String,
    workoutState: WorkoutState,
    timeElapsed: Int,
    onNavigateBack: () -> Unit,
    onPauseResume: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = FitsoulColors.TextPrimary
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = workoutName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FitsoulColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                if (workoutState != WorkoutState.NOT_STARTED) {
                    Text(
                        text = "${timeElapsed / 60}:${(timeElapsed % 60).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = FitsoulColors.Primary
                    )
                }
            }
            
            if (workoutState == WorkoutState.ACTIVE || workoutState == WorkoutState.PAUSED) {
                IconButton(onClick = onPauseResume) {
                    Icon(
                        imageVector = if (workoutState == WorkoutState.PAUSED) 
                            Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (workoutState == WorkoutState.PAUSED) "Resume" else "Pause",
                        tint = FitsoulColors.Primary
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }
    }
}

@Composable
fun AICoachMessage(
    message: String,
    onDismiss: () -> Unit
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
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = FitsoulColors.Primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI Coach",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FitsoulColors.Primary
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextPrimary
                )
            }
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = FitsoulColors.TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun WorkoutOverview(
    workout: AIWorkout,
    exercises: List<WorkoutExercise>,
    onStartWorkout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Workout Summary
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = FitsoulColors.Surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Workout Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FitsoulColors.TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        WorkoutSummaryItem(
                            title = "Exercises",
                            value = exercises.size.toString(),
                            icon = Icons.Default.FitnessCenter,
                            modifier = Modifier.weight(1f)
                        )
                        WorkoutSummaryItem(
                            title = "Duration",
                            value = "${workout.duration}min",
                            icon = Icons.Default.Schedule,
                            modifier = Modifier.weight(1f)
                        )
                        WorkoutSummaryItem(
                            title = "Calories",
                            value = "${workout.caloriesEstimate}",
                            icon = Icons.Default.LocalFireDepartment,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    PremiumButton(
                        text = "Start Workout",
                        onClick = onStartWorkout,
                        icon = Icons.Default.PlayArrow,
                        variant = ButtonVariant.Primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        // Exercise List
        item {
            Text(
                text = "Exercises",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitsoulColors.TextPrimary
            )
        }
        
        items(exercises) { exercise ->
            ExercisePreviewCard(exercise = exercise)
        }
    }
}

@Composable
fun WorkoutSummaryItem(
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FitsoulColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FitsoulColors.Primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = FitsoulColors.TextSecondary
            )
        }
    }
}

@Composable
fun CurrentExerciseCard(
    exercise: WorkoutExercise,
    currentSet: Int,
    workoutState: WorkoutState,
    onSetCompleted: () -> Unit,
    onGetFormTips: () -> Unit
) {
    Card(
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
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FitsoulColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = onGetFormTips) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = "Form Tips",
                        tint = FitsoulColors.Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Set $currentSet of ${exercise.sets} • ${exercise.reps} reps",
                style = MaterialTheme.typography.titleMedium,
                color = FitsoulColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (exercise.instructions.isNotEmpty()) {
                Text(
                    text = exercise.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Target muscles
            if (exercise.targetMuscles.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(exercise.targetMuscles) { muscle ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = FitsoulColors.Secondary.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = muscle,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = FitsoulColors.Secondary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Complete Set Button
            PremiumButton(
                text = "Complete Set",
                onClick = onSetCompleted,
                icon = Icons.Default.Check,
                variant = ButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                enabled = workoutState == WorkoutState.ACTIVE
            )
        }
    }
}

@Composable
fun RestTimerCard(
    timeRemaining: Int,
    totalRestTime: Int,
    onSkipRest: () -> Unit
) {
    val progress = if (totalRestTime > 0) (totalRestTime - timeRemaining).toFloat() / totalRestTime else 0f
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rest Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitsoulColors.Primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Circular Progress Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Canvas(
                    modifier = Modifier.size(120.dp)
                ) {
                    drawRestTimer(progress = progress, color = FitsoulColors.Primary)
                }
                
                Text(
                    text = timeRemaining.toString(),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = FitsoulColors.Primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PremiumButton(
                text = "Skip Rest",
                onClick = onSkipRest,
                icon = Icons.Default.SkipNext,
                variant = ButtonVariant.Secondary,
                size = ButtonSize.Small
            )
        }
    }
}

@Composable
fun WorkoutProgressCard(
    currentExercise: Int,
    totalExercises: Int,
    currentSet: Int,
    totalSets: Int
) {
    val exerciseProgress = if (totalExercises > 0) currentExercise.toFloat() / totalExercises else 0f
    val setProgress = if (totalSets > 0) currentSet.toFloat() / totalSets else 0f
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Workout Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitsoulColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Exercise Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Exercise:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary,
                    modifier = Modifier.width(80.dp)
                )
                LinearProgressIndicator(
                    progress = exerciseProgress,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = FitsoulColors.Primary,
                    trackColor = FitsoulColors.Primary.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$currentExercise/$totalExercises",
                    style = MaterialTheme.typography.bodySmall,
                    color = FitsoulColors.TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Set Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Set:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary,
                    modifier = Modifier.width(80.dp)
                )
                LinearProgressIndicator(
                    progress = setProgress,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = FitsoulColors.Secondary,
                    trackColor = FitsoulColors.Secondary.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$currentSet/$totalSets",
                    style = MaterialTheme.typography.bodySmall,
                    color = FitsoulColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun ExercisePreviewCard(exercise: WorkoutExercise) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FitsoulColors.TextPrimary
                )
                Text(
                    text = "${exercise.sets} sets × ${exercise.reps}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary
                )
                if (exercise.equipment.isNotEmpty()) {
                    Text(
                        text = exercise.equipment.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = FitsoulColors.TextTertiary
                    )
                }
            }
            
            Text(
                text = "${exercise.restTime}s",
                style = MaterialTheme.typography.labelMedium,
                color = FitsoulColors.Primary
            )
        }
    }
}

@Composable
fun UpcomingExerciseCard(exercise: WorkoutExercise) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.SurfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = FitsoulColors.TextTertiary,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary
                )
                Text(
                    text = "${exercise.sets} sets × ${exercise.reps}",
                    style = MaterialTheme.typography.bodySmall,
                    color = FitsoulColors.TextTertiary
                )
            }
        }
    }
}

@Composable
fun WorkoutCompletedScreen(
    workout: AIWorkout,
    totalTime: Int,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Celebration Animation
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = FitsoulColors.Warning,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Workout Complete!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = FitsoulColors.TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Amazing job crushing that workout! 🎉",
            style = MaterialTheme.typography.titleMedium,
            color = FitsoulColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Workout Stats
        Card(
            colors = CardDefaults.cardColors(
                containerColor = FitsoulColors.Surface
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WorkoutStatItem(
                        title = "Duration",
                        value = "${totalTime / 60}:${(totalTime % 60).toString().padStart(2, '0')}",
                        icon = Icons.Default.Schedule
                    )
                    WorkoutStatItem(
                        title = "Calories",
                        value = "~${workout.caloriesEstimate}",
                        icon = Icons.Default.LocalFireDepartment
                    )
                    WorkoutStatItem(
                        title = "Exercises",
                        value = "${workout.exercises.size}",
                        icon = Icons.Default.FitnessCenter
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        PremiumButton(
            text = "Finish Workout",
            onClick = onFinish,
            icon = Icons.Default.Check,
            variant = ButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WorkoutStatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FitsoulColors.Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = FitsoulColors.TextPrimary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = FitsoulColors.TextSecondary
        )
    }
}

private fun DrawScope.drawRestTimer(progress: Float, color: Color) {
    val strokeWidth = 8.dp.toPx()
    val radius = (size.minDimension - strokeWidth) / 2
    val center = Offset(size.width / 2, size.height / 2)
    
    // Background circle
    drawCircle(
        color = color.copy(alpha = 0.1f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // Progress arc
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = progress * 360f,
        useCenter = false,
        topLeft = Offset(
            center.x - radius,
            center.y - radius
        ),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private fun showAICoachMessage(
    message: String,
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    onVisibilityChange(true)
    coroutineScope.launch {
        delay(4000) // Show for 4 seconds
        onVisibilityChange(false)
    }
}

private fun getWorkoutExercises(workout: AIWorkout): List<WorkoutExercise> {
    // Convert AIWorkout exercises to detailed WorkoutExercise objects
    return workout.exercises.map { exerciseName ->
        WorkoutExercise(
            name = exerciseName,
            sets = when (exerciseName.lowercase()) {
                "plank" -> 3
                "burpees" -> 3
                "jumping jacks" -> 3
                else -> 3
            },
            reps = when (exerciseName.lowercase()) {
                "plank" -> "30-60s"
                "burpees" -> "8-12"
                "jumping jacks" -> "20-30"
                "push-ups" -> "8-15"
                "squats" -> "12-20"
                "lunges" -> "10-15 each"
                "mountain climbers" -> "20-30"
                "russian twists" -> "15-25"
                else -> "10-15"
            },
            restTime = when (exerciseName.lowercase()) {
                "plank" -> 45
                "burpees" -> 90
                else -> 60
            },
            instructions = when (exerciseName.lowercase()) {
                "push-ups" -> "Keep your body straight, lower chest to ground, push back up"
                "squats" -> "Keep feet shoulder-width apart, lower hips back and down"
                "plank" -> "Hold your body straight from head to heels"
                "burpees" -> "Squat down, jump back to plank, do push-up, jump forward, jump up"
                "lunges" -> "Step forward, lower back knee toward ground, push back to start"
                "jumping jacks" -> "Jump feet apart while raising arms, jump back to start"
                "mountain climbers" -> "In plank position, alternate bringing knees to chest"
                "russian twists" -> "Sit with knees bent, lean back slightly, rotate torso side to side"
                else -> "Focus on proper form and controlled movement"
            },
            targetMuscles = when (exerciseName.lowercase()) {
                "push-ups" -> listOf("Chest", "Arms", "Core")
                "squats" -> listOf("Legs", "Glutes")
                "plank" -> listOf("Core", "Shoulders")
                "burpees" -> listOf("Full Body", "Cardio")
                "lunges" -> listOf("Legs", "Glutes")
                "jumping jacks" -> listOf("Cardio", "Legs")
                "mountain climbers" -> listOf("Core", "Cardio")
                "russian twists" -> listOf("Core", "Abs")
                else -> listOf("Full Body")
            },
            difficulty = workout.difficulty,
            equipment = when (exerciseName.lowercase()) {
                "push-ups", "plank", "burpees", "lunges", "jumping jacks", "mountain climbers", "russian twists" -> listOf("Bodyweight")
                else -> listOf("Bodyweight")
            }
        )
    }
}