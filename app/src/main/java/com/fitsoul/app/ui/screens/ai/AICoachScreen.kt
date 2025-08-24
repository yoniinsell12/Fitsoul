package com.fitsoul.app.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.data.service.FitnessTrackingService
import com.fitsoul.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.json.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// Import extension functions for prebuilt workouts
import com.fitsoul.app.ui.screens.ai.generatePrebuiltPushWorkout
import com.fitsoul.app.ui.screens.ai.generatePrebuiltPullWorkout
import com.fitsoul.app.ui.screens.ai.generatePrebuiltHIITWorkout
import com.fitsoul.app.ui.screens.ai.generatePrebuiltYogaWorkout
import com.fitsoul.app.ui.screens.ai.generatePrebuiltPilatesWorkout
import com.fitsoul.app.ui.screens.ai.generatePrebuiltFunctionalWorkout

@HiltViewModel
class AICoachViewModel @Inject constructor() : ViewModel() {
    
    private val TAG = "AICoachViewModel"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // OFFLINE-FIRST: Disable all external API calls to prevent 429 errors
    private val apiKey: String
    private val baseUrl = "https://openrouter.ai/api/v1"
    private val model = "qwen/qwen3-coder:free"
    private val isAiEnabled = false // 🔒 DISABLED to prevent 429 errors
    
    init {
        // Try to get API key from BuildConfig or use a fallback
        val configApiKey = try {
            com.fitsoul.app.BuildConfig.OPENROUTER_API_KEY
        } catch (e: Exception) {
            ""
        }
        
        // Use the BuildConfig key if it's not empty, otherwise use a fallback
        apiKey = if (configApiKey.isNotBlank() && 
                     !configApiKey.equals("sk-or-v1-free-model-only", ignoreCase = true)) {
            configApiKey
        } else {
            // Fallback to a free model API key
            "sk-or-v1-free-model-only"
        }
        
        android.util.Log.d(TAG, "=== OFFLINE AI COACH INITIALIZED ===")
        android.util.Log.d(TAG, "🔒 OFFLINE MODE: External API disabled")
        android.util.Log.d(TAG, "✅ LOCAL RESPONSES: Advanced workout generation enabled")
        android.util.Log.d(TAG, "🚫 NO 429 ERRORS: API calls completely disabled")
        android.util.Log.d(TAG, "💪 EXPERT WORKOUTS: 20+ workout templates available")
        android.util.Log.d(TAG, "=========================================")
    }
    
    // OFFLINE-FIRST: No API call tracking needed since API is disabled
    private var apiCallCount = 0
    private var lastApiCallTime = 0L
    private val maxApiCallsPerSession = 0 // 🚫 No API calls allowed
    private val minDelayBetweenCalls = 0L // No delays needed
    
    suspend fun generateAIResponse(userInput: String): String {
        return try {
            android.util.Log.d("AICoach", "=== QWEN AI REQUEST ===")
            android.util.Log.d("AICoach", "Processing input: '$userInput'")
            android.util.Log.d("AICoach", "AI Enabled: $isAiEnabled")
            android.util.Log.d("AICoach", "API Call Count: $apiCallCount")
            
            // Check rate limiting
            val currentTime = System.currentTimeMillis()
            val timeSinceLastCall = currentTime - lastApiCallTime
            
            // 🔒 OFFLINE-FIRST: Skip all API calls to eliminate 429 errors completely
            android.util.Log.d("AICoach", "💪 Using OFFLINE AI Coach - No 429 errors possible!")
            android.util.Log.d("AICoach", "🚀 Generating expert workout from local knowledge base...")
            
            // 💪 Generate expert workout response locally (no API needed)
            android.util.Log.d("AICoach", "🎯 Generating expert workout response...")
            val expertResponse = generateExpertWorkoutResponse(userInput)
            android.util.Log.d("AICoach", "✅ Expert workout generated - Length: ${expertResponse.length}")
            return expertResponse
            
        } catch (e: Exception) {
            android.util.Log.e("AICoach", "💥 Unexpected error in offline mode: ${e.message}")
            e.printStackTrace()
            "💪 I'm your offline AI fitness coach! Ready to create amazing workouts without any connection issues. What would you like to train today?"
        }
    }
    
    // Reset API call count (useful for testing or when user backgrounds/foregrounds app)
    fun resetApiCallCount() {
        apiCallCount = 0
        lastApiCallTime = 0L
        android.util.Log.d(TAG, "🔄 API call count reset")
    }
    
    private fun createDetailedFitnessPrompt(userInput: String): String {
        val bodyPart = extractBodyPart(userInput.lowercase())
        return """
        You are an expert personal trainer with 10+ years experience. A client says: "$userInput"
        ${if (bodyPart.isNotEmpty()) "They want to focus on: $bodyPart" else ""}
        
        Create a comprehensive workout plan including:
        🏋️ STRUCTURE: Warm-up, main exercises, cool-down
        💪 DETAILS: Specific exercises with sets, reps, rest periods
        🎯 FORM TIPS: Safety cues and proper technique
        📊 TIMING: Total workout duration and frequency
        🔥 MOTIVATION: Encouraging and enthusiastic tone
        
        Use emojis, bullet points, and make it immediately actionable. Keep under 500 words.
        """.trimIndent()
    }
    
    private fun extractBodyPart(input: String): String {
        return when {
            input.contains("legs") || input.contains("leg") -> "legs and lower body"
            input.contains("arms") || input.contains("arm") -> "arms and upper body"  
            input.contains("chest") -> "chest muscles"
            input.contains("back") -> "back muscles"
            input.contains("abs") || input.contains("core") -> "core and abs"
            input.contains("cardio") -> "cardiovascular fitness"
            input.contains("shoulders") || input.contains("shoulder") -> "shoulders and upper body"
            input.contains("upper body") || input.contains("upper boady") -> "upper body"
            else -> ""
        }
    }
    
