package com.fitsoul.app.ui.screens.onboarding

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.observeAsState()
    
    // Premium animation states
    var showLogo by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }
    
    // Rotation animation for logo
    val infiniteTransition = rememberInfiniteTransition(label = "splash_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_animation"
    )
    
    // Pulsing effect for background
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_animation"
    )
    
    // Scale animation for logo
    val logoScale by animateFloatAsState(
        targetValue = if (showLogo) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // Ensure splash shows for at least 2 seconds
    var minTimeElapsed by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Staggered animations
        delay(300)
        showLogo = true
        delay(500)
        showTitle = true
        delay(300)
        showTagline = true
        delay(400)
        showProgress = true
        delay(1000) // Additional buffer
        minTimeElapsed = true
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        FitsoulColors.Surface.copy(alpha = pulseAlpha),
                        FitsoulColors.Background
                    ),
                    radius = 800f
                )
            )
    ) {
        // Animated background particles
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawAnimatedParticles(rotation)
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Premium animated logo
            AnimatedVisibility(
                visible = showLogo,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    FitsoulColors.Primary.copy(alpha = 0.3f),
                                    FitsoulColors.Primary.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                        .scale(logoScale),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Fitsoul Logo",
                        modifier = Modifier
                            .size(64.dp)
                            .rotate(rotation),
                        tint = FitsoulColors.Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Title with staggered animation
            AnimatedVisibility(
                visible = showTitle,
                enter = slideInVertically(
                    animationSpec = tween(600, easing = EaseOutBack),
                    initialOffsetY = { it / 2 }
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Text(
                    text = "Fitsoul",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = FitsoulColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
            }
            
            // Tagline with animation
            AnimatedVisibility(
                visible = showTagline,
                enter = slideInVertically(
                    animationSpec = tween(600, 200, EaseOutBack),
                    initialOffsetY = { it / 3 }
                ) + fadeIn(animationSpec = tween(600, 200))
            ) {
                Text(
                    text = "Your AI-Powered Fitness Journey",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitsoulColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Premium loading indicator
            AnimatedVisibility(
                visible = showProgress,
                enter = scaleIn(animationSpec = tween(400)) + fadeIn()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = FitsoulColors.Primary,
                        strokeWidth = 4.dp,
                        trackColor = FitsoulColors.Primary.copy(alpha = 0.2f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Status text based on auth state
                    val statusText = when {
                        authState?.isLoading() == true -> "Initializing AI Coach..."
                        authState?.isAuthenticated() == true -> "Welcome back, champion!"
                        authState?.isUnauthenticated() == true && minTimeElapsed -> "Ready to transform?"
                        else -> "Loading premium experience..."
                    }
                    
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitsoulColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Enhanced version info at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Premium Edition",
                style = MaterialTheme.typography.labelSmall,
                color = FitsoulColors.Primary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = FitsoulColors.TextTertiary
            )
        }
    }
}

// Helper function for animated background particles
private fun DrawScope.drawAnimatedParticles(rotation: Float) {
    val particleCount = 12
    val centerX = size.width / 2
    val centerY = size.height / 2
    val radius = minOf(size.width, size.height) * 0.4f
    
    repeat(particleCount) { index ->
        val angle = (rotation + (index * 360f / particleCount)) * (kotlin.math.PI / 180f).toFloat()
        val x = centerX + cos(angle) * radius
        val y = centerY + sin(angle) * radius
        
        drawCircle(
            color = FitsoulColors.Primary.copy(alpha = 0.1f),
            radius = 4.dp.toPx(),
            center = Offset(x, y)
        )
    }
}
