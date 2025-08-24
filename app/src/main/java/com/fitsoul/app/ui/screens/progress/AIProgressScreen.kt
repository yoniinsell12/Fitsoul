package com.fitsoul.app.ui.screens.progress

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.data.service.FitnessTrackingService
import com.fitsoul.app.data.service.WorkoutStats
import com.fitsoul.app.data.service.FitnessMetrics
import com.fitsoul.app.ui.components.*
import com.fitsoul.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ProgressInsight(
    val title: String,
    val description: String,
    val type: InsightType,
    val trend: String, // "up", "down", "stable"
    val value: String,
    val suggestion: String
)

enum class InsightType {
    ACHIEVEMENT, WARNING, TIP, MILESTONE
}

data class WeeklyProgress(
    val week: String,
    val workouts: Int,
    val calories: Int,
    val avgDuration: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIProgressScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var aiInsights by remember { mutableStateOf<List<ProgressInsight>>(emptyList()) }
    var isGeneratingInsights by remember { mutableStateOf(false) }
    var selectedTimeRange by remember { mutableStateOf("Week") }
    
    // Mock states (replace with actual service when available)
    val workoutStats by remember { mutableStateOf(com.fitsoul.app.data.service.WorkoutStats()) }
    val dailyMetrics by remember { mutableStateOf(com.fitsoul.app.data.service.FitnessMetrics()) }
    
    val coroutineScope = rememberCoroutineScope()
    
    // Generate AI insights on first load
    LaunchedEffect(Unit) {
        generateAIInsights(
            workoutStats = workoutStats,
            dailyMetrics = dailyMetrics,
            onInsightsUpdate = { aiInsights = it },
            onLoadingUpdate = { isGeneratingInsights = it },
            coroutineScope = coroutineScope
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Header with AI Analysis
        item {
            AIProgressHeader(
                workoutStats = workoutStats,
                isGeneratingInsights = isGeneratingInsights,
                onRefreshInsights = {
                    generateAIInsights(
                        workoutStats = workoutStats,
                        dailyMetrics = dailyMetrics,
                        onInsightsUpdate = { aiInsights = it },
                        onLoadingUpdate = { isGeneratingInsights = it },
                        coroutineScope = coroutineScope
                    )
                }
            )
        }
        
        // Time Range Selector
        item {
            TimeRangeSelector(
                selectedRange = selectedTimeRange,
                onRangeSelected = { selectedTimeRange = it }
            )
        }
        
        // Key Metrics Overview
        item {
            KeyMetricsOverview(
                workoutStats = workoutStats,
                dailyMetrics = dailyMetrics,
                timeRange = selectedTimeRange
            )
        }
        
        // AI Insights
        if (aiInsights.isNotEmpty()) {
            item {
                Text(
                    text = "🤖 AI Insights & Recommendations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FitsoulColors.TextPrimary
                )
            }
            
            items(aiInsights) { insight ->
                AIInsightCard(insight = insight)
            }
        }
        
        // Progress Charts
        item {
            Text(
                text = "📊 Progress Visualization",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FitsoulColors.TextPrimary
            )
        }
        
        item {
            WorkoutFrequencyChart(
                weeklyProgress = getSampleWeeklyProgress(),
                timeRange = selectedTimeRange
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CaloriesProgressChart(
                    totalCalories = workoutStats.totalCaloriesBurned,
                    goalCalories = 2000,
                    modifier = Modifier.weight(1f)
                )
                
                StreakProgressChart(
                    currentStreak = workoutStats.currentStreak,
                    longestStreak = workoutStats.longestStreak,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Personal Records
        item {
            PersonalRecordsSection(
                strengthPRs = workoutStats.strengthPR,
                cardioRecords = workoutStats.cardioRecords
            )
        }
        
        // Bottom padding for navigation
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AIProgressHeader(
    workoutStats: WorkoutStats,
    isGeneratingInsights: Boolean,
    onRefreshInsights: () -> Unit
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
                Column {
                    Text(
                        text = "Progress Analytics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = FitsoulColors.TextPrimary
                    )
                    Text(
                        text = "AI-Powered Insights",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary
                    )
                }
                
                IconButton(
                    onClick = onRefreshInsights,
                    enabled = !isGeneratingInsights
                ) {
                    if (isGeneratingInsights) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = FitsoulColors.Primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Insights",
                            tint = FitsoulColors.Primary
                        )
                    }
                }
            }
            
            if (isGeneratingInsights) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = FitsoulColors.Primary.copy(alpha = 0.1f)
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = FitsoulColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI is analyzing your progress...",
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
fun TimeRangeSelector(
    selectedRange: String,
    onRangeSelected: (String) -> Unit
) {
    val ranges = listOf("Week", "Month", "3 Months", "Year")
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(ranges) { range ->
            FilterChip(
                onClick = { onRangeSelected(range) },
                label = { Text(text = range) },
                selected = selectedRange == range,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = FitsoulColors.Primary,
                    selectedLabelColor = Color.White,
                    containerColor = FitsoulColors.Surface,
                    labelColor = FitsoulColors.TextPrimary
                )
            )
        }
    }
}

@Composable
fun KeyMetricsOverview(
    workoutStats: WorkoutStats,
    dailyMetrics: FitnessMetrics,
    timeRange: String
) {
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
                text = "Key Metrics - $timeRange",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitsoulColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Workouts",
                    value = workoutStats.totalWorkouts.toString(),
                    change = "+12%",
                    trend = "up",
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Calories",
                    value = "${workoutStats.totalCaloriesBurned}",
                    change = "+8%",
                    trend = "up",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Avg Duration",
                    value = "${workoutStats.totalDuration / 1000 / 60 / maxOf(workoutStats.totalWorkouts, 1)}min",
                    change = "+5%",
                    trend = "up",
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Streak",
                    value = "${workoutStats.currentStreak}d",
                    change = if (workoutStats.currentStreak > 3) "+${workoutStats.currentStreak - 3}" else "0",
                    trend = if (workoutStats.currentStreak > 3) "up" else "stable",
                    icon = Icons.Default.Whatshot,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    change: String,
    trend: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.SurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FitsoulColors.Primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = FitsoulColors.TextSecondary
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = FitsoulColors.TextPrimary
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = when (trend) {
                        "up" -> Icons.Default.TrendingUp
                        "down" -> Icons.Default.TrendingDown
                        else -> Icons.Default.TrendingFlat
                    },
                    contentDescription = null,
                    tint = when (trend) {
                        "up" -> FitsoulColors.Success
                        "down" -> FitsoulColors.Warning
                        else -> FitsoulColors.TextTertiary
                    },
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = change,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (trend) {
                        "up" -> FitsoulColors.Success
                        "down" -> FitsoulColors.Warning
                        else -> FitsoulColors.TextTertiary
                    }
                )
            }
        }
    }
}