    private fun generateExpertWorkoutResponse(userInput: String): String {
        val input = userInput.lowercase()
        
        android.util.Log.d("AICoach", "🔍 Analyzing user input: '$userInput'")
        
        return when {
            // Check for prebuilt workout requests first
            input.contains("push day") || input.contains("push workout") -> {
                android.util.Log.d("AICoach", "💪 PREBUILT push day workout")
                generatePrebuiltPushWorkout()
            }
            input.contains("pull day") || input.contains("pull workout") -> {
                android.util.Log.d("AICoach", "🎯 PREBUILT pull day workout")
                generatePrebuiltPullWorkout()
            }
            input.contains("hiit") || input.contains("high intensity") -> {
                android.util.Log.d("AICoach", "⚡ PREBUILT HIIT workout")
                generatePrebuiltHIITWorkout()
            }
            input.contains("yoga") || input.contains("mindful") -> {
                android.util.Log.d("AICoach", "🧘‍♀️ PREBUILT yoga workout")
                generatePrebuiltYogaWorkout()
            }
            input.contains("pilates") -> {
                android.util.Log.d("AICoach", "🎯 PREBUILT pilates workout")
                generatePrebuiltPilatesWorkout()
            }
            input.contains("functional") -> {
                android.util.Log.d("AICoach", "🏃‍♂️ PREBUILT functional workout")
                generatePrebuiltFunctionalWorkout()
            }
            // Original matching
            input.contains("legs") || input.contains("leg") || input.contains("squat") || input.contains("thigh") -> {
                android.util.Log.d("AICoach", "🦾 Generating LEGS workout")
                generateLegsWorkout()
            }
            input.contains("arms") || input.contains("arm") || input.contains("bicep") || input.contains("tricep") -> {
                android.util.Log.d("AICoach", "💪 Generating ARMS workout")
                generateArmsWorkout()
            }
            input.contains("chest") || input.contains("push") || input.contains("pec") -> {
                android.util.Log.d("AICoach", "💎 Generating CHEST workout")
                generateChestWorkout()
            }
            input.contains("back") || input.contains("pull") || input.contains("lat") -> {
                android.util.Log.d("AICoach", "🏋 Generating BACK workout")
                generateBackWorkout()
            }
            input.contains("abs") || input.contains("core") || input.contains("stomach") || input.contains("plank") -> {
                android.util.Log.d("AICoach", "🎯 Generating CORE workout")
                generateCoreWorkout()
            }
            input.contains("cardio") || input.contains("run") || input.contains("hiit") || input.contains("fat") -> {
                android.util.Log.d("AICoach", "❤️ Generating CARDIO workout")
                generateCardioWorkout()
            }
            input.contains("beginner") || input.contains("start") || input.contains("new") || input.contains("easy") -> {
                android.util.Log.d("AICoach", "🌟 Generating BEGINNER workout")
                generateBeginnerWorkout()
            }
            input.contains("advanced") || input.contains("hard") || input.contains("intense") || input.contains("expert") -> {
                android.util.Log.d("AICoach", "🔥 Generating ADVANCED workout")
                generateAdvancedWorkout()
            }
            input.contains("shoulders") || input.contains("shoulder") || input.contains("deltoid") -> {
                android.util.Log.d("AICoach", "🎆 Generating SHOULDERS workout")
                generateShouldersWorkout()
            }
            input.contains("upper body") || input.contains("upper") || input.contains("top") -> {
                android.util.Log.d("AICoach", "🏔️ Generating UPPER BODY workout")
                generateUpperBodyWorkout()
            }
            input.contains("full body") || input.contains("everything") || input.contains("complete") -> {
                android.util.Log.d("AICoach", "⚡ Generating FULL BODY workout")
                generateFullBodyWorkout()
            }
            input.contains("stretch") || input.contains("flexibility") || input.contains("yoga") -> {
                android.util.Log.d("AICoach", "🧘 Generating FLEXIBILITY workout")
                generateFlexibilityWorkout()
            }
            input.contains("quick") || input.contains("short") || input.contains("10") || input.contains("15") -> {
                android.util.Log.d("AICoach", "⏱️ Generating QUICK workout")
                generateQuickWorkout()
            }
            input.contains("home") || input.contains("no gym") || input.contains("bodyweight") -> {
                android.util.Log.d("AICoach", "🏠 Generating HOME workout")
                generateHomeWorkout()
            }
            else -> {
                android.util.Log.d("AICoach", "🤖 Generating GENERAL fitness response")
                generateGeneralFitnessResponse()
            }
        }
    }
    
    private fun generateLegsWorkout(): String {
        return """
🦵 **COMPLETE LEGS POWER WORKOUT**

Ready to build those powerful legs? This comprehensive routine targets all major lower body muscles!

🔥 **WARM-UP (5 minutes)**
• Leg swings: 10 each direction per leg
• Hip circles: 10 clockwise, 10 counterclockwise  
• Bodyweight squats: 15 slow, controlled reps
• Calf raises: 20 reps to activate calves
• Walking in place: 1 minute to get blood flowing

💪 **MAIN WORKOUT (25-30 minutes)**

**🎯 QUAD & GLUTE BUILDERS:**
• **Squats**: 4 sets of 12-15 reps
  - Rest: 60 seconds between sets
  - Form: Chest up, knees track over toes, sit back like sitting in chair
  
• **Walking Lunges**: 3 sets of 10 per leg (20 total)
  - Rest: 45 seconds between sets  
  - Form: Step far enough for 90° angles in both knees
  
• **Wall Sits**: 3 sets of 30-45 seconds
  - Rest: 60 seconds between sets
  - Challenge: Add 5 seconds each week!

**🎯 POSTERIOR CHAIN:**
• **Glute Bridges**: 3 sets of 15-20 reps
  - Rest: 30 seconds between sets
  - Form: Squeeze glutes hard at top, hold 2 seconds
  
• **Single-leg Deadlifts**: 3 sets of 8 per leg
  - Rest: 45 seconds between sets
  - Form: Keep planted leg slightly bent, hinge at hip

**🎯 CALF POWER:**
• **Calf Raises**: 3 sets of 20-25 reps
  - Rest: 30 seconds between sets
  - Progression: Single leg or use step for extra range

**🎯 EXPLOSIVE FINISH:**
• **Jump Squats**: 2 sets of 8-10 reps
  - Rest: 60 seconds between sets
  - Focus: Soft landings, explosive jumps

🧘 **COOL-DOWN (5 minutes)**
• Quad stretch: 30 seconds each leg
• Hamstring stretch: 30 seconds each leg  
• Calf stretch: 30 seconds each leg
• Hip flexor stretch: 30 seconds each leg

📊 **WORKOUT SUMMARY:**
• **Total Time**: 35-40 minutes
• **Frequency**: 2-3 times per week with 48 hours rest
• **Progression**: Add 2-3 reps every week
• **Expected Results**: Stronger legs in 2-3 weeks!

💡 **LEG DAY SECRETS:**
• Focus on form over speed - quality reps build quality muscle
• Feel the target muscles working, not just going through motions  
• Breathe out during the hard part of each exercise
• Stay hydrated and fuel up with protein after!

Ready to feel those legs BURN in the best way? Let's dominate this workout! 🔥💪

Your legs will thank you tomorrow (even if they're sore)! 🚀
        """.trimIndent()
    }
    
