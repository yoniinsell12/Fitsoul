package com.fitsoul.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitsoul.app.core.theme.FitsoulColors

@Composable
fun FitsoulPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = FitsoulColors.Primary,
            contentColor = Color.White,
            disabledContainerColor = FitsoulColors.Primary.copy(alpha = 0.6f),
            disabledContentColor = Color.White.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

enum class ButtonVariant {
    Primary, Secondary, Tertiary
}

enum class ButtonSize {
    Small, Medium, Large
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )
    
    val (containerColor, contentColor, height) = when (variant) {
        ButtonVariant.Primary -> Triple(
            FitsoulColors.Primary,
            Color.White,
            when (size) {
                ButtonSize.Small -> 40.dp
                ButtonSize.Medium -> 52.dp
                ButtonSize.Large -> 60.dp
            }
        )
        ButtonVariant.Secondary -> Triple(
            Color.Transparent,
            FitsoulColors.Primary,
            when (size) {
                ButtonSize.Small -> 40.dp
                ButtonSize.Medium -> 52.dp
                ButtonSize.Large -> 60.dp
            }
        )
        ButtonVariant.Tertiary -> Triple(
            FitsoulColors.Surface,
            FitsoulColors.TextPrimary,
            when (size) {
                ButtonSize.Small -> 36.dp
                ButtonSize.Medium -> 48.dp
                ButtonSize.Large -> 56.dp
            }
        )
    }
    
    Button(
        onClick = {
            isPressed = true
            onClick()
        },
        enabled = enabled,
        modifier = modifier
            .height(height)
            .scale(scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (variant == ButtonVariant.Secondary) Color.Transparent else containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        border = if (variant == ButtonVariant.Secondary) {
            ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(
                    colors = listOf(FitsoulColors.Primary, FitsoulColors.Primary.copy(alpha = 0.7f))
                )
            )
        } else null,
        shape = RoundedCornerShape(
            when (size) {
                ButtonSize.Small -> 8.dp
                ButtonSize.Medium -> 12.dp
                ButtonSize.Large -> 16.dp
            }
        ),
        contentPadding = PaddingValues(
            horizontal = when (size) {
                ButtonSize.Small -> 12.dp
                ButtonSize.Medium -> 16.dp
                ButtonSize.Large -> 20.dp
            },
            vertical = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                when (size) {
                    ButtonSize.Small -> 4.dp
                    ButtonSize.Medium -> 6.dp
                    ButtonSize.Large -> 8.dp
                }
            )
        ) {
            icon?.let { iconVector ->
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier.size(
                        when (size) {
                            ButtonSize.Small -> 16.dp
                            ButtonSize.Medium -> 18.dp
                            ButtonSize.Large -> 20.dp
                        }
                    )
                )
            }
            
            Text(
                text = text,
                style = when (size) {
                    ButtonSize.Small -> MaterialTheme.typography.bodyMedium
                    ButtonSize.Medium -> MaterialTheme.typography.titleMedium
                    ButtonSize.Large -> MaterialTheme.typography.titleLarge
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(150)
            isPressed = false
        }
    }
}
