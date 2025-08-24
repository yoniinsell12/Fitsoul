package com.fitsoul.app.ui.screens.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.core.theme.FitsoulTypography
import com.fitsoul.app.ui.components.FitsoulPrimaryButton

data class FitnessQuestionnaire(
    val fitnessLevel: String = "",
    val workoutFrequency: String = "",
    val availableTime: String = "",
    val preferredWorkoutTypes: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    val injuries: String = "",
    val primaryGoal: String = ""
)

@Composable
fun FitnessQuestionnaireScreen(
    onComplete: (FitnessQuestionnaire) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var questionnaire by remember { mutableStateOf(FitnessQuestionnaire()) }
    
    val totalSteps = 7
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background)
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentStep + 1).toFloat() / totalSteps },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = FitsoulColors.Primary,
            trackColor = FitsoulColors.SurfaceVariant
        )
        
        when (currentStep) {
            0 -> FitnessLevelStep(
                selectedLevel = questionnaire.fitnessLevel,
                onLevelSelected = { questionnaire = questionnaire.copy(fitnessLevel = it) }
            )
            1 -> WorkoutFrequencyStep(
                selectedFrequency = questionnaire.workoutFrequency,
                onFrequencySelected = { questionnaire = questionnaire.copy(workoutFrequency = it) }
            )
            2 -> AvailableTimeStep(
                selectedTime = questionnaire.availableTime,
                onTimeSelected = { questionnaire = questionnaire.copy(availableTime = it) }
            )
            3 -> WorkoutTypesStep(
                selectedTypes = questionnaire.preferredWorkoutTypes,
                onTypesSelected = { questionnaire = questionnaire.copy(preferredWorkoutTypes = it) }
            )
            4 -> EquipmentStep(
                selectedEquipment = questionnaire.equipment,
                onEquipmentSelected = { questionnaire = questionnaire.copy(equipment = it) }
            )
            5 -> InjuriesStep(
                selectedInjuries = questionnaire.injuries,
                onInjuriesSelected = { questionnaire = questionnaire.copy(injuries = it) }
            )
            6 -> PrimaryGoalStep(
                selectedGoal = questionnaire.primaryGoal,
                onGoalSelected = { questionnaire = questionnaire.copy(primaryGoal = it) }
            )
        }
        
        // Navigation buttons
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = FitsoulColors.Primary
                    ),
                    border = BorderStroke(1.dp, FitsoulColors.Primary)
                ) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }
            
            FitsoulPrimaryButton(
                text = if (currentStep == totalSteps - 1) "Complete" else "Next",
                onClick = {
                    if (currentStep == totalSteps - 1) {
                        onComplete(questionnaire)
                    } else {
                        currentStep++
                    }
                },
                enabled = isStepComplete(currentStep, questionnaire)
            )
        }
    }
}

@Composable
private fun FitnessLevelStep(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit
) {
    QuestionStep(
        title = "What's your current fitness level?",
        subtitle = "Help us tailor workouts to your ability",
        options = listOf(
            "Beginner" to "New to fitness or returning after a break",
            "Intermediate" to "Regularly active with some experience",
            "Advanced" to "Experienced athlete or fitness enthusiast"
        ),
        selectedOption = selectedLevel,
        onOptionSelected = onLevelSelected
    )
}

@Composable
private fun WorkoutFrequencyStep(
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit
) {
    QuestionStep(
        title = "How often do you want to work out?",
        subtitle = "We'll plan your schedule accordingly",
        options = listOf(
            "2-3 times per week" to "Perfect for beginners or busy schedules",
            "4-5 times per week" to "Great for steady progress",
            "6-7 times per week" to "For serious fitness enthusiasts"
        ),
        selectedOption = selectedFrequency,
        onOptionSelected = onFrequencySelected
    )
}

@Composable
private fun AvailableTimeStep(
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    QuestionStep(
        title = "How much time do you have per workout?",
        subtitle = "We'll create efficient workouts for your schedule",
        options = listOf(
            "15-30 minutes" to "Quick and effective sessions",
            "30-45 minutes" to "Balanced workout duration",
            "45-60 minutes" to "Comprehensive training sessions",
            "60+ minutes" to "Extended training sessions"
        ),
        selectedOption = selectedTime,
        onOptionSelected = onTimeSelected
    )
}

@Composable
private fun WorkoutTypesStep(
    selectedTypes: List<String>,
    onTypesSelected: (List<String>) -> Unit
) {
    MultiSelectQuestionStep(
        title = "What types of workouts do you enjoy?",
        subtitle = "Select all that apply - we'll mix and match",
        options = listOf(
            "Strength Training" to "Build muscle and power",
            "Cardio" to "Improve heart health and endurance",
            "HIIT" to "High-intensity interval training",
            "Yoga/Stretching" to "Flexibility and mindfulness",
            "Functional Training" to "Real-world movement patterns",
            "Sports-Specific" to "Training for specific sports"
        ),
        selectedOptions = selectedTypes,
        onOptionsSelected = onTypesSelected
    )
}

