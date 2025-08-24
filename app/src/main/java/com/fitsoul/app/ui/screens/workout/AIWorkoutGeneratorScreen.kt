package com.fitsoul.app.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.ui.viewmodels.AIWorkoutViewModel
import com.fitsoul.app.ui.viewmodels.WorkoutPlanState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIWorkoutGeneratorScreen(
    viewModel: AIWorkoutViewModel = hiltViewModel(),
    onBackPressed: () -> Unit = {},
    onStartWorkout: (String) -> Unit = {}
) {
    var workoutPlanState by remember { mutableStateOf<WorkoutPlanState>(WorkoutPlanState.Initial) }
    
    LaunchedEffect(Unit) {
        viewModel.workoutPlanState.collect { state ->
            workoutPlanState = state
        }
    }
    
    var selectedGoals by remember { mutableStateOf(listOf<String>()) }
    var fitnessLevel by remember { mutableStateOf("Beginner") }
    var availableTime by remember { mutableStateOf(30) }
    var selectedEquipment by remember { mutableStateOf(listOf<String>()) }
    
    val scrollState = rememberScrollState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Workout Generator") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(FitsoulColors.Background)
        ) {
            when (val state = workoutPlanState) {
                is WorkoutPlanState.Initial -> {
                    // Show workout configuration UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Fitness Goals Selection
                        Text(
                            "Select Your Fitness Goals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val allGoals = listOf(
                            "Build Muscle", "Lose Weight", "Improve Endurance",
                            "Increase Strength", "Enhance Flexibility", "Improve Cardiovascular Health"
                        )
                        
                        // Custom implementation for goals
                        Column(modifier = Modifier.fillMaxWidth()) {
                            var currentRowItems = mutableListOf<String>()
                            
                            allGoals.forEach { goal ->
                                currentRowItems.add(goal)
                                
                                if (currentRowItems.size == 2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        currentRowItems.forEach { rowGoal ->
                                            FilterChip(
                                                modifier = Modifier.weight(1f),
                                                selected = selectedGoals.contains(rowGoal),
                                                onClick = {
                                                    selectedGoals = if (selectedGoals.contains(rowGoal)) {
                                                        selectedGoals - rowGoal
                                                    } else {
                                                        selectedGoals + rowGoal
                                                    }
                                                },
                                                label = { Text(rowGoal) }
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    currentRowItems = mutableListOf()
                                }
                            }
                            
                            // Handle any remaining items
                            if (currentRowItems.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    currentRowItems.forEach { rowGoal ->
                                        FilterChip(
                                            modifier = Modifier.weight(1f),
                                            selected = selectedGoals.contains(rowGoal),
                                            onClick = {
                                                selectedGoals = if (selectedGoals.contains(rowGoal)) {
                                                    selectedGoals - rowGoal
                                                } else {
                                                    selectedGoals + rowGoal
                                                }
                                            },
                                            label = { Text(rowGoal) }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Fitness Level Selection
                        Text(
                            "Fitness Level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val levels = listOf("Beginner", "Intermediate", "Advanced")
                        
                        // Use Row with buttons instead of SegmentedButtonRow
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            levels.forEach { level ->
                                Button(
                                    onClick = { fitnessLevel = level },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (fitnessLevel == level) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Text(level)
                                }
                                
                                if (level != levels.last()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Available Time Selection
                        Text(
                            "Available Time (minutes)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Slider(
                            value = availableTime.toFloat(),
                            onValueChange = { availableTime = it.toInt() },
                            valueRange = 10f..60f,
                            steps = 5
                        )
                        
                        Text(
                            "$availableTime minutes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Equipment Selection
                        Text(
                            "Available Equipment (Optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        val equipmentOptions = listOf(
                            "Dumbbells", "Resistance Bands", "Kettlebells",
                            "Pull-up Bar", "Bench", "Barbell"
                        )
                        
                        // Custom implementation for equipment
                        Column(modifier = Modifier.fillMaxWidth()) {
                            var currentRowItems = mutableListOf<String>()
                            
                            equipmentOptions.forEach { equipment ->
                                currentRowItems.add(equipment)
                                
                                if (currentRowItems.size == 2) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        currentRowItems.forEach { rowEquipment ->
                                            FilterChip(
                                                modifier = Modifier.weight(1f),
                                                selected = selectedEquipment.contains(rowEquipment),
                                                onClick = {
                                                    selectedEquipment = if (selectedEquipment.contains(rowEquipment)) {
                                                        selectedEquipment - rowEquipment
                                                    } else {
                                                        selectedEquipment + rowEquipment
                                                    }
                                                },
                                                label = { Text(rowEquipment) }
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    currentRowItems = mutableListOf()
                                }
                            }
                            
                            // Handle any remaining items
                            if (currentRowItems.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    currentRowItems.forEach { rowEquipment ->
                                        FilterChip(
                                            modifier = Modifier.weight(1f),
                                            selected = selectedEquipment.contains(rowEquipment),
                                            onClick = {
                                                selectedEquipment = if (selectedEquipment.contains(rowEquipment)) {
                                                    selectedEquipment - rowEquipment
                                                } else {
                                                    selectedEquipment + rowEquipment
                                                }
                                            },
                                            label = { Text(rowEquipment) }
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Generate Button
                        Button(
                            onClick = {
                                viewModel.generateWorkoutPlan(
                                    goals = if (selectedGoals.isEmpty()) listOf("General Fitness") else selectedGoals,
                                    fitnessLevel = fitnessLevel,
                                    availableTime = availableTime,
                                    equipment = selectedEquipment
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = true
                        ) {
                            Text("Generate Workout Plan")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Quick workout button
                        OutlinedButton(
                            onClick = { viewModel.generateQuickWorkout(15) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Quick 15-Min Workout")
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // API Test button for debugging
                        OutlinedButton(
                            onClick = { viewModel.testDeepSeekConnection() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("🧪 Test API Connection")
                        }
                    }
                }
                
                is WorkoutPlanState.Loading -> {
                    // Show loading UI
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = FitsoulColors.Primary
                            )
                            
                            Text(
                                "Creating your personalized workout plan...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            Text(
                                "This may take a few moments",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                is WorkoutPlanState.Success -> {
                    // Show workout plan
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            "Your Personalized Workout Plan",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Workout plan content
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    state.plan,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Start workout button
                        Button(
                            onClick = { onStartWorkout(state.plan) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Start This Workout")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Generate new button
                        OutlinedButton(
                            onClick = {
                                viewModel.generateWorkoutPlan(
                                    goals = if (selectedGoals.isEmpty()) listOf("General Fitness") else selectedGoals,
                                    fitnessLevel = fitnessLevel,
                                    availableTime = availableTime,
                                    equipment = selectedEquipment
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Generate New Plan")
                        }
                    }
                }
                
                is WorkoutPlanState.Error -> {
                    // Show error UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "Unable to generate workout plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                viewModel.generateWorkoutPlan(
                                    goals = if (selectedGoals.isEmpty()) listOf("General Fitness") else selectedGoals,
                                    fitnessLevel = fitnessLevel,
                                    availableTime = availableTime,
                                    equipment = selectedEquipment
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}