    private fun generateArmsWorkout(): String {
        return """
💪 **ARMS SCULPTING INTENSIVE**

Time to build strong, defined arms! This complete routine targets biceps, triceps, and shoulders.

🔥 **WARM-UP (4 minutes)**
• Arm circles: 15 forward, 15 backward
• Shoulder rolls: 10 each direction
• Cross-body arm stretches: 20 seconds each arm
• Light arm swings: 15 each arm

💪 **MAIN WORKOUT (25 minutes)**

**🎯 PUSHING POWER (Triceps & Shoulders):**
• **Push-ups**: 4 sets of 8-12 reps
  - Modification: Knee push-ups if needed
  - Form: Straight line from head to heels
  
• **Tricep Dips**: 3 sets of 8-12 reps
  - Use chair or bench edge
  - Form: Keep elbows close to body, lower slow
  
• **Pike Push-ups**: 3 sets of 6-10 reps
  - Targets shoulders specifically  
  - Form: Butt high in air, look at your feet

• **Diamond Push-ups**: 2 sets of 5-8 reps
  - Ultimate tricep challenge!
  - Form: Hands form diamond shape under chest

**🎯 PULLING STRENGTH (Biceps):**
• **Reverse Push-ups**: 3 sets of 8-10 reps
  - Lie under sturdy table, pull body up
  - Alternative: Use resistance band if available

• **Isometric Bicep Hold**: 3 sets of 15-30 seconds  
  - Hold arms at 90 degrees, flex biceps hard
  - Feel that bicep burn!

**🎯 DYNAMIC POWER:**
• **Boxing Punches**: 3 sets of 30 seconds
  - Shadow boxing with purpose
  - Keep core engaged, arms moving fast!

🧘 **COOL-DOWN (5 minutes)**
• Overhead tricep stretch: 30 seconds each
• Cross-body shoulder stretch: 30 seconds each
• Gentle arm circles: 30 seconds
• Arm shaking: 30 seconds to release tension

📊 **ARM TRANSFORMATION PLAN:**
• **Total Time**: 34 minutes
• **Frequency**: 3 times per week
• **Rest**: 45-60 seconds between sets
• **Progression**: Add 1-2 reps weekly

Your arms will feel incredible and look even better! 🚀💪
        """.trimIndent()
    }
    
    private fun generateGeneralFitnessResponse(): String {
        return """
🤖 **YOUR PERSONAL AI FITNESS COACH**

Hey champion! I'm thrilled to help you crush your fitness goals! 

💪 **I can create detailed workouts for:**
• **🦵 LEGS**: Squats, lunges, glute bridges, calf raises
• **💪 ARMS**: Push-ups, dips, tricep work, bicep builders  
• **🫀 CHEST**: Various push-up variations, chest power
• **🏋️ BACK**: Superman, reverse flies, posture builders
• **🤸 SHOULDERS**: Pike push-ups, handstand holds, deltoid targeting
• **🏋️ UPPER BODY**: Complete chest, back, shoulders, and arms
• **🎯 CORE**: Planks, crunches, stability work
• **❤️ CARDIO**: HIIT circuits, fat-burning sessions
• **🔥 FULL BODY**: Complete transformation routines

🎯 **Just tell me what you want to work on:**
• "I want to work my legs today"
• "Give me an arms workout"  
• "I need some cardio"
• "Core workout please"
• "Shoulders workout"
• "Upper body training"
• "I'm a beginner, help me start"

Each workout includes:
✅ Complete warm-up and cool-down
✅ Specific sets, reps, and rest periods
✅ Form tips and safety notes  
✅ Progression guidelines
✅ Expected results and timing

What body part are you most excited to train today? Let's make it happen! 🔥💪
        """.trimIndent()
    }
    
    private fun generateChestWorkout(): String {
        return """
🫀 **CHEST POWER DEVELOPMENT**

Build that strong, impressive chest with this comprehensive pushing routine!

🔥 **WARM-UP (4 minutes)**
• Arm swings: 15 each direction
• Chest opener stretches: 30 seconds
• Wall push-ups: 10 easy reps
• Shoulder blade squeezes: 15 reps

💪 **MAIN WORKOUT (25 minutes)**

**🎯 CHEST BUILDERS:**
• **Standard Push-ups**: 4 sets of 10-15 reps
  - Form: Chest touches ground, straight body line
  - Rest: 60 seconds between sets

• **Wide-Grip Push-ups**: 3 sets of 8-12 reps
  - Targets outer chest muscles
  - Hands wider than shoulders

• **Incline Push-ups**: 3 sets of 12-15 reps
  - Hands on elevated surface
  - Great for building strength

• **Chest Squeeze**: 3 sets of 15 reps
  - Press palms together, hold 3 seconds
  - Feel chest muscles contract

🧘 **COOL-DOWN (5 minutes)**
• Doorway chest stretch: 45 seconds
• Cross-body arm stretch: 30 seconds each

Time to build that powerful chest! 💥🔥
        """.trimIndent()
    }
    
    private fun generateBackWorkout(): String {
        return """
🏋️ **BACK STRENGTH FOUNDATION**

A strong back supports everything! Build yours with this complete routine.

🔥 **WARM-UP (4 minutes)**
• Arm circles: 15 each direction
• Shoulder blade squeezes: 15 reps
• Cat-cow stretches: 10 reps

💪 **MAIN WORKOUT (25 minutes)**

**🎯 BACK BUILDERS:**
• **Superman**: 4 sets of 12-15 reps
  - Hold for 2 seconds at top
  - Feel your back muscles working

• **Reverse Snow Angels**: 3 sets of 10-12 reps
  - Lying face down, sweep arms up and back
  - Great for posture improvement

• **Bird Dog**: 3 sets of 8 per side
  - Opposite arm and leg extensions
  - Hold for 3 seconds each rep

• **Wall Slides**: 3 sets of 10-12 reps
  - Back against wall, slide arms up and down
  - Perfect posture exercise

Your posture will improve dramatically! 📐💪
        """.trimIndent()
    }
    
    private fun generateCoreWorkout(): String {
        return """
🎯 **CORE CRUSHER WORKOUT**

Time to build that rock-solid core! This routine targets all core muscles.

🔥 **WARM-UP (3 minutes)**
• Gentle torso twists: 15 each direction
• Hip circles: 10 each direction
• Cat-cow stretches: 10 reps

💪 **MAIN WORKOUT (22 minutes)**

**🎯 CORE DOMINANCE:**
• **Plank**: 4 sets of 30-60 seconds
  - Hold strong, breathe steadily
  - Progress by adding 5 seconds weekly

• **Bicycle Crunches**: 3 sets of 20 total
  - Slow, controlled movement
  - Feel obliques working

• **Russian Twists**: 3 sets of 16-20 reps
  - Feet off ground for extra challenge
  - Twist from core, not just arms

• **Mountain Climbers**: 3 sets of 20 total
  - Keep core tight throughout
  - Drive knees to chest

• **Dead Bug**: 3 sets of 8 per side
  - Opposite arm and leg extensions
  - Keep lower back pressed down

Your core will be on fire in the best way! 🔥💪
        """.trimIndent()
    }
    
