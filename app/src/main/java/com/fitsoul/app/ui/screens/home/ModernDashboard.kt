package com.fitsoul.app.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.ui.components.*
import com.fitsoul.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

data class WorkoutSession(
    val name: String,
    val type: String,
    val date: String,
    val completion: String,
    val isCompleted: Boolean = false
)

data class FriendActivity(
    val name: String,
    val activity: String,
    val description: String,
    val timeAgo: String,
    val hashtags: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernDashboard(
    onNavigateToWorkouts: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onNavigateToProfile: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {

    val authState by authViewModel.authState.observeAsState()
    val user = authState?.getUser()
    
    // Sample data for demonstration
    val runningData = listOf(2.1f, 3.5f, 2.8f, 4.2f, 3.4f, 2.9f, 3.4f)
    val recentWorkouts = listOf(
        WorkoutSession("Upper Body", "Push Up", "02/16", "02 / 16", true),
        WorkoutSession("Lower Body", "Bodyweight Squats", "06/10", "06 / 10"),
        WorkoutSession("Core", "Plank Variations", "12/12", "12 / 12", true)
    )
    
    val friendsActivities = listOf(
        FriendActivity(
            "Tanara W",
            "Workout Complete!",
            "Just Wrapped Up An Intense Workout Session! 💪",
            "1 HR AGO",
            listOf("#FitnessJourney", "#WorkoutDone")
        ),
        FriendActivity(
            "Hiday Nana",
            "Milestone Unlocked!",
            "Hit A Personal Best On The Bike Today! 🚴",
            "1 HR AGO",
            listOf("#FitnessJourney", "#WorkoutDone")
        )
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                FitsoulColors.Surface,
                                FitsoulColors.Background
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // User Profile Section
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            FitsoulColors.Primary,
                                            FitsoulColors.Secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.displayName?.firstOrNull()?.toString()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Find training",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FitsoulColors.TextSecondary
                            )
                        }
                    }
                    
                    // Action Buttons
                    Row {
                        IconButton(
                            onClick = { /* Calendar action */ },
                            modifier = Modifier
                                .background(
                                    FitsoulColors.SurfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Calendar",
                                tint = FitsoulColors.TextSecondary
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = { /* Notifications */ },
                            modifier = Modifier
                                .background(
                                    FitsoulColors.SurfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = FitsoulColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }
        
        item {
            // Stats Section
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FitsoulColors.Primary
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your Steps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FitsoulColors.SurfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = FitsoulColors.Error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Heart Rate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FitsoulColors.TextPrimary
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FitsoulColors.SurfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bedtime,
                            contentDescription = null,
                            tint = FitsoulColors.Info,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Time Sleep",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FitsoulColors.TextPrimary
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Total Steps Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FitsoulColors.SurfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Steps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "12/02/2024",
                            style = MaterialTheme.typography.bodySmall,
                            color = FitsoulColors.TextSecondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgress(
                            progress = 0.12f,
                            modifier = Modifier.size(60.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1,235 / 10,000",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Walking Boosts Mood And Reduces Stress.\nYou're On The Path To A Healthier Mind!",
                                style = MaterialTheme.typography.bodySmall,
                                color = FitsoulColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Trending Workout
            WorkoutCard(
                title = "Ultimate Fat-Burning HIIT",
                instructor = "+182 Joined",
                participants = "",
                onJoinClick = onNavigateToWorkouts,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Running Progress Chart
            ProgressChart(
                data = runningData,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                targetValue = 6f,
                currentValue = 3.4f
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Recent Workouts Section
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Work Out",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { /* See all */ }) {
                        Text(
                            text = "See All",
                            color = FitsoulColors.Primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                recentWorkouts.forEach { workout ->
                    WorkoutItem(workout = workout)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        item {
            // Friends Activities
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Friends Activities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                friendsActivities.forEach { activity ->
                    FriendActivityItem(activity = activity)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for navigation bar
        }
    }
}

@Composable
private fun WorkoutItem(
    workout: WorkoutSession,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        FitsoulColors.SurfaceVariant,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder for workout icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            FitsoulColors.TextSecondary.copy(alpha = 0.3f),
                            RoundedCornerShape(4.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${workout.type} ${workout.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = workout.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = FitsoulColors.TextSecondary
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = workout.completion,
                    style = MaterialTheme.typography.bodySmall,
                    color = FitsoulColors.TextSecondary
                )
                
                if (workout.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = FitsoulColors.Success,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        tint = FitsoulColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendActivityItem(
    activity: FriendActivity,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(FitsoulColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activity.name.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = activity.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = FitsoulColors.TextSecondary
                )
            }
            
            Text(
                text = "🏆 ${activity.activity}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
            
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodySmall,
                color = FitsoulColors.TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Text(
                text = activity.hashtags.joinToString(" "),
                style = MaterialTheme.typography.bodySmall,
                color = FitsoulColors.Primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
