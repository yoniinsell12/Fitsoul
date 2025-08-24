package com.fitsoul.app.ui.screens.challenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitsoul.app.core.theme.FitsoulColors

data class Challenge(
    val title: String,
    val type: String,
    val description: String? = null,
    val isDaily: Boolean = false,
    val isActive: Boolean = false,
    val icon: ImageVector = Icons.Default.FitnessCenter
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen() {
    val challenges = listOf(
        Challenge(
            title = "5-Minute Dynamic Stretching",
            type = "Daily Challenge",
            isDaily = true,
            isActive = true,
            icon = Icons.Default.SelfImprovement
        ),
        Challenge(
            title = "Stairs Streak Challenge",
            type = "Weekly Challenge",
            icon = Icons.Default.Stairs
        ),
        Challenge(
            title = "Hydration Habit",
            type = "Daily Challenge",
            icon = Icons.Default.WaterDrop
        ),
        Challenge(
            title = "Lunchtime Walk",
            type = "Daily Challenge",
            icon = Icons.Default.DirectionsWalk
        ),
        Challenge(
            title = "Mindful Breathing",
            type = "Wellness Challenge",
            icon = Icons.Default.Air
        )
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fitness Challenges",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = FitsoulColors.TextPrimary
                    )
                    Text(
                        text = "Push Yourself, Achieve More",
                        style = MaterialTheme.typography.bodyLarge,
                        color = FitsoulColors.TextSecondary
                    )
                }
                
                // Points Badge
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FitsoulColors.Primary.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = "Points",
                            tint = FitsoulColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "350 Pts",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = FitsoulColors.Primary
                        )
                    }
                }
            }
        }
        
        item {
            // Active Daily Challenge
            challenges.find { it.isActive }?.let { challenge ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
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
                                        FitsoulColors.Primary,
                                        FitsoulColors.Secondary
                                    )
                                ),
                                RoundedCornerShape(20.dp)
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
                                        text = challenge.title,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White.copy(alpha = 0.2f)
                                        )
                                    ) {
                                        Text(
                                            text = challenge.type,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                
                                Button(
                                    onClick = { /* Start challenge */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = FitsoulColors.Primary
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Let's Go!",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Challenge illustration placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = challenge.icon,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            // Challenge Variations Section
            Text(
                text = "Challenge Variations",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = FitsoulColors.TextPrimary
            )
        }
        
        items(challenges.filter { !it.isActive }) { challenge ->
            ChallengeVariationCard(
                challenge = challenge,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for navigation
        }
    }
}

@Composable
private fun ChallengeVariationCard(
    challenge: Challenge,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.SurfaceVariant
        ),
        onClick = { /* Navigate to challenge details */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Challenge Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        FitsoulColors.Primary.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = challenge.icon,
                    contentDescription = null,
                    tint = FitsoulColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Challenge Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = challenge.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FitsoulColors.TextPrimary
                )
                
                Text(
                    text = challenge.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary
                )
                
                challenge.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = FitsoulColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Action Button
            IconButton(
                onClick = { /* Start or view challenge */ }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Start Challenge",
                    tint = FitsoulColors.Primary
                )
            }
        }
    }
}