@Composable
private fun EquipmentStep(
    selectedEquipment: List<String>,
    onEquipmentSelected: (List<String>) -> Unit
) {
    MultiSelectQuestionStep(
        title = "What equipment do you have access to?",
        subtitle = "We'll design workouts with what you have",
        options = listOf(
            "No Equipment" to "Bodyweight exercises only",
            "Dumbbells" to "Versatile weight training",
            "Resistance Bands" to "Portable strength training",
            "Full Gym" to "Complete equipment access",
            "Home Gym" to "Personal equipment setup",
            "Outdoor Space" to "Parks and outdoor areas"
        ),
        selectedOptions = selectedEquipment,
        onOptionsSelected = onEquipmentSelected
    )
}

@Composable
private fun InjuriesStep(
    selectedInjuries: String,
    onInjuriesSelected: (String) -> Unit
) {
    QuestionStep(
        title = "Do you have any injuries or limitations?",
        subtitle = "We'll modify exercises to keep you safe",
        options = listOf(
            "No limitations" to "Ready for any exercise",
            "Lower back issues" to "We'll focus on core stability",
            "Knee problems" to "Low-impact alternatives available",
            "Shoulder issues" to "Modified upper body exercises",
            "Other injuries" to "Tell us in your profile later"
        ),
        selectedOption = selectedInjuries,
        onOptionSelected = onInjuriesSelected
    )
}

@Composable
private fun PrimaryGoalStep(
    selectedGoal: String,
    onGoalSelected: (String) -> Unit
) {
    QuestionStep(
        title = "What's your primary fitness goal?",
        subtitle = "We'll prioritize this in your workout plans",
        options = listOf(
            "Weight Loss" to "Burn calories and lose fat",
            "Muscle Building" to "Gain strength and size",
            "Endurance" to "Improve cardiovascular fitness",
            "Flexibility" to "Increase mobility and flexibility",
            "General Health" to "Overall wellness and fitness",
            "Athletic Performance" to "Sport-specific improvement"
        ),
        selectedOption = selectedGoal,
        onOptionSelected = onGoalSelected
    )
}

@Composable
private fun QuestionStep(
    title: String,
    subtitle: String,
    options: List<Pair<String, String>>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = title,
                style = FitsoulTypography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subtitle,
                style = FitsoulTypography.bodyLarge.copy(
                    color = FitsoulColors.TextSecondary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        items(options.size) { index ->
            val (option, description) = options[index]
            OptionCard(
                title = option,
                description = description,
                isSelected = selectedOption == option,
                onClick = { onOptionSelected(option) }
            )
        }
    }
}

@Composable
private fun MultiSelectQuestionStep(
    title: String,
    subtitle: String,
    options: List<Pair<String, String>>,
    selectedOptions: List<String>,
    onOptionsSelected: (List<String>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(40.dp))
            
            Text(
                text = title,
                style = FitsoulTypography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = subtitle,
                style = FitsoulTypography.bodyLarge.copy(
                    color = FitsoulColors.TextSecondary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        items(options.size) { index ->
            val (option, description) = options[index]
            OptionCard(
                title = option,
                description = description,
                isSelected = selectedOptions.contains(option),
                onClick = {
                    val newSelection = if (selectedOptions.contains(option)) {
                        selectedOptions - option
                    } else {
                        selectedOptions + option
                    }
                    onOptionsSelected(newSelection)
                }
            )
        }
    }
}

@Composable
private fun OptionCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FitsoulColors.Primary.copy(alpha = 0.1f) else FitsoulColors.Surface
        ),
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) FitsoulColors.Primary else FitsoulColors.Border
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = FitsoulTypography.titleMedium,
                    color = if (isSelected) FitsoulColors.Primary else FitsoulColors.OnSurface
                )
                Text(
                    text = description,
                    style = FitsoulTypography.bodyMedium,
                    color = FitsoulColors.TextSecondary
                )
            }
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(FitsoulColors.Primary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = FitsoulColors.OnPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private fun isStepComplete(step: Int, questionnaire: FitnessQuestionnaire): Boolean {
    return when (step) {
        0 -> questionnaire.fitnessLevel.isNotEmpty()
        1 -> questionnaire.workoutFrequency.isNotEmpty()
        2 -> questionnaire.availableTime.isNotEmpty()
        3 -> questionnaire.preferredWorkoutTypes.isNotEmpty()
        4 -> questionnaire.equipment.isNotEmpty()
        5 -> questionnaire.injuries.isNotEmpty()
        6 -> questionnaire.primaryGoal.isNotEmpty()
        else -> false
    }
}