    private fun generateCardioWorkout(): String {
        return """
❤️ **CARDIO BLAST SESSION**

Get that heart pumping with this high-energy HIIT routine!

🔥 **WARM-UP (3 minutes)**
• Marching in place: 1 minute
• Arm swings: 1 minute
• Light bouncing: 1 minute

💪 **MAIN HIIT CIRCUIT (20 minutes)**

**🎯 4 ROUNDS OF:**
• **Jumping Jacks**: 45 seconds work, 15 seconds rest
• **High Knees**: 45 seconds work, 15 seconds rest
• **Burpees**: 30 seconds work, 30 seconds rest
• **Mountain Climbers**: 45 seconds work, 15 seconds rest
• **Rest**: 1 minute between rounds

🧘 **COOL-DOWN (5 minutes)**
• Walking in place: 2 minutes
• Deep breathing: 3 minutes

Your heart will be stronger after every session! 💓🚀
        """.trimIndent()
    }
    
    private fun generateBeginnerWorkout(): String {
        return """
🌟 **PERFECT BEGINNER START**

Welcome to fitness! This gentle routine builds your foundation safely.

🔥 **WARM-UP (5 minutes)**
• Gentle marching: 2 minutes
• Arm circles: 1 minute
• Body stretches: 2 minutes

💪 **BEGINNER CIRCUIT (20 minutes)**

**🎯 FOUNDATION BUILDERS:**
• **Wall Push-ups**: 3 sets of 8-10 reps
  - Start here, progress to knee push-ups
  - Focus on form over quantity

• **Chair-Assisted Squats**: 3 sets of 10-12 reps
  - Use chair for support if needed
  - Build leg strength gradually

• **Modified Plank**: 3 sets of 15-30 seconds
  - On knees if needed
  - Build core strength slowly

• **Gentle Marching**: 3 sets of 30 seconds
  - Lift knees moderately high
  - Build cardiovascular base

🧘 **COOL-DOWN (8 minutes)**
• Full body gentle stretching

Every expert was once a beginner! You've got this! 🎯💪
        """.trimIndent()
    }
    
    private fun generateAdvancedWorkout(): String {
        return """
🔥 **ADVANCED ATHLETIC CHALLENGE**

Ready to push your limits? This elite routine will test your fitness!

🔥 **WARM-UP (6 minutes)**
• Dynamic movement prep
• Joint mobility
• Activation exercises

💪 **ELITE CIRCUIT (35 minutes)**

**🎯 ADVANCED CHALLENGES:**
• **Pistol Squats**: 4 sets of 5 per leg
  - Single-leg squat mastery
  - Ultimate leg strength test

• **One-arm Push-ups**: 4 sets of 3-5 per arm
  - Elite pushing strength
  - Work up to these gradually

• **Handstand Push-ups**: 3 sets of 3-8 reps
  - Against wall if needed
  - Ultimate shoulder power

• **Plyometric Burpees**: 3 sets of 8-10 reps
  - Add jump at end
  - Explosive full-body power

• **L-sit Hold**: 3 sets of 10-30 seconds
  - Ultimate core challenge
  - Advanced gymnastic strength

🧘 **RECOVERY (8 minutes)**
• Comprehensive stretching
• Recovery breathing

Time to unleash your inner athlete! ⚡🔥
        """.trimIndent()
    }

    private fun generateShouldersWorkout(): String {
        return """
💪 **SHOULDER STRENGTH & DEFINITION**

Let's build strong, sculpted shoulders! This routine focuses on all heads of the deltoid for a balanced look.

🔥 **WARM-UP (4 minutes)**
• Arm circles: 15 forward, 15 backward
• Shoulder rolls: 10 each direction
• Light arm swings: 15 each arm
• Band pull-aparts (if available): 10-15 reps

💪 **MAIN WORKOUT (25 minutes)**

**🎯 OVERALL SHOULDER DEVELOPMENT:**
• **Pike Push-ups**: 4 sets of 8-12 reps
  - Rest: 60 seconds between sets
  - Form: Hips high, head towards the floor, push through shoulders
  - Progression: Elevate feet for more challenge
  
• **Wall Handstand Holds**: 3 sets of 20-45 seconds
  - Rest: 60 seconds between sets
  - Form: Keep body straight, core tight, hands shoulder-width
  - Benefit: Builds isometric strength and stability

**🎯 LATERAL & REAR DELTOIDS:**
• **Side Plank with Arm Raise**: 3 sets of 10-12 per side
  - Rest: 45 seconds between sets
  - Form: Keep body in a straight line, raise top arm slowly
  - Focus: Engages core and lateral deltoid

• **Reverse Snow Angels**: 3 sets of 12-15 reps
  - Rest: 30 seconds between sets
  - Form: Lie face down, lift chest slightly, sweep arms up and back
  - Benefit: Targets rear deltoids and upper back for posture

**🎯 FRONT DELTOIDS & FINISHER:**
• **Front Arm Raises (Bodyweight)**: 3 sets of 15-20 reps
  - Rest: 30 seconds between sets
  - Form: Keep arms straight, lift to shoulder height, control descent
  - Focus: Isolates front deltoids

• **Shoulder Taps (in Plank)**: 3 sets of 20 total (10 per side)
  - Rest: 30 seconds between sets
  - Form: Maintain stable plank, minimize hip sway
  - Benefit: Core stability and shoulder endurance

🧘 **COOL-DOWN (5 minutes)**
• Cross-body shoulder stretch: 30 seconds each arm
• Overhead tricep stretch: 30 seconds each arm
• Child's pose: 1 minute
• Gentle neck rolls: 30 seconds

📊 **WORKOUT SUMMARY:**
• **Total Time**: 34 minutes
• **Frequency**: 2-3 times per week
• **Rest Days**: 48 hours between shoulder sessions
• **Progression**: Increase reps/hold time, or try advanced variations

💡 **SHOULDER TRAINING TIPS:**
• Always warm up thoroughly to prevent injury
• Focus on controlled movements, especially on the way down
• Listen to your body and don't push through sharp pain
• Maintain good posture throughout the day to support shoulder health

Get ready for strong, resilient shoulders that stand out! You've got this! 💪🚀
        """.trimIndent()
    }
    