@Composable
fun AIInsightCard(insight: ProgressInsight) {
    val cardColor = when (insight.type) {
        InsightType.ACHIEVEMENT -> FitsoulColors.Success
        InsightType.WARNING -> FitsoulColors.Warning
        InsightType.TIP -> FitsoulColors.Primary
        InsightType.MILESTONE -> FitsoulColors.Secondary
    }
    
    val icon = when (insight.type) {
        InsightType.ACHIEVEMENT -> Icons.Default.EmojiEvents
        InsightType.WARNING -> Icons.Default.Warning
        InsightType.TIP -> Icons.Default.Lightbulb
        InsightType.MILESTONE -> Icons.Default.Flag
    }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = cardColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = cardColor,
                    modifier = Modifier.size(20.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FitsoulColors.TextPrimary
                    )
                    Text(
                        text = insight.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary
                    )
                }
                
                if (insight.value.isNotEmpty()) {
                    Text(
                        text = insight.value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = cardColor
                    )
                }
            }
            
            if (insight.suggestion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = FitsoulColors.Surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "💡 ${insight.suggestion}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = FitsoulColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutFrequencyChart(
    weeklyProgress: List<WeeklyProgress>,
    timeRange: String
) {
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
                text = "Workout Frequency",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitsoulColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple bar chart visualization
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyProgress.forEach { week ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Bar
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height((week.workouts * 10).dp.coerceAtMost(100.dp))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            FitsoulColors.Primary,
                                            FitsoulColors.Primary.copy(alpha = 0.7f)
                                        )
                                    )
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = week.workouts.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = FitsoulColors.TextPrimary
                        )
                        
                        Text(
                            text = week.week,
                            style = MaterialTheme.typography.labelSmall,
                            color = FitsoulColors.TextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaloriesProgressChart(
    totalCalories: Int,
    goalCalories: Int,
    modifier: Modifier = Modifier
) {
    val progress = (totalCalories.toFloat() / goalCalories).coerceAtMost(1f)
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Calories Goal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = FitsoulColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Circular progress
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Canvas(
                    modifier = Modifier.size(120.dp)
                ) {
                    drawCircularProgress(
                        progress = progress,
                        color = FitsoulColors.Primary
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalCalories.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FitsoulColors.TextPrimary
                    )
                    Text(
                        text = "/ $goalCalories",
                        style = MaterialTheme.typography.bodySmall,
                        color = FitsoulColors.TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(progress * 100).toInt()}% Complete",
                style = MaterialTheme.typography.labelSmall,
                color = FitsoulColors.TextSecondary
            )
        }
    }
}

@Composable
fun StreakProgressChart(
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Workout Streak",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = FitsoulColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = null,
                tint = FitsoulColors.Warning,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$currentStreak",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = FitsoulColors.TextPrimary
            )
            
            Text(
                text = "days",
                style = MaterialTheme.typography.titleMedium,
                color = FitsoulColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Best: $longestStreak days",
                style = MaterialTheme.typography.labelSmall,
                color = FitsoulColors.TextTertiary
            )
        }
    }
}

@Composable
fun PersonalRecordsSection(
    strengthPRs: Map<String, Float>,
    cardioRecords: Map<String, Float>
) {
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
                text = "Personal Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitsoulColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (strengthPRs.isNotEmpty()) {
                Text(
                    text = "💪 Strength",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = FitsoulColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                strengthPRs.entries.take(3).forEach { (exercise, weight) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = exercise,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FitsoulColors.TextPrimary
                        )
                        Text(
                            text = "${weight.toInt()} lbs",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FitsoulColors.Primary
                        )
                    }
                }
            }
            
            if (cardioRecords.isNotEmpty()) {
                if (strengthPRs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Text(
                    text = "🏃 Cardio",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = FitsoulColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                cardioRecords.entries.take(3).forEach { (activity, record) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = activity,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FitsoulColors.TextPrimary
                        )
                        Text(
                            text = "${record} min",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = FitsoulColors.Secondary
                        )
                    }
                }
            }
            
            if (strengthPRs.isEmpty() && cardioRecords.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = FitsoulColors.TextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Complete workouts to set your first records!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawCircularProgress(
    progress: Float,
    color: Color
) {
    val strokeWidth = 12.dp.toPx()
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
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

private fun generateAIInsights(
    workoutStats: WorkoutStats,
    dailyMetrics: FitnessMetrics,
    onInsightsUpdate: (List<ProgressInsight>) -> Unit,
    onLoadingUpdate: (Boolean) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    onLoadingUpdate(true)
    
    coroutineScope.launch {
        try {
            delay(2500) // Simulate AI analysis time
            
            // Mock AI recommendations (replace with actual service when available)
            
            // Generate insights based on data
            val insights = mutableListOf<ProgressInsight>()
            
            // Achievement insights
            if (workoutStats.currentStreak > 5) {
                insights.add(
                    ProgressInsight(
                        title = "Amazing Consistency! 🔥",
                        description = "You've maintained a ${workoutStats.currentStreak}-day workout streak",
                        type = InsightType.ACHIEVEMENT,
                        trend = "up",
                        value = "${workoutStats.currentStreak}d",
                        suggestion = "Keep it up! Consistency is key to reaching your fitness goals."
                    )
                )
            }
            
            // Progress insights
            if (workoutStats.totalWorkouts > 0) {
                insights.add(
                    ProgressInsight(
                        title = "Calorie Burn Improving",
                        description = "Your average calories per workout is trending upward",
                        type = InsightType.TIP,
                        trend = "up",
                        value = "+12%",
                        suggestion = "Your fitness is improving! Consider increasing intensity for even better results."
                    )
                )
            }
            
            // Warning insights
            if (workoutStats.currentStreak == 0) {
                insights.add(
                    ProgressInsight(
                        title = "Time to Get Back On Track",
                        description = "It's been a while since your last workout",
                        type = InsightType.WARNING,
                        trend = "down",
                        value = "",
                        suggestion = "Start with a light 15-minute session to rebuild your momentum."
                    )
                )
            }
            
            // Milestone insights
            if (workoutStats.totalWorkouts >= 10) {
                insights.add(
                    ProgressInsight(
                        title = "10 Workout Milestone! 🎉",
                        description = "You've completed ${workoutStats.totalWorkouts} workouts",
                        type = InsightType.MILESTONE,
                        trend = "up",
                        value = "${workoutStats.totalWorkouts}",
                        suggestion = "Fantastic progress! Time to set more challenging goals."
                    )
                )
            }
            
            // Add more insights based on AI analysis
            insights.add(
                ProgressInsight(
                    title = "Workout Variety Suggestion",
                    description = "Your favorite workout type shows great consistency",
                    type = InsightType.TIP,
                    trend = "stable",
                    value = "",
                    suggestion = "Try mixing in some ${if (workoutStats.favoriteWorkoutType.contains("strength", ignoreCase = true)) "cardio" else "strength"} workouts for balanced fitness."
                )
            )
            
            onInsightsUpdate(insights)
            
        } catch (e: Exception) {
            // Fallback insights
            val fallbackInsights = listOf(
                ProgressInsight(
                    title = "Keep Moving Forward! 💪",
                    description = "Every workout brings you closer to your goals",
                    type = InsightType.TIP,
                    trend = "up",
                    value = "",
                    suggestion = "Consistency beats perfection. Focus on showing up regularly."
                ),
                ProgressInsight(
                    title = "Track Your Progress",
                    description = "Regular logging helps identify patterns and improvements",
                    type = InsightType.TIP,
                    trend = "stable",
                    value = "",
                    suggestion = "Log your workouts to unlock more personalized insights."
                )
            )
            
            onInsightsUpdate(fallbackInsights)
        } finally {
            onLoadingUpdate(false)
        }
    }
}

private fun getSampleWeeklyProgress() = listOf(
    WeeklyProgress("W1", 3, 450, 25),
    WeeklyProgress("W2", 5, 720, 30),
    WeeklyProgress("W3", 4, 580, 28),
    WeeklyProgress("W4", 6, 860, 32),
    WeeklyProgress("W5", 5, 750, 30),
    WeeklyProgress("W6", 7, 980, 35)
)