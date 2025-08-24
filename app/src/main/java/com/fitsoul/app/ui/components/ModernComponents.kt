package com.fitsoul.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fitsoul.app.core.theme.FitsoulColors
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    gradient: Boolean = true,
    onClick: (() -> Unit)? = null,
    trendIcon: ImageVector? = null,
    trendValue: String? = null,
    isPositiveTrend: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    Card(
        onClick = { 
            onClick?.invoke()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        },
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (gradient) 12.dp else 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = FitsoulColors.Primary.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gradient) Color.Transparent else FitsoulColors.SurfaceVariant
        ),
        interactionSource = remember { MutableInteractionSource() },
        enabled = onClick != null
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (gradient) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(
                                    FitsoulColors.Primary.copy(alpha = 0.15f),
                                    FitsoulColors.Secondary.copy(alpha = 0.1f)
                                )
                            )
                        )
                    } else Modifier
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = FitsoulColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (trendValue != null && trendIcon != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = trendIcon,
                                    contentDescription = null,
                                    tint = if (isPositiveTrend) FitsoulColors.Success else FitsoulColors.Error,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = trendValue,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isPositiveTrend) FitsoulColors.Success else FitsoulColors.Error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        FitsoulColors.Primary.copy(alpha = 0.2f),
                                        FitsoulColors.Primary.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = FitsoulColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = FitsoulColors.TextPrimary
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// Keep the old component for backwards compatibility
@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    gradient: Boolean = false
) = PremiumMetricCard(
    title = title,
    value = value,
    subtitle = subtitle,
    icon = icon,
    modifier = modifier,
    gradient = gradient
)

@Composable
fun ProgressChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    targetValue: Float = 6f,
    currentValue: Float = 3.4f
) {
    var animationProgress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(data) {
        animationProgress = 0f
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(1500, easing = EaseOutCubic)
        ) { value, _ ->
            animationProgress = value
        }
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.SurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = FitsoulColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Running",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FitsoulColors.Surface
                    )
                ) {
                    Text(
                        text = "1 Week",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Today's Progress
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodyMedium,
                color = FitsoulColors.TextSecondary
            )
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${currentValue}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = FitsoulColors.TextPrimary
                )
                Text(
                    text = "Km",
                    style = MaterialTheme.typography.titleLarge,
                    color = FitsoulColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target",
                    style = MaterialTheme.typography.bodySmall,
                    color = FitsoulColors.TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${targetValue}Km",
                    style = MaterialTheme.typography.bodySmall,
                    color = FitsoulColors.Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                drawProgressChart(
                    data = data,
                    animationProgress = animationProgress,
                    size = size,
                    primaryColor = FitsoulColors.Primary,
                    targetValue = targetValue
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Week days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                days.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (index == 2) FitsoulColors.Primary else FitsoulColors.TextSecondary,
                        fontWeight = if (index == 2) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawProgressChart(
    data: List<Float>,
    animationProgress: Float,
    size: Size,
    primaryColor: androidx.compose.ui.graphics.Color,
    targetValue: Float
) {
    if (data.isEmpty()) return
    
    val maxValue = maxOf(data.maxOrNull() ?: 0f, targetValue)
    val stepX = size.width / (data.size - 1).coerceAtLeast(1)
    
    // Draw target line
    val targetY = size.height - (targetValue / maxValue) * size.height
    drawLine(
        color = primaryColor.copy(alpha = 0.3f),
        start = Offset(0f, targetY),
        end = Offset(size.width, targetY),
        strokeWidth = 2.dp.toPx(),
        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
    )
    
    // Draw chart path
    val path = Path()
    val points = data.mapIndexed { index, value ->
        val x = index * stepX
        val y = size.height - (value / maxValue) * size.height
        Offset(x, y)
    }
    
    if (points.isNotEmpty()) {
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val animatedIndex = (i * animationProgress).coerceAtMost(points.size - 1f)
            if (animatedIndex >= i) {
                path.lineTo(points[i].x, points[i].y)
            } else {
                val prevPoint = points[i - 1]
                val currentPoint = points[i]
                val progress = animatedIndex - (i - 1)
                val animatedX = prevPoint.x + (currentPoint.x - prevPoint.x) * progress
                val animatedY = prevPoint.y + (currentPoint.y - prevPoint.y) * progress
                path.lineTo(animatedX, animatedY)
                break
            }
        }
        
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
        
        // Draw gradient fill
        val fillPath = Path()
        fillPath.addPath(path)
        if (points.isNotEmpty()) {
            fillPath.lineTo(points.last().x, size.height)
            fillPath.lineTo(points.first().x, size.height)
            fillPath.close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.3f),
                    primaryColor.copy(alpha = 0.0f)
                )
            )
        )
        
        // Draw points
        points.forEachIndexed { index, point ->
            val animatedIndex = (index * animationProgress).coerceAtMost(points.size - 1f)
            if (animatedIndex >= index) {
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = androidx.compose.ui.graphics.Color.White,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }
        }
    }
}

@Composable
fun CircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 8.dp.value,
    size: Int = 60
) {
    var animatedProgress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(progress) {
        animatedProgress = 0f
        animate(
            initialValue = 0f,
            targetValue = progress,
            animationSpec = tween(1000, easing = EaseOutCubic)
        ) { value, _ ->
            animatedProgress = value
        }
    }
    
    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokeWidth) / 2
            val center = Offset(canvasSize / 2, canvasSize / 2)
            
            // Background circle
            drawCircle(
                color = FitsoulColors.SurfaceVariant,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc
            val sweepAngle = 360 * animatedProgress
            drawArc(
                color = FitsoulColors.Primary,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                ),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
        }
    }
}

@Composable
fun WorkoutCard(
    title: String,
    instructor: String,
    participants: String,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.SurfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            FitsoulColors.Primary.copy(alpha = 0.8f),
                            FitsoulColors.Secondary.copy(alpha = 0.6f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "Trending",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "#1",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = instructor,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = participants,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    Button(
                        onClick = onJoinClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FitsoulColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Join",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun WorkoutStatCard(
    title: String,
    value: String,
    unit: String,
    progress: Float,
    icon: ImageVector,
    color: Color = FitsoulColors.Primary,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic)
    )
    
    Card(
        onClick = { onClick?.invoke() },
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = color.copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.SurfaceVariant
        )
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
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = FitsoulColors.TextSecondary
                )
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    color.copy(alpha = 0.2f),
                                    color.copy(alpha = 0.05f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = FitsoulColors.TextPrimary
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.titleMedium,
                    color = FitsoulColors.TextSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(color, color.copy(alpha = 0.8f))
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.displayLarge
) {
    var currentValue by remember { mutableStateOf(0) }
    
    LaunchedEffect(targetValue) {
        val animationDuration = 1500L
        val steps = 50
        val stepDuration = animationDuration / steps
        val stepValue = targetValue.toFloat() / steps
        
        repeat(steps) { step ->
            currentValue = (stepValue * (step + 1)).toInt()
            kotlinx.coroutines.delay(stepDuration)
        }
        currentValue = targetValue
    }
    
    Text(
        text = currentValue.toString(),
        modifier = modifier,
        style = style,
        fontWeight = FontWeight.ExtraBold,
        color = FitsoulColors.TextPrimary
    )
}