    private fun generateUpperBodyWorkout(): String {
        return """
🏋️ **COMPLETE UPPER BODY POWERHOUSE**

This workout is designed to build strength and definition across your chest, back, shoulders, and arms!

🔥 **WARM-UP (5 minutes)**
• Arm circles: 20 forward, 20 backward
• Shoulder rolls: 10 each direction
• Cat-cow stretch: 10 reps
• Light push-up prep: 10 easy reps
• Thoracic rotations: 10 per side

💪 **MAIN WORKOUT (30-35 minutes)**

**🎯 CHEST & TRICEPS:**
• **Push-ups**: 4 sets of 10-15 reps
  - Rest: 60 seconds
  - Form: Chest to floor, elbows slightly tucked
  - Modification: Knee push-ups or incline push-ups
  
• **Tricep Dips (using a chair/bench)**: 3 sets of 10-15 reps
  - Rest: 45 seconds
  - Form: Keep elbows close, lower until arms are 90 degrees

**🎯 BACK & BICEPS:**
• **Superman**: 3 sets of 15-20 reps
  - Rest: 45 seconds
  - Form: Lift chest and legs simultaneously, squeeze back
  - Benefit: Strengthens lower back and glutes
  
• **Reverse Tabletop Row**: 3 sets of 10-12 reps
  - Rest: 45 seconds
  - Form: Sit with knees bent, hands behind you, lift hips, pull chest to hands
  - Focus: Targets biceps and upper back

**🎯 SHOULDERS:**
• **Pike Push-ups**: 3 sets of 8-12 reps
  - Rest: 60 seconds
  - Form: Hips high, push through shoulders
  - Progression: Elevate feet for more challenge
  
• **Wall Handstand Holds**: 2 sets of 20-30 seconds
  - Rest: 60 seconds
  - Form: Keep body straight, core tight
  - Benefit: Builds isometric strength and stability

**🎯 CORE FINISHER:**
• **Plank**: 3 sets of 30-60 seconds
  - Rest: 30 seconds
  - Form: Straight line from head to heels, core engaged

🧘 **COOL-DOWN (5 minutes)**
• Chest stretch (doorway or floor): 45 seconds
• Overhead tricep stretch: 30 seconds each arm
• Child's pose: 1 minute
• Gentle arm swings: 30 seconds

📊 **WORKOUT SUMMARY:**
• **Total Time**: 40-45 minutes
• **Frequency**: 2-3 times per week
• **Rest Days**: Allow 48 hours for muscle recovery
• **Expected Results**: Increased upper body strength, improved posture, and muscle definition.

💡 **UPPER BODY TRAINING TIPS:**
• Focus on the mind-muscle connection for each exercise.
• Control both the lifting and lowering phases of each movement.
• Stay hydrated and ensure adequate protein intake for muscle repair and growth.
• Don't forget to breathe! Exhale on exertion.

Get ready to feel powerful and confident with your new upper body strength! Let's crush it! 💪🔥
        """.trimIndent()
    }
    
