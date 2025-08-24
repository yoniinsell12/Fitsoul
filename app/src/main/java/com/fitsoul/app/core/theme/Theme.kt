package com.fitsoul.app.core.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = FitsoulColors.Primary,
    onPrimary = FitsoulColors.OnPrimary,
    primaryContainer = FitsoulColors.PrimaryVariant,
    onPrimaryContainer = FitsoulColors.OnPrimary,
    
    secondary = FitsoulColors.Secondary,
    onSecondary = FitsoulColors.OnSecondary,
    secondaryContainer = FitsoulColors.SecondaryVariant,
    onSecondaryContainer = FitsoulColors.OnSecondary,
    
    tertiary = FitsoulColors.PrimaryLight,
    onTertiary = Color.White,
    tertiaryContainer = FitsoulColors.PrimaryLight.copy(alpha = 0.3f),
    onTertiaryContainer = FitsoulColors.PrimaryLight,
    
    background = FitsoulColors.Background,
    onBackground = FitsoulColors.OnBackground,
    
    surface = FitsoulColors.Surface,
    onSurface = FitsoulColors.OnSurface,
    surfaceVariant = FitsoulColors.SurfaceVariant,
    onSurfaceVariant = FitsoulColors.OnSurfaceVariant,
    surfaceContainer = FitsoulColors.SurfaceContainer,
    surfaceContainerHigh = FitsoulColors.SurfaceVariant,
    surfaceContainerHighest = FitsoulColors.SurfaceVariant.copy(alpha = 0.8f),
    
    error = FitsoulColors.Error,
    onError = Color.White,
    errorContainer = FitsoulColors.Error.copy(alpha = 0.12f),
    onErrorContainer = FitsoulColors.Error,
    
    outline = FitsoulColors.Border,
    outlineVariant = FitsoulColors.Border.copy(alpha = 0.5f),
    
    inverseSurface = FitsoulColors.OnSurface,
    inverseOnSurface = FitsoulColors.Surface,
    inversePrimary = FitsoulColors.Primary.copy(alpha = 0.8f)
)

private val LightColorScheme = lightColorScheme(
    primary = FitsoulColors.Primary,
    onPrimary = Color.White,
    primaryContainer = FitsoulColors.Primary.copy(alpha = 0.12f),
    onPrimaryContainer = FitsoulColors.PrimaryVariant,
    
    secondary = FitsoulColors.Secondary,
    onSecondary = Color.White,
    secondaryContainer = FitsoulColors.Secondary.copy(alpha = 0.12f),
    onSecondaryContainer = FitsoulColors.SecondaryVariant,
    
    tertiary = FitsoulColors.PrimaryLight,
    onTertiary = Color.White,
    tertiaryContainer = FitsoulColors.PrimaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = FitsoulColors.PrimaryLight,
    
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),
    
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainer = Color(0xFFF3EDF7),
    
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun FitsoulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val systemUiController = rememberSystemUiController()
    
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = !darkTheme
        )
        systemUiController.setNavigationBarColor(
            color = colorScheme.surface,
            darkIcons = !darkTheme
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = FitsoulTypography,
        content = content
    )
}
