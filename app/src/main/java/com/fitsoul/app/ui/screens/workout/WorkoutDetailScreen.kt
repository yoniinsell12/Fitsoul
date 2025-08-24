package com.fitsoul.app.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workout: AIWorkout,
    onNavigateBack: () -> Unit,
    onStartWorkout: (AIWorkout) -> Unit,
    onDeleteWorkout: (String) -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = "Workout Details",
                    color = FitsoulColors.TextPrimary
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = FitsoulColors.TextPrimary
                    )
                }
            },
            actions = {
                // Delete button for saved workouts
                if (workout.id.startsWith("ai_workout_") || workout.id.contains("-")) {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete workout",
                            tint = FitsoulColors.Warning
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = FitsoulColors.Background
            )
        )
        
        // Workout Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            item {
                WorkoutHeaderCard(workout = workout)
            }
            
            // Quick Stats
            item {
                WorkoutStatsCard(workout = workout)
            }
            
            // Target Muscle Groups
            if (workout.targetMuscleGroups.isNotEmpty()) {
                item {
                    MuscleGroupsCard(muscleGroups = workout.targetMuscleGroups)
                }
            }
            
            // Exercises List
            item {
                ExercisesCard(exercises = workout.exercises)
            }
            
            // Start Workout Button
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PremiumButton(
                    text = "Start Workout",
                    onClick = { onStartWorkout(workout) },
                    icon = Icons.Default.PlayArrow,
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for nav
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Workout") },
            text = { Text("Are you sure you want to delete \"${workout.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteWorkout(workout.id)
                        showDeleteDialog = false
                        onNavigateBack() // Go back after deleting
                    }
                ) {
                    Text("Delete", color = FitsoulColors.Warning)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WorkoutHeaderCard(workout: AIWorkout) {
    val cardColors = when (workout.difficulty) {
        "Beginner" -> FitsoulColors.Success
        "Intermediate" -> FitsoulColors.Primary
        "Advanced" -> FitsoulColors.Warning
        else -> FitsoulColors.Primary
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = FitsoulColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = workout.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = cardColors,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutStatsCard(workout: AIWorkout) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Workout Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FitsoulColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WorkoutStatItem(
                    icon = Icons.Default.Schedule,
                    label = "Duration",
                    value = "${workout.duration} min",
                    modifier = Modifier.weight(1f)
                )
                
                WorkoutStatItem(
                    icon = Icons.Default.FitnessCenter,
                    label = "Exercises",
                    value = "${workout.exercises.size}",
                    modifier = Modifier.weight(1f)
                )
                
                WorkoutStatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    label = "Calories",
                    value = "${workout.caloriesEstimate}",
                    modifier = Modifier.weight(1f)
                )
            }
            
            if (workout.completionCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = FitsoulColors.Surface)
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = FitsoulColors.Success,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Completed ${workout.completionCount} times",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FitsoulColors.Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = FitsoulColors.Primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = FitsoulColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MuscleGroupsCard(muscleGroups: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Target Muscle Groups",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FitsoulColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(muscleGroups) { muscle ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = FitsoulColors.Primary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = muscle,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = FitsoulColors.Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExercisesCard(exercises: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = FitsoulColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Exercises (${exercises.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FitsoulColors.TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            exercises.forEachIndexed { index, exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = FitsoulColors.Primary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = FitsoulColors.Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = exercise,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = FitsoulColors.TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                if (index < exercises.size - 1) {
                    Divider(
                        color = FitsoulColors.Surface,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                }
            }
        }
    }
}