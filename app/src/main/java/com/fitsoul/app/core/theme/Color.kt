package com.fitsoul.app.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Fitsoul Color Palette
 * 
 * Comprehensive color system for the Fitsoul fitness app
 * Using object pattern for singleton access to color constants
 */
object FitsoulColors {
    // Primary Fitness Colors - Energetic Green Palette
    val Primary = Color(0xFF4ADE80)        // Vibrant fitness green
    val PrimaryVariant = Color(0xFF22C55E) // Darker green
    val PrimaryLight = Color(0xFF4ADE80)   // Same as primary
    val Secondary = Color(0xFF10B981)      // Emerald accent
    val SecondaryVariant = Color(0xFF059669)
    
    // Dark Theme Background System
    val Background = Color(0xFF0A0A0A)     // Deep black
    val Surface = Color(0xFF1A1A1A)        // Dark surface
    val SurfaceVariant = Color(0xFF2A2A2A) // Elevated surface
    val SurfaceContainer = Color(0xFF1E1E1E) // Container surface
    
    // Text Colors
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFE0E0E0)   // Soft white
    val OnSurface = Color(0xFFE0E0E0)
    val OnSurfaceVariant = Color(0xFFB0B0B0)
    
    // Semantic Colors
    val Success = Color(0xFF4CAF50)        // Green for achievements
    val Warning = Color(0xFFFF9800)        // Orange for warnings
    val Error = Color(0xFFF44336)          // Red for errors
    val Info = Color(0xFF2196F3)           // Blue for information
    
    // Fitness-Specific Colors
    val Cardio = Color(0xFFE91E63)         // Pink for cardio
    val Strength = Color(0xFF3F51B5)       // Blue for strength
    val Flexibility = Color(0xFF9C27B0)    // Purple for flexibility
    val Endurance = Color(0xFF009688)      // Teal for endurance
    val Yoga = Color(0xFFFF9800)           // Orange for yoga
    val Recovery = Color(0xFF2196F3)       // Blue for recovery
    
    // Gradient Colors
    val GradientStart = Color(0xFF4ADE80)
    val GradientMiddle = Color(0xFF22C55E)
    val GradientEnd = Color(0xFF10B981)
    
    // Background Gradients
    val BackgroundGradientStart = Color(0xFF1A1A1A)
    val BackgroundGradientEnd = Color(0xFF0A0A0A)
    
    // Text Hierarchy
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0B0B0)
    val TextTertiary = Color(0xFF808080)
    val TextDisabled = Color(0xFF606060)
    
    // Overlay Colors
    val Overlay = Color(0x80000000)        // Semi-transparent black
    val OverlayLight = Color(0x40000000)   // Light overlay
    
    // Border Colors
    val Border = Color(0xFF404040)
    val BorderFocus = Primary
}