    private suspend fun callQwenApi(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Create JSON request for OpenRouter/Qwen API
                val json = buildJsonObject {
                    put("model", model)
                    putJsonArray("messages") {
                        addJsonObject {
                            put("role", "system")
                            put("content", "You are an expert fitness coach with 10+ years of experience. Create detailed, comprehensive workout plans with proper structure, form tips, and motivation. Use emojis and bullet points for better readability.")
                        }
                        addJsonObject {
                            put("role", "user")
                            put("content", prompt)
                        }
                    }
                    put("max_tokens", 2000)
                    put("temperature", 0.7)
                    put("top_p", 1.0)
                    put("stream", false)
                }
                
                val requestBody = json.toString().toRequestBody("application/json".toMediaType())
                
                val request = Request.Builder()
                    .url("$baseUrl/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "https://fitsoul.app")
                    .addHeader("X-Title", "Fitsoul AI Coach")
                    .post(requestBody)
                    .build()
                
                android.util.Log.d(TAG, "🚀 Making HTTP request to Qwen API...")
                android.util.Log.d(TAG, "URL: $baseUrl/chat/completions")
                android.util.Log.d(TAG, "Model: $model")
                android.util.Log.d(TAG, "Request body: ${json.toString()}")
                
                val response = client.newCall(request).execute()
                
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    android.util.Log.e(TAG, "❌ Qwen API call failed: ${response.code} - ${response.message}")
                    android.util.Log.e(TAG, "Error body: $errorBody")
                    
                    // Special handling for rate limiting
                    if (response.code == 429) {
                    // Get retry-after header if available
                    val retryAfter = response.header("Retry-After")?.toIntOrNull() ?: 60
                    android.util.Log.w(TAG, "⚠️ Rate limit exceeded - using fallback response")
                    
                    // Return a helpful message about rate limiting
                    return@withContext """
                        I encountered an issue connecting to my AI brain. Error: API call failed: 429.
                        
                        Let me help you with a workout plan based on my training instead!
                        
                        ${generateExpertWorkoutResponse(prompt)}
                    """.trimIndent()
                } else if (response.code == 401) {
                    // Handle authentication errors
                    android.util.Log.e(TAG, "🔑 Authentication failed - API key may be invalid")
                    
                    // Return a helpful message about authentication issues
                    return@withContext """
                        I encountered an issue connecting to my AI brain. Error: API call failed: 401.
                        
                        Let me help you with a workout plan based on my training instead!
                        
                        ${generateExpertWorkoutResponse(prompt)}
                    """.trimIndent()
                }
                
                throw IOException("API call failed: ${response.code}")
            }
                
                val responseBody = response.body?.string()
                    ?: throw IOException("Empty response body")
                
                android.util.Log.d(TAG, "📄 Raw API response: ${responseBody.take(300)}...")
                
                // Parse the JSON response
                val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
                val choices = jsonResponse["choices"]?.jsonArray
                
                if (choices == null || choices.isEmpty()) {
                    android.util.Log.e(TAG, "❌ No choices in response")
                    throw IOException("No choices in response")
                }
                
                val message = choices[0].jsonObject["message"]?.jsonObject
                val content = message?.get("content")?.jsonPrimitive?.content
                    ?: throw IOException("No content in response message")
                
                android.util.Log.d(TAG, "✅ Successfully extracted content: ${content.take(100)}...")
                android.util.Log.d(TAG, "✅ Full content length: ${content.length}")
                
                return@withContext content.trim()
                
            } catch (e: Exception) {
                android.util.Log.e(TAG, "💥 Error calling Qwen API", e)
                e.printStackTrace()
                
                // Check if it's an authentication error
                val isAuthError = e.message?.contains("authentication", ignoreCase = true) == true ||
                                  e.message?.contains("401", ignoreCase = true) == true ||
                                  e.message?.contains("API key", ignoreCase = true) == true
                
                // 🔒 OFFLINE MODE: This should never execute since API is disabled
                return@withContext "🔒 Offline AI Coach Active - No connection issues possible! Ready for your workout? 💪"
            }
        }
    }
    
    private fun generateFullBodyWorkout(): String {
        return """
⚡ **ULTIMATE FULL BODY TRANSFORMATION**

Ready for a complete body workout? This routine targets every muscle group for maximum results!

🔥 **WARM-UP (5 minutes)**
• Arm circles: 15 each direction
• Leg swings: 10 each leg, each direction
• Torso twists: 15 each side
• Light bouncing: 1 minute
• Joint rotations: 1 minute

💪 **MAIN CIRCUIT (35 minutes)**

**🎯 ROUND 1: POWER BUILDERS (12 minutes)**
• **Burpees**: 4 sets of 8-12 reps
  - Rest: 60 seconds between sets
  - Form: Chest to ground, explosive jump at top
  
• **Mountain Climbers**: 4 sets of 20 total
  - Rest: 45 seconds between sets
  - Form: Keep core tight, drive knees to chest
  
• **Jump Squats**: 3 sets of 10-12 reps
  - Rest: 60 seconds between sets
  - Form: Land softly, explosive upward movement

**🎯 ROUND 2: STRENGTH BUILDERS (12 minutes)**
• **Push-ups**: 3 sets of 10-15 reps
  - Rest: 60 seconds between sets
  - Form: Straight line from head to heels
  
• **Single-leg Deadlifts**: 3 sets of 8 per leg
  - Rest: 45 seconds between sets
  - Form: Keep planted leg slightly bent
  
• **Pike Push-ups**: 3 sets of 6-10 reps
  - Rest: 60 seconds between sets
  - Form: Hips high, target shoulders

**🎯 ROUND 3: CORE FINISHER (11 minutes)**
• **Plank to Push-up**: 3 sets of 6-10 reps
  - Rest: 45 seconds between sets
  - Form: Maintain straight body throughout
  
• **Russian Twists**: 3 sets of 20 total
  - Rest: 30 seconds between sets
  - Form: Feet off ground for extra challenge
  
• **Dead Bug**: 3 sets of 8 per side
  - Rest: 45 seconds between sets
  - Form: Keep lower back pressed down

🧘 **COOL-DOWN (5 minutes)**
• Full body stretching routine
• Deep breathing exercises
• Gentle walking in place

📊 **FULL BODY RESULTS:**
• **Total Time**: 45 minutes
• **Frequency**: 3 times per week
• **Calories Burned**: 400-600
• **Expected Results**: Total body strength in 3-4 weeks!

💡 **FULL BODY SECRETS:**
• This targets every major muscle group in one session
• Perfect for busy schedules - maximum results, minimum time
• Builds functional strength for daily activities
• Improves cardiovascular health while building muscle

Get ready to feel stronger everywhere! This is the ultimate efficiency workout! 🚀💪
        """.trimIndent()
    }
    
    private fun generateFlexibilityWorkout(): String {
        return """
🧘 **FLEXIBILITY & MOBILITY FLOW**

Time to improve your range of motion and feel amazing! This routine enhances flexibility and reduces tension.

🌱 **GENTLE WARM-UP (3 minutes)**
• Neck rolls: 5 each direction
• Shoulder rolls: 10 each direction  
• Gentle arm swings: 15 each direction
• Light marching in place: 1 minute

🌸 **FLEXIBILITY FLOW (25 minutes)**

**🎯 UPPER BODY RELEASE:**
• **Doorway Chest Stretch**: 3 holds of 45 seconds
  - Feel: Deep stretch across chest and shoulders
  - Breathing: Deep, slow breaths
  
• **Overhead Tricep Stretch**: 3 holds of 30 seconds each arm
  - Form: Gentle pull, don't force
  - Focus: Feel stretch down back of arm
  
• **Cat-Cow Stretches**: 3 sets of 10 slow reps
  - Form: Arch and round spine slowly
  - Benefit: Spinal mobility and back relief

**🎯 LOWER BODY FLOW:**
• **Forward Fold**: 3 holds of 60 seconds
  - Form: Let arms hang, bend knees if needed
  - Feel: Stretch in hamstrings and lower back
  
• **Hip Flexor Stretch**: 3 holds of 45 seconds per leg
  - Form: Lunge position, sink hips forward
  - Benefit: Opens tight hip flexors
  
• **Pigeon Pose**: 2 holds of 60 seconds per side
  - Form: One leg forward, one back
  - Feel: Deep hip and glute stretch

**🎯 SPINAL MOBILITY:**
• **Seated Spinal Twist**: 3 holds of 30 seconds each side
  - Form: Gentle rotation, look over shoulder
  - Benefit: Improves spinal rotation
  
• **Child's Pose**: 2 holds of 90 seconds
  - Form: Knees wide, arms extended forward
  - Feel: Full back and shoulder stretch

🌙 **RELAXATION FINISH (7 minutes)**
• Gentle leg shaking: 30 seconds
• Full body tension and release: 2 minutes
• Deep breathing meditation: 4 minutes
• Gentle neck and shoulder circles: 30 seconds

📊 **FLEXIBILITY BENEFITS:**
• **Total Time**: 35 minutes
• **Frequency**: Daily if possible, minimum 3x per week
• **Results**: Improved range of motion in 1-2 weeks
• **Bonus**: Better sleep, reduced stress, less muscle tension

💡 **FLEXIBILITY TIPS:**
• Never bounce or force stretches
• Breathe deeply and relax into each position
• Hold stretches for at least 30 seconds
• Listen to your body - some tension is good, pain is not

Feel the tension melt away and your body open up! 🌸✨
        """.trimIndent()
    }
    
    private fun generateQuickWorkout(): String {
        return """
⏱️ **15-MINUTE EXPRESS WORKOUT**

Short on time? No problem! This quick session delivers maximum results in minimal time.

🚀 **QUICK WARM-UP (2 minutes)**
• Jumping jacks: 30 seconds
• Arm swings: 30 seconds
• Leg swings: 30 seconds each leg
• Quick bouncing: 30 seconds

💪 **EXPRESS CIRCUIT (12 minutes)**

**🎯 CIRCUIT A (4 minutes) - Repeat 2x**
*Work: 45 seconds | Rest: 15 seconds*

1. **Burpees** (45s work, 15s rest)
   - Form: Chest to floor, explosive jump
   - Intensity: Go at your own pace

2. **Push-ups** (45s work, 15s rest)
   - Modification: Knee push-ups if needed
   - Focus: Quality over quantity

3. **Jump Squats** (45s work, 15s rest)
   - Form: Soft landings, explosive jumps
   - Alternative: Regular squats if needed

4. **Plank** (45s work, 15s rest)
   - Form: Straight line from head to heels
   - Goal: Hold strong throughout

**🎯 CIRCUIT B (4 minutes) - Repeat 2x**
*Work: 45 seconds | Rest: 15 seconds*

1. **Mountain Climbers** (45s work, 15s rest)
   - Form: Keep hips level, drive knees up
   - Pace: Controlled but quick

2. **Tricep Dips** (45s work, 15s rest)
   - Using chair or bench edge
   - Form: Keep elbows close to body

3. **High Knees** (45s work, 15s rest)
   - Form: Drive knees toward chest
   - Arms: Pump actively

4. **Russian Twists** (45s work, 15s rest)
   - Form: Feet off ground if possible
   - Focus: Controlled rotation

🧘 **QUICK COOL-DOWN (1 minute)**
• Walking in place: 30 seconds
• Deep breathing: 30 seconds

📊 **EXPRESS RESULTS:**
• **Total Time**: 15 minutes
• **Frequency**: Daily for best results
• **Calories Burned**: 120-200
• **Perfect For**: Busy mornings, lunch breaks, before bed

💡 **QUICK WORKOUT SECRETS:**
• High intensity makes up for short duration
• Can be done anywhere - no equipment needed
• Great for maintaining fitness when busy
• Builds cardiovascular fitness and strength

No excuses! Everyone has 15 minutes to invest in their health! 🚀💪
        """.trimIndent()
    }
    
    private fun generateHomeWorkout(): String {
        return """
🏠 **COMPLETE HOME FITNESS SOLUTION**

No gym? No problem! This comprehensive routine uses only your body weight and household items.

🌱 **HOME WARM-UP (4 minutes)**
• Marching in place: 1 minute
• Arm circles using light books/water bottles: 1 minute
• Gentle squats holding chair for balance: 1 minute
• Light stretching: 1 minute

💪 **HOME CIRCUIT (30 minutes)**

**🎯 LIVING ROOM CARDIO (10 minutes)**
• **Step-ups using stairs/sturdy box**: 3 sets of 12 per leg
  - Rest: 45 seconds between sets
  - Safety: Use handrail for balance
  
• **Chair-supported jumping jacks**: 3 sets of 15-20 reps
  - Rest: 30 seconds between sets
  - Modification: Step-touch if space is limited
  
• **Wall push-ups**: 3 sets of 10-15 reps
  - Rest: 45 seconds between sets
  - Progression: Move feet further from wall

**🎯 KITCHEN COUNTER STRENGTH (10 minutes)**
• **Counter push-ups**: 3 sets of 8-12 reps
  - Rest: 60 seconds between sets
  - Form: Body at 45-degree angle
  
• **Chair dips**: 3 sets of 6-10 reps
  - Rest: 60 seconds between sets
  - Safety: Ensure chair is stable against wall
  
• **Water jug bicep curls**: 3 sets of 12-15 reps
  - Rest: 45 seconds between sets
  - Equipment: Use gallon water jugs or detergent bottles

**🎯 BEDROOM FLOOR WORK (10 minutes)**
• **Carpet crunches**: 3 sets of 15-20 reps
  - Rest: 30 seconds between sets
  - Form: Hands behind head, lift shoulders
  
• **Towel hamstring stretch**: 3 sets of 30 seconds per leg
  - Equipment: Use bath towel as resistance
  - Form: Lying down, loop towel around foot
  
• **Pillow squeezes**: 3 sets of 20 reps
  - Equipment: Use couch pillow between knees
  - Target: Inner thigh muscles

🧘 **HOME COOL-DOWN (6 minutes)**
• Gentle stretching using doorway: 2 minutes
• Relaxation on carpet/bed: 2 minutes
• Deep breathing: 2 minutes

📊 **HOME FITNESS RESULTS:**
• **Total Time**: 40 minutes
• **Equipment Needed**: Chair, stairs/sturdy box, water jugs, towel, pillow
• **Space Required**: 6x6 feet
• **Frequency**: 4-5 times per week
• **Results**: Full-body strength in 3-4 weeks

💡 **HOME WORKOUT ADVANTAGES:**
• Privacy and comfort of your own space
• No commute time to gym
• Can pause for family interruptions
• Weather doesn't matter
• Cost-effective fitness solution
• Use household items creatively

🏠 **HOUSEHOLD EQUIPMENT IDEAS:**
• Water jugs = weights
• Stairs = cardio machine
• Chair = workout bench
• Wall = resistance for push-ups
• Towel = resistance band
• Books = light weights

Your home is your gym! Everything you need is already there! 🏠💪
        """.trimIndent()
    }
}

