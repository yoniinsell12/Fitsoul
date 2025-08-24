package com.fitsoul.app.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.ui.components.*
import kotlinx.coroutines.delay

data class WorkoutPlan(
    val name: String,
    val description: String,
    val duration: String,
    val difficulty: String,
    val exercises: List<Exercise>
)

data class Exercise(
    val name: String,
    val sets: String,
    val reps: String,
    val restTime: String,
    val instructions: String,
    val muscleGroups: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIWorkoutScreen(
    onBackPressed: () -> Unit = {},
    onStartWorkout: (WorkoutPlan) -> Unit = {}
) {
    var isGenerating by remember { mutableStateOf(false) }
    var workoutPlan by remember { mutableStateOf<WorkoutPlan?>(null) }
    var showWorkout by remember { mutableStateOf(false) }
    
    // Sample workout data
    val sampleWorkout = WorkoutPlan(
        name = "🔥 AI-Powered HIIT Blast",
        description = "High-intensity workout designed specifically for your fitness level and goals",
        duration = "30 minutes",
        difficulty = "Intermediate",
        exercises = listOf(
            Exercise(
                name = "Burpees",
                sets = "3",
                reps = "12-15",
                restTime = "45s",
                instructions = "Start standing, squat down, jump back to plank, push-up, jump forward, jump up",
                muscleGroups = listOf("Full Body", "Cardio")
            ),
            Exercise(
                name = "Mountain Climbers",
                sets = "3",
                reps = "20 per leg",
                restTime = "30s",
                instructions = "Start in plank position, alternate bringing knees to chest rapidly",
                muscleGroups = listOf("Core", "Cardio")
            ),
            Exercise(
                name = "Jump Squats",
                sets = "3",
                reps = "15-18",
                restTime = "45s",
                instructions = "Squat down, then explode up into a jump. Land softly and repeat",
                muscleGroups = listOf("Legs", "Glutes")
            ),
            Exercise(
                name = "Push-up to T",
                sets = "3",
                reps = "10-12",
                restTime = "60s",
                instructions = "Do a push-up, then rotate to side plank, alternate sides",
                muscleGroups = listOf("Chest", "Core", "Arms")
            )
        )
    )
    
    LaunchedEffect(Unit) {
        // Simulate AI generation
        delay(1000)
        isGenerating = true
        delay(3000)
        workoutPlan = sampleWorkout
        isGenerating = false
        delay(500)
        showWorkout = true
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background)
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    "AI Workout Generator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = FitsoulColors.TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = FitsoulColors.Surface
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (isGenerating) {
                item {
                    AIGenerationCard()
                }
            } else if (workoutPlan != null && showWorkout) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                            animationSpec = tween(800),
                            initialOffsetY = { it / 2 }
                        )
                    ) {
                        WorkoutHeaderCard(
                            workoutPlan = workoutPlan!!,
                            onStartWorkout = { onStartWorkout(workoutPlan!!) }
                        )
                    }
                }
                
                items(workoutPlan!!.exercises) { exercise ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = tween(600, delayMillis = 200)
                        ) + slideInVertically(
                            animationSpec = tween(600, delayMillis = 200),
                            initialOffsetY = { it / 3 }
                        )
                    ) {
                        ExerciseCard(exercise = exercise)
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun AIGenerationCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = FitsoulColors.Primary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            FitsoulColors.Surface,
                            FitsoulColors.Background
                        )
                    )
                )
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Animated loading indicator
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    FitsoulColors.Primary.copy(alpha = 0.3f),
                                    FitsoulColors.Primary.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = FitsoulColors.Primary,
                        strokeWidth = 4.dp
                    )
                    
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = FitsoulColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "🤖 AI is crafting your workout...",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = FitsoulColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Analyzing your fitness level, goals, and preferences to create the perfect workout just for you",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Generation steps
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GenerationStep("✅ Analyzing your fitness profile", true)
                    GenerationStep("⚡ Selecting optimal exercises", true)
                    GenerationStep("🎯 Customizing intensity levels", false)
                    GenerationStep("🔥 Finalizing your workout", false)
                }
            }
        }
    }
}

@Composable
fun GenerationStep(text: String, isComplete: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (isComplete) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = FitsoulColors.Success,
                modifier = Modifier.size(16.dp)
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = FitsoulColors.Primary,
                strokeWidth = 2.dp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isComplete) FitsoulColors.TextPrimary else FitsoulColors.TextSecondary
        )
    }
}

@Composable
fun WorkoutHeaderCard(
    workoutPlan: WorkoutPlan,
    onStartWorkout: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = FitsoulColors.Primary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            FitsoulColors.Primary.copy(alpha = 0.9f),
                            FitsoulColors.Secondary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = workoutPlan.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Text(
                            text = workoutPlan.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WorkoutInfoChip(
                        icon = Icons.Default.Schedule,
                        text = workoutPlan.duration,
                        modifier = Modifier.weight(1f)
                    )
                    
                    WorkoutInfoChip(
                        icon = Icons.Default.TrendingUp,
                        text = workoutPlan.difficulty,
                        modifier = Modifier.weight(1f)
                    )
                    
                    WorkoutInfoChip(
                        icon = Icons.Default.FitnessCenter,
                        text = "${workoutPlan.exercises.size} exercises",
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                PremiumButton(
                    text = "Start Workout",
                    onClick = onStartWorkout,
                    icon = Icons.Default.PlayArrow,
                    variant = ButtonVariant.Secondary,
                    size = ButtonSize.Large,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun WorkoutInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCard(
    exercise: Exercise
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Card(
        onClick = { isExpanded = !isExpanded },
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = FitsoulColors.Primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.SurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    FitsoulColors.Primary.copy(alpha = 0.2f),
                                    FitsoulColors.Primary.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = FitsoulColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FitsoulColors.TextPrimary
                    )
                    
                    Text(
                        text = "${exercise.sets} sets • ${exercise.reps} reps",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary
                    )
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = FitsoulColors.TextSecondary
                )
            }
            
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "Instructions",
                        style = MaterialTheme.typography.labelLarge,
                        color = FitsoulColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = exercise.instructions,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        exercise.muscleGroups.forEach { muscle ->
                            AssistChip(
                                onClick = { /* Show muscle group info */ },
                                label = {
                                    Text(
                                        text = muscle,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = FitsoulColors.Primary.copy(alpha = 0.1f),
                                    labelColor = FitsoulColors.Primary
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PremiumButton(
                            text = "Form Tips",
                            onClick = { /* Show AI form tips */ },
                            icon = Icons.Default.Help,
                            variant = ButtonVariant.Secondary,
                            size = ButtonSize.Small,
                            modifier = Modifier.weight(1f)
                        )
                        
                        PremiumButton(
                            text = "Start Exercise",
                            onClick = { /* Start this exercise */ },
                            icon = Icons.Default.PlayArrow,
                            variant = ButtonVariant.Primary,
                            size = ButtonSize.Small,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}