package com.fitsoul.app.ui.screens.onboarding

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fitsoul.app.core.theme.FitsoulColors

data class FitnessObjective(
    val title: String,
    val description: String,
    val isSelected: Boolean = false
)

@Composable
fun ObjectivesScreen(
    onContinue: (List<String>) -> Unit
) {
    var objectives by remember {
        mutableStateOf(
            listOf(
                FitnessObjective("Weight Loss", "Burn calories and shed pounds", true),
                FitnessObjective("Muscle Gain", "Build strength and muscle mass"),
                FitnessObjective("Increased Flexibility", "Improve range of motion", true),
                FitnessObjective("Cardiovascular Endurance", "Boost heart health"),
                FitnessObjective("Mind-Body Connection", "Mental wellness and mindfulness")
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Header Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Choose Your Fitness Objectives",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = FitsoulColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Personalized Fitness\nExperience Just For You.",
                style = MaterialTheme.typography.bodyLarge,
                color = FitsoulColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Objectives List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(objectives.size) { index ->
                ObjectiveCard(
                    objective = objectives[index],
                    onClick = {
                        objectives = objectives.mapIndexed { i, obj ->
                            if (i == index) obj.copy(isSelected = !obj.isSelected)
                            else obj
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Continue Button
        Button(
            onClick = {
                val selectedObjectives = objectives
                    .filter { it.isSelected }
                    .map { it.title }
                onContinue(selectedObjectives)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FitsoulColors.Primary
            ),
            enabled = objectives.any { it.isSelected }
        ) {
            Text(
                text = "Set Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ObjectiveCard(
    objective: FitnessObjective,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (objective.isSelected) {
                    Modifier.border(
                        2.dp,
                        FitsoulColors.Primary,
                        RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (objective.isSelected) {
                FitsoulColors.Primary.copy(alpha = 0.1f)
            } else {
                FitsoulColors.SurfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = objective.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = FitsoulColors.TextPrimary
                )
                
                Text(
                    text = objective.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Selection Indicator
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        if (objective.isSelected) FitsoulColors.Primary else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        2.dp,
                        if (objective.isSelected) FitsoulColors.Primary else FitsoulColors.TextSecondary.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (objective.isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}