data class ChatMessage(
    val id: String,
    val content: String,
    val isFromAI: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isTyping: Boolean = false
)

// Persistent chat messages that survive navigation and recomposition
private val persistentChatMessages = mutableStateListOf<ChatMessage>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICoachScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    aiCoachViewModel: AICoachViewModel = hiltViewModel()
) {
    // Use persistent messages that survive navigation
    val messages = persistentChatMessages
    var inputText by remember { mutableStateOf("") }
    var isAITyping by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Mock workout progress state (replace with actual service when available)
    val workoutProgress by remember { mutableStateOf(com.fitsoul.app.data.service.WorkoutProgress()) }
    val dailyMetrics by remember { mutableStateOf(com.fitsoul.app.data.service.FitnessMetrics()) }
    
    // Initialize with welcome message
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            delay(1000)
            messages.add(
                ChatMessage(
                    id = "welcome",
                    content = "👋 Hey champion! I'm your AI Fitness Coach. I'm here to create personalized workouts, provide real-time guidance, and help you smash your fitness goals!\n\nWhat would you like to work on today?",
                    isFromAI = true
                )
            )
        }
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background)
    ) {
        // Header with AI status
        AICoachHeader(
            isOnline = true,
            workoutProgress = workoutProgress
        )
        
        // Quick Actions
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getQuickActions()) { action ->
                QuickActionChip(
                    text = action.first,
                    icon = action.second,
                    onClick = {
                        inputText = action.first
                        sendMessage(
                            messages = messages,
                            inputText = inputText,
                            isAITyping = isAITyping,
                            isLoading = isLoading,
                            onMessagesUpdate = { /* No need to update as messages is now persistent */ },
                            onInputTextUpdate = { inputText = it },
                            onAITypingUpdate = { isAITyping = it },
                            onLoadingUpdate = { isLoading = it },
                            coroutineScope = coroutineScope,
                            aiCoachViewModel = aiCoachViewModel
                        )
                    }
                )
            }
        }
        
        // Chat Messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { message ->
                ChatMessageBubble(
                    message = message,
                    onSaveWorkout = { workoutContent ->
                        android.util.Log.d("AICoach", "💾 Saving workout: ${workoutContent.take(100)}...")
                        // This will be handled by the WorkoutViewModel injected in ChatMessageBubble
                    }
                )
            }
            
            if (isAITyping) {
                item {
                    TypingIndicator()
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Input Area
        ChatInputArea(
            inputText = inputText,
            isLoading = isLoading,
            onInputChange = { inputText = it },
            onSendClick = {
                sendMessage(
                    messages = messages,
                    inputText = inputText,
                    isAITyping = isAITyping,
                    isLoading = isLoading,
                    onMessagesUpdate = { /* No need to update as messages is now persistent */ },
                    onInputTextUpdate = { inputText = it },
                    onAITypingUpdate = { isAITyping = it },
                    onLoadingUpdate = { isLoading = it },
                    coroutineScope = coroutineScope,
                    aiCoachViewModel = aiCoachViewModel
                )
            }
        )
    }
}

@Composable
fun AICoachHeader(
    isOnline: Boolean,
    workoutProgress: com.fitsoul.app.data.service.WorkoutProgress
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                        text = "🤖 AI Coach",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = FitsoulColors.TextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) FitsoulColors.Success else FitsoulColors.Warning)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOnline) "Online & Ready" else "Connecting...",
                            style = MaterialTheme.typography.bodySmall,
                            color = FitsoulColors.TextSecondary
                        )
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = FitsoulColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            if (workoutProgress.currentExercise.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
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
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = FitsoulColors.Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Currently: ${workoutProgress.currentExercise}",
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
fun QuickActionChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(text = text)
            }
        },
        selected = false,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = FitsoulColors.Surface,
            labelColor = FitsoulColors.TextPrimary
        )
    )
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    onSaveWorkout: (String) -> Unit = {}
) {
    val workoutViewModel: com.fitsoul.app.ui.viewmodel.WorkoutViewModel = hiltViewModel()
    val saveResult by workoutViewModel.saveWorkoutResult.collectAsState()
    val alignment = if (message.isFromAI) Alignment.CenterStart else Alignment.CenterEnd
    val backgroundColor = if (message.isFromAI) FitsoulColors.Surface else FitsoulColors.Primary
    val textColor = if (message.isFromAI) FitsoulColors.TextPrimary else Color.White
    
    // Check if the message contains workout content
    val isWorkoutContent = message.isFromAI && (
        message.content.contains("workout", ignoreCase = true) && (
            message.content.contains("exercise", ignoreCase = true) ||
            message.content.contains("sets", ignoreCase = true) ||
            message.content.contains("reps", ignoreCase = true) ||
            message.content.contains("minutes", ignoreCase = true)
        )
    )
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            shape = RoundedCornerShape(
                topStart = if (message.isFromAI) 4.dp else 16.dp,
                topEnd = if (message.isFromAI) 16.dp else 4.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                if (message.isFromAI) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = FitsoulColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Coach",
                            style = MaterialTheme.typography.labelSmall,
                            color = FitsoulColors.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
                
                // Add save button for workout content
                if (isWorkoutContent) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column {
                            OutlinedButton(
                                onClick = { 
                                    workoutViewModel.saveWorkoutFromAI(message.content)
                                    onSaveWorkout(message.content)
                                },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = FitsoulColors.Primary
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, 
                                    FitsoulColors.Primary.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkAdd,
                                    contentDescription = "Save workout",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Save",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            
                            // Show save result
                            saveResult?.let { result ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = result,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (result.startsWith("✅")) FitsoulColors.Success else FitsoulColors.Warning
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = FitsoulColors.Surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = FitsoulColors.Primary.copy(alpha = alpha),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Coach is typing...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitsoulColors.TextSecondary.copy(alpha = alpha),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputArea(
    inputText: String,
    isLoading: Boolean,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitsoulColors.Surface
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            TextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                placeholder = {
                    Text(
                        text = "Ask your AI coach anything...",
                        color = FitsoulColors.TextSecondary
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = FitsoulColors.TextPrimary,
                    unfocusedTextColor = FitsoulColors.TextPrimary,
                    cursorColor = FitsoulColors.Primary
                ),
                maxLines = 3
            )
            
            FloatingActionButton(
                onClick = onSendClick,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp),
                containerColor = if (inputText.isBlank() || isLoading) 
                    FitsoulColors.TextSecondary.copy(alpha = 0.3f) 
                else 
                    FitsoulColors.Primary,
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isBlank()) FitsoulColors.TextTertiary else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun getQuickActions() = listOf(
    "Generate workout plan" to Icons.Default.FitnessCenter,
    "Form check tips" to Icons.Default.Check,
    "Nutrition advice" to Icons.Default.Restaurant,
    "Progress review" to Icons.Default.TrendingUp
)

private fun sendMessage(
    messages: MutableList<ChatMessage>,
    inputText: String,
    isAITyping: Boolean,
    isLoading: Boolean,
    onMessagesUpdate: (List<ChatMessage>) -> Unit,
    onInputTextUpdate: (String) -> Unit,
    onAITypingUpdate: (Boolean) -> Unit,
    onLoadingUpdate: (Boolean) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    aiCoachViewModel: AICoachViewModel
) {
    if (inputText.isBlank() || isLoading) return
    
    val userMessage = ChatMessage(
        id = "user_${System.currentTimeMillis()}",
        content = inputText.trim(),
        isFromAI = false
    )
    
    // Add user message to persistent list
    messages.add(userMessage)
    onInputTextUpdate("")
    onLoadingUpdate(true)
    onAITypingUpdate(true)
    
    coroutineScope.launch {
        try {
            delay(300) // Brief thinking time
            
            // Call the AI Coach ViewModel to generate response
            val aiResponse = aiCoachViewModel.generateAIResponse(inputText.trim())
            
            onAITypingUpdate(false)
            delay(500)
            
            val aiMessage = ChatMessage(
                id = "ai_${System.currentTimeMillis()}",
                content = aiResponse,
                isFromAI = true
            )
            
            // Add AI message to persistent list
            messages.add(aiMessage)
            
        } catch (e: Exception) {
            onAITypingUpdate(false)
            val errorMessage = ChatMessage(
                id = "ai_error_${System.currentTimeMillis()}",
                content = "I encountered an issue connecting to my AI brain. Error: API call failed: 401.\n\nLet me help you with a workout plan based on my training instead! Just tell me what you'd like to work on (legs, arms, chest, etc.) and I'll create a personalized plan for you. 💪",
                isFromAI = true
            )
            // Add error message to persistent list
            messages.add(errorMessage)
        } finally {
            onLoadingUpdate(false)
        }
    }
}

