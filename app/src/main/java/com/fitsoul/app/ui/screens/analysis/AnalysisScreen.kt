package com.fitsoul.app.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen() {
    val runningData = listOf(1.5f, 2.8f, 2.5f, 3.8f, 3.4f, 2.9f, 4.2f)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Header
            Text(
                text = "Analysis",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = FitsoulColors.TextPrimary
            )
        }
        
        item {
            // Main Progress Chart
            ProgressChart(
                data = runningData,
                modifier = Modifier.fillMaxWidth(),
                targetValue = 6f,
                currentValue = 3.4f
            )
        }
        
        item {
            // Running Growth Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FitsoulColors.SurfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Running Growth",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Visualize The Progression Of Your Running Distance Over Time And Identify Trends In Your Training Routine.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Pace Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FitsoulColors.Surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Pace",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "02:19:20",
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Distance  4.5 Km",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = FitsoulColors.TextSecondary
                                    )
                                }
                                
                                Column(
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "3.3 min/km",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FitsoulColors.Primary
                                    )
                                    
                                    // Mini pace chart
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp, 30.dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        FitsoulColors.Primary.copy(alpha = 0.1f),
                                                        FitsoulColors.Primary.copy(alpha = 0.3f)
                                                    )
                                                ),
                                                RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        // Simple pace visualization
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize(),
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.Bottom
                                            ) {
                                                repeat(5) { index ->
                                                    Box(
                                                        modifier = Modifier
                                                            .width(2.dp)
                                                            .height((8 + index * 2).dp)
                                                            .background(
                                                                FitsoulColors.Primary,
                                                                RoundedCornerShape(1.dp)
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item {
            // Route Mapping Section
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            text = "Route Mapping",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "View Route",
                            tint = FitsoulColors.Primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetricBadge(
                            value = "4.5km",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            value = "2.2hr",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            value = "6,246ft",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Route Map Placeholder
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FitsoulColors.Surface
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            FitsoulColors.SurfaceVariant,
                                            FitsoulColors.Surface
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Simple route visualization
                            Box(
                                modifier = Modifier
                                    .size(120.dp, 80.dp)
                                    .background(
                                        Color.Transparent
                                    )
                            ) {
                                // Route path visualization
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .background(
                                            FitsoulColors.Primary,
                                            RoundedCornerShape(2.dp)
                                        )
                                        .align(Alignment.Center)
                                )
                                
                                // Start point
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .align(Alignment.CenterStart)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            // Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    title = "Weekly Distance",
                    value = "24.8",
                    subtitle = "Km this week",
                    icon = Icons.Default.DirectionsRun,
                    modifier = Modifier.weight(1f),
                    gradient = true
                )
                
                MetricCard(
                    title = "Avg Speed",
                    value = "12.5",
                    subtitle = "Km/h average",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    title = "Calories",
                    value = "2,840",
                    subtitle = "kcal burned",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Active Time",
                    value = "4.2",
                    subtitle = "hours today",
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f),
                    gradient = true
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for navigation
        }
    }
}

@Composable
private fun MetricBadge(
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        )
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = FitsoulColors.TextPrimary
        )
    }
}