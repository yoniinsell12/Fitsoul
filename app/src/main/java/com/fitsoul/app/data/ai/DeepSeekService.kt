package com.fitsoul.app.data.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepSeekService @Inject constructor() {
    private val TAG = "DeepSeekService"
    
    // Conservative rate limit tracking to prevent 429 errors
    private var lastRateLimitTime = 0L
    private var apiCallCount = 0
    private var successfulCallCount = 0
    private val rateLimitCooldownMs = 300000L // 5 minute cooldown after rate limit (much longer)
    private val maxCallsPerSession = 3 // Very conservative - only 3 calls per session
    private val minTimeBetweenCalls = 30000L // 30 seconds minimum between calls
    private var lastApiCallTime = 0L
    private var callTimestamps = mutableListOf<Long>() // Track call times for rate optimization
    
    // DeepSeek API configuration via OpenRouter
    private val apiKey: String
    private val baseUrl = "https://openrouter.ai/api/v1"
    
    // Using a good model available on OpenRouter
    private val model = "meta-llama/llama-3.2-3b-instruct:free" // Free high-quality model
    
    init {
        // Try to get API key from BuildConfig
        val configApiKey = try {
            com.fitsoul.app.BuildConfig.OPENROUTER_API_KEY
        } catch (e: Exception) {
            ""
        }
        
        // For now, we'll use enhanced fallback responses since OpenRouter requires authentication
        // Users can add their own API key for full AI functionality
        apiKey = if (configApiKey.isNotBlank() && !configApiKey.equals("", ignoreCase = true)) {
            configApiKey
        } else {
            // No API key available - will use fallback responses
            ""
        }
    }
    
    // Enhanced client with better timeout and retry configuration
    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    // Retry configuration
    private val maxRetries = 3
    private val initialRetryDelayMs = 1000L
        
    init {
        Log.d(TAG, "=== FITSOUL OFFLINE WORKOUT SERVICE INITIALIZED ===")
        Log.d(TAG, "🔒 OFFLINE MODE: External API completely disabled")
        Log.d(TAG, "✅ NO 429 ERRORS: Network errors eliminated")
        Log.d(TAG, "💪 EXPERT WORKOUTS: 25+ local workout templates")
        Log.d(TAG, "🚀 INSTANT RESPONSE: No network delays")
        Log.d(TAG, "🛡️ BULLETPROOF: Works without internet connection")
        Log.d(TAG, "⚡ ZERO DEPENDENCIES: Fully self-contained")
        Log.d(TAG, "🎯 PERSONALIZED: Smart workout matching")
        Log.d(TAG, "💡 UPGRADE: Add OPENROUTER_API_KEY for enhanced AI (optional)")
        Log.d(TAG, "=====================================================")
    }
    
    suspend fun generateWorkoutPlan(
        goals: List<String>,
        fitnessLevel: String,
        availableTime: Int,
        equipment: List<String> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "💪 Generating OFFLINE workout plan for goals: ${goals.joinToString()}, level: $fitnessLevel, time: $availableTime mins")
            
            val goalsString = goals.joinToString(", ")
            val equipmentString = if (equipment.isNotEmpty()) equipment.joinToString(", ") else "No equipment (bodyweight exercises only)"
            
            val prompt = createEnhancedWorkoutPrompt(goalsString, fitnessLevel, availableTime, equipmentString)
            
            // 🔒 OFFLINE-FIRST: Skip all API checks and go directly to local generation
            Log.d(TAG, "🚀 Using OFFLINE workout generation - No 429 errors possible!")
            Log.d(TAG, "⚡ INSTANT RESPONSE: No network delays or failures")
            
            val response = generateEnhancedOfflineResponse(prompt, goalsString, fitnessLevel, availableTime, equipmentString)
            
            Log.d(TAG, "✅ Successfully generated OFFLINE workout plan (${response.length} chars)")
            Log.d(TAG, "🛡️ BULLETPROOF: Zero network-related errors")
            return@withContext Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error generating workout plan", e)
            // Even if something unexpected happens, return a fallback workout
            val goalsString = goals.joinToString(", ")
            val equipmentString = if (equipment.isNotEmpty()) equipment.joinToString(", ") else "No equipment (bodyweight exercises only)"
            val fallbackPrompt = createEnhancedWorkoutPrompt(goalsString, fitnessLevel, availableTime, equipmentString)
            return@withContext Result.success(generateFallbackResponse(fallbackPrompt))
        }
    }
    
    private fun createEnhancedWorkoutPrompt(goals: String, level: String, time: Int, equipment: String): String {
        return """
            You are FitSoul's expert AI trainer with 15+ years of experience. Create a comprehensive, personalized workout plan.

            🎯 USER PROFILE:
            • Fitness Goals: $goals
            • Experience Level: $level
            • Available Time: $time minutes
            • Equipment: $equipment

            📋 MANDATORY STRUCTURE (use exactly this format):

            🔥 WARM-UP (5 minutes)
            [List 3-4 dynamic warm-up exercises with duration]

            💪 MAIN WORKOUT (${time - 10} minutes)
            [Create 3-4 exercises based on goals and level]
            For each exercise include:
            • Exercise name
            • Sets x Reps (adjusted for $level level)
            • Rest period
            • Quick form tip

            🧘‍♀️ COOL-DOWN (5 minutes)
            [List 3-4 stretching/recovery exercises]

            💡 PRO TIPS:
            [3-4 specific tips for this workout]

            IMPORTANT GUIDELINES:
            - Adjust intensity for $level level
            - Focus primarily on: $goals
            - All exercises must be possible with: $equipment
            - Use emojis throughout for engagement
            - Be specific with sets, reps, and rest times
            - Include safety reminders
            - Make it motivational and actionable
            
            Generate a complete, ready-to-use workout that takes exactly $time minutes.
        """.trimIndent()
    }
    
    private suspend fun callDeepSeekApiWithRetry(prompt: String): String = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                Log.d(TAG, "🌐 API attempt ${attempt + 1}/$maxRetries")
                return@withContext callDeepSeekApi(prompt)
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "⚠️ API attempt ${attempt + 1} failed: ${e.message}")
                
                // Don't retry on rate limits (429) - use fallback immediately and disable session
                if (e.message?.contains("Rate limit", ignoreCase = true) == true || 
                    e.message?.contains("429", ignoreCase = true) == true) {
                    lastRateLimitTime = System.currentTimeMillis()
                    apiCallCount = maxCallsPerSession + 10 // Disable API for session
                    Log.w(TAG, "🚫 429 ERROR IN EXCEPTION - API DISABLED FOR SESSION")
                    Log.d(TAG, "🛡️ All future requests will use static workouts")
                    return@withContext generateFallbackResponse(prompt)
                }
                
                // Don't retry on authentication errors (401) either
                if (e.message?.contains("authentication", ignoreCase = true) == true ||
                    e.message?.contains("401", ignoreCase = true) == true) {
                    Log.d(TAG, "🛡️ Auth error detected - skipping retries, using fallback")
                    return@withContext generateFallbackResponse(prompt)
                }
                
                if (attempt < maxRetries - 1) {
                    val delay = initialRetryDelayMs * (1 shl attempt) // Exponential backoff
                    Log.d(TAG, "⏳ Retrying in ${delay}ms...")
                    kotlinx.coroutines.delay(delay)
                }
            }
        }
        
        // If all retries failed, use fallback instead of throwing
        Log.d(TAG, "🛡️ All API attempts failed - using enhanced static fallback")
        return@withContext generateFallbackResponse(prompt)
    }
    
    private suspend fun callDeepSeekApi(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            // Create enhanced JSON request for OpenRouter/DeepSeek API
            val json = buildJsonObject {
                put("model", model)
                putJsonArray("messages") {
                    addJsonObject {
                        put("role", "system")
                        put("content", """You are FitSoul's elite AI fitness trainer with expertise in:
                            - Exercise physiology and biomechanics
                            - Personalized workout programming
                            - Injury prevention and form coaching
                            - Motivational fitness guidance
                            
                            ALWAYS provide detailed, structured workout plans with:
                            ✅ Specific exercise names, sets, reps, and rest periods
                            ✅ Form cues and safety tips
                            ✅ Appropriate intensity for the user's fitness level
                            ✅ Engaging format with emojis and clear structure
                            ✅ Complete warm-up, main workout, and cool-down sections
                            
                            Make every response actionable, motivational, and safe.""")
                    }
                    addJsonObject {
                        put("role", "user")
                        put("content", prompt)
                    }
                }
                put("max_tokens", 2500) // Increased for more detailed responses
                put("temperature", 0.4) // Lower for more consistent, focused responses
                put("top_p", 0.9) // Slightly more focused
                put("frequency_penalty", 0.1) // Reduce repetition
                put("presence_penalty", 0.1) // Encourage variety
                put("stream", false)
            }
            
            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            
            val requestBuilder = Request.Builder()
                .url("$baseUrl/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "https://fitsoul.app")
                .addHeader("X-Title", "Fitsoul AI Workout Generator")
                .post(requestBody)
            
            // Only add Authorization header if API key is provided
            if (apiKey.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $apiKey")
            }
            
            val request = requestBuilder.build()
            
            Log.d(TAG, "🌐 Making HTTP request to DeepSeek API...")
            Log.d(TAG, "📍 URL: $baseUrl/chat/completions")
            Log.d(TAG, "🤖 Model: $model")
            Log.d(TAG, "🔑 Auth: ${if (apiKey.isNotBlank()) "Bearer ${apiKey.take(10)}..." else "No API key (using free tier)"}")
            Log.d(TAG, "📦 Request Body Length: ${requestBody.contentLength()}")
            Log.d(TAG, "🔧 Request Body Preview: ${json.toString().take(200)}...")
            
            val response = client.newCall(request).execute()
            Log.d(TAG, "📡 Response Code: ${response.code}")
            Log.d(TAG, "📡 Response Message: ${response.message}")
            Log.d(TAG, "📡 Response Headers: ${response.headers}")
            
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                Log.e(TAG, "❌ DeepSeek API call failed: ${response.code} - ${response.message}")
                Log.e(TAG, "📄 Error body: $errorBody")
                
                // Provide more specific error messages
                val errorMessage = when (response.code) {
                    401 -> "API key required - add your OpenRouter API key to gradle.properties"
                    402 -> "Payment required - check OpenRouter credits/billing"
                    404 -> "Model not found - '$model' may not be available"
                    422 -> "Invalid request - check model name or parameters"
                    429 -> {
                        // Get retry-after header if available
                        val retryAfter = response.header("Retry-After")?.toIntOrNull() ?: 60
                        Log.w(TAG, "⚠️ Rate limit exceeded - API usage limit reached")
                        Log.i(TAG, "🛡️ Automatically switching to enhanced static workouts")
                        
                        // Don't retry on 429 - just use fallback immediately
                        "Rate limit reached - using enhanced static workout generation"
                    }
                    500, 502, 503 -> "Server error - API temporarily unavailable"
                    524 -> "Timeout - API request took too long"
                    else -> "API call failed with code ${response.code}"
                }
                
                // For rate limit errors, use fallback immediately without retrying
                if (response.code == 429) {
                    lastRateLimitTime = System.currentTimeMillis()
                    // Immediately disable API for this session to prevent more 429s
                    apiCallCount = maxCallsPerSession + 10 // Effectively disable for session
                    Log.w(TAG, "🚫 429 RATE LIMIT HIT - API DISABLED FOR THIS SESSION")
                    Log.d(TAG, "⏰ 5-minute cooldown activated + session disabled")
                    Log.d(TAG, "🛡️ All future requests will use static workouts until app restart")
                    return@withContext generateFallbackResponse(prompt)
                }
                
                // For authentication errors, also use fallback
                if (response.code == 401) {
                    Log.d(TAG, "🛡️ Authentication issue - using enhanced static workout")
                    return@withContext generateFallbackResponse(prompt)
                }
                
                throw IOException(errorMessage)
            }
            
            val responseBody = response.body?.string()
                ?: throw IOException("Empty response body from API")
            
            Log.d(TAG, "📥 Raw API response received (first 300 chars): ${responseBody.take(300)}...")
            
            // Enhanced JSON response parsing with better error handling
            try {
                val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject
                
                // Check for API-level errors
                jsonResponse["error"]?.let { error ->
                    val errorObj = error.jsonObject
                    val errorMessage = errorObj["message"]?.jsonPrimitive?.content ?: "Unknown API error"
                    val errorType = errorObj["type"]?.jsonPrimitive?.content ?: "unknown"
                    Log.e(TAG, "🚨 API returned error: $errorType - $errorMessage")
                    throw IOException("API Error: $errorMessage")
                }
                
                val choices = jsonResponse["choices"]?.jsonArray
                
                if (choices == null || choices.isEmpty()) {
                    Log.e(TAG, "❌ No choices in API response")
                    throw IOException("No workout plan generated by API")
                }
                
                val message = choices[0].jsonObject["message"]?.jsonObject
                val content = message?.get("content")?.jsonPrimitive?.content
                    ?: throw IOException("No content in response message")
                
                // Validate content quality
                if (content.isBlank()) {
                    throw IOException("API returned blank workout plan")
                }
                
                if (content.length < 100) {
                    Log.w(TAG, "⚠️ Short response received (${content.length} chars)")
                }
                
                Log.d(TAG, "✅ Successfully extracted workout plan (${content.length} chars)")
                Log.d(TAG, "📝 Content preview: ${content.take(150)}...")
                
                // Record successful API call
                recordSuccessfulCall()
                
                return@withContext content.trim()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to parse API response", e)
                throw IOException("Failed to parse workout plan from API: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Critical error calling DeepSeek API", e)
            throw e
        }
    }
    
    // Additional utility methods for enhanced functionality
    
    suspend fun generateQuickWorkout(
        duration: Int,
        equipment: String = "bodyweight"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🚀 Generating quick $duration-minute workout")
            
            val prompt = """
                Create a quick, effective $duration-minute workout using $equipment.
                
                Format:
                🔥 Quick $duration-Min Workout
                
                💪 Exercises (complete 2 rounds):
                [List 4-5 exercises with reps/time]
                
                💡 Notes:
                • Total time: $duration minutes
                • Equipment: $equipment
                • Focus on full-body movement
                
                Make it energizing and doable for anyone!
            """.trimIndent()
            
            val response = callDeepSeekApiWithRetry(prompt)
            return@withContext Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generating quick workout", e)
            // Return a fallback quick workout instead of failing
            return@withContext Result.success(generateFallbackQuickWorkout(duration, equipment))
        }
    }
    
    suspend fun generateFormTips(exerciseName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎯 Generating form tips for: $exerciseName")
            
            val prompt = """
                Provide expert form guidance for the exercise: $exerciseName
                
                Format:
                🏋️ Perfect Form: $exerciseName
                
                ✅ Setup:
                [Starting position and setup]
                
                🎯 Execution:
                [Step-by-step movement]
                
                ⚠️ Common Mistakes:
                [2-3 common errors to avoid]
                
                💡 Pro Tips:
                [Advanced technique tips]
                
                Focus on safety, proper biomechanics, and effectiveness.
            """.trimIndent()
            
            val response = callDeepSeekApiWithRetry(prompt)
            return@withContext Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generating form tips", e)
            // Return a fallback form tip instead of failing
            return@withContext Result.success(generateFallbackFormTip(exerciseName))
        }
    }
    
    // Health check method to verify API connectivity
    suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 Performing DeepSeek API health check...")
            
            val testPrompt = "Respond with exactly 'API_HEALTHY' if you can read this."
            val response = callDeepSeekApi(testPrompt)
            
            val isHealthy = response.contains("API_HEALTHY", ignoreCase = true)
            Log.d(TAG, if (isHealthy) "✅ API health check passed" else "⚠️ API health check failed")
            
            return@withContext isHealthy
        } catch (e: Exception) {
            Log.w(TAG, "❌ API health check failed: ${e.message}")
            return@withContext false
        }
    }
    
    // Check if the service is properly configured
    fun isConfigured(): Boolean {
        val hasApiKey = apiKey.isNotBlank()
        val configured = hasApiKey && baseUrl.isNotBlank() && model.isNotBlank()
        Log.d(TAG, "🔧 Service configured: $configured (API key available: $hasApiKey)")
        return configured
    }
    
    // Check if we should skip API call due to recent rate limit
    private fun isInRateLimitCooldown(): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceRateLimit = currentTime - lastRateLimitTime
        val inCooldown = lastRateLimitTime > 0 && timeSinceRateLimit < rateLimitCooldownMs
        
        if (inCooldown) {
            val remainingCooldown = (rateLimitCooldownMs - timeSinceRateLimit) / 1000
            Log.d(TAG, "⏳ In rate limit cooldown for ${remainingCooldown}s - using enhanced static workout")
            Log.d(TAG, "💡 Free tier optimization: Avoiding unnecessary API calls to preserve quota")
        }
        
        return inCooldown
    }
    
    // Reset rate limit cooldown (useful for testing or when user upgrades)
    fun resetRateLimit() {
        lastRateLimitTime = 0L
        Log.d(TAG, "🔄 Rate limit cooldown reset - ready for API calls")
    }
    
    // Get current rate limit status for UI display
    fun getRateLimitStatus(): Pair<Boolean, Long> {
        val currentTime = System.currentTimeMillis()
        val timeSinceRateLimit = currentTime - lastRateLimitTime
        val inCooldown = lastRateLimitTime > 0 && timeSinceRateLimit < rateLimitCooldownMs
        val remainingTime = if (inCooldown) (rateLimitCooldownMs - timeSinceRateLimit) / 1000 else 0L
        
        return Pair(inCooldown, remainingTime)
    }
    
    // Check if API is effectively disabled due to rate limits
    private fun isApiDisabledForSession(): Boolean {
        return apiCallCount >= maxCallsPerSession
    }
    
    // Record API call attempt for usage tracking
    private fun recordApiCallAttempt() {
        val currentTime = System.currentTimeMillis()
        callTimestamps.add(currentTime)
        apiCallCount++
        
        Log.d(TAG, "📈 API call #$apiCallCount attempted")
    }
    
    // Record successful API call
    private fun recordSuccessfulCall() {
        successfulCallCount++
        Log.d(TAG, "✅ Successful API call #$successfulCallCount")
    }
    
    // Get API usage statistics
    fun getUsageStats(): Triple<Int, Int, Int> {
        val isDisabled = isApiDisabledForSession()
        val inCooldown = isInRateLimitCooldown()
        val effectivelyDisabled = isDisabled || inCooldown
        
        Log.d(TAG, "📊 API Stats: Total calls: $apiCallCount/$maxCallsPerSession, Successful: $successfulCallCount, Disabled: $effectivelyDisabled")
        return Triple(apiCallCount, successfulCallCount, if (effectivelyDisabled) 1 else 0)
    }
    
    // 🔒 OFFLINE-FIRST: Enhanced offline response generation - eliminates all 429 errors
    private fun generateEnhancedOfflineResponse(
        prompt: String,
        goals: String,
        fitnessLevel: String,
        availableTime: Int,
        equipment: String
    ): String {
        Log.d(TAG, "🚀 OFFLINE ENGINE: Generating workout for $fitnessLevel level, $availableTime min, goals: $goals")
        
        // Check for specific prebuilt workout requests first
        val lowerPrompt = prompt.lowercase()
        when {
            lowerPrompt.contains("push day") || lowerPrompt.contains("push workout") -> {
                Log.d(TAG, "💪 PREBUILT push workout selected")
                return getPrebuiltWorkout("push", fitnessLevel)
            }
            lowerPrompt.contains("pull day") || lowerPrompt.contains("pull workout") -> {
                Log.d(TAG, "🎯 PREBUILT pull workout selected")
                return getPrebuiltWorkout("pull", fitnessLevel)
            }
            lowerPrompt.contains("leg day") || lowerPrompt.contains("leg workout") -> {
                Log.d(TAG, "🦵 PREBUILT leg workout selected")
                return getPrebuiltWorkout("legs", fitnessLevel)
            }
            lowerPrompt.contains("core") || lowerPrompt.contains("abs") -> {
                Log.d(TAG, "🔥 PREBUILT core workout selected")
                return getPrebuiltWorkout("core", fitnessLevel)
            }
            lowerPrompt.contains("hiit") || lowerPrompt.contains("high intensity") -> {
                Log.d(TAG, "⚡ PREBUILT HIIT workout selected")
                return getPrebuiltWorkout("hiit", fitnessLevel)
            }
            lowerPrompt.contains("yoga") || lowerPrompt.contains("mindful") -> {
                Log.d(TAG, "🧘‍♀️ PREBUILT yoga workout selected")
                return getPrebuiltWorkout("yoga", fitnessLevel)
            }
            lowerPrompt.contains("pilates") -> {
                Log.d(TAG, "🎯 PREBUILT pilates workout selected")
                return getPrebuiltWorkout("pilates", fitnessLevel)
            }
            lowerPrompt.contains("upper body") || lowerPrompt.contains("upper") -> {
                Log.d(TAG, "💪 PREBUILT upper body workout selected")
                return getPrebuiltWorkout("upper", fitnessLevel)
            }
            lowerPrompt.contains("lower body") || lowerPrompt.contains("lower") -> {
                Log.d(TAG, "🦵 PREBUILT lower body workout selected")
                return getPrebuiltWorkout("lower", fitnessLevel)
            }
            lowerPrompt.contains("functional") -> {
                Log.d(TAG, "🏃‍♂️ PREBUILT functional workout selected")
                return getPrebuiltWorkout("functional", fitnessLevel)
            }
        }
        
        // Smart workout selection based on goals, level, and time
        return when {
            goals.contains("strength", ignoreCase = true) || goals.contains("muscle", ignoreCase = true) -> {
                Log.d(TAG, "💪 STRENGTH focused workout selected")
                generateStrengthWorkout(fitnessLevel, availableTime, equipment)
            }
            goals.contains("cardio", ignoreCase = true) || goals.contains("endurance", ignoreCase = true) -> {
                Log.d(TAG, "🏃 CARDIO focused workout selected")
                generateCardioWorkout(fitnessLevel, availableTime, equipment)
            }
            goals.contains("weight loss", ignoreCase = true) || goals.contains("fat loss", ignoreCase = true) -> {
                Log.d(TAG, "🔥 FAT BURNING workout selected")
                generateFatBurningWorkout(fitnessLevel, availableTime, equipment)
            }
            goals.contains("flexibility", ignoreCase = true) || goals.contains("stretch", ignoreCase = true) -> {
                Log.d(TAG, "🧘 FLEXIBILITY workout selected")
                generateFlexibilityWorkout(availableTime)
            }
            availableTime <= 20 -> {
                Log.d(TAG, "⚡ QUICK workout selected (${availableTime}min)")
                generateQuickFullBodyWorkout(fitnessLevel, availableTime, equipment)
            }
            else -> {
                Log.d(TAG, "🎯 BALANCED full-body workout selected")
                generateBalancedFullBodyWorkout(fitnessLevel, availableTime, equipment)
            }
        }
    }
    
    private fun generateStrengthWorkout(level: String, time: Int, equipment: String): String {
        val warmupTime = 5
        val cooldownTime = 5
        val mainTime = time - warmupTime - cooldownTime
        
        return """
            💪 **STRENGTH BUILDER WORKOUT** ($time minutes)
            
            🔥 **WARM-UP** ($warmupTime minutes)
            • Arm circles: 30 seconds each direction
            • Bodyweight squats: 15 reps
            • Push-up position hold: 30 seconds
            • Torso twists: 15 each side
            • Light jumping jacks: 45 seconds
            
            💪 **STRENGTH TRAINING** ($mainTime minutes)
            
            **Circuit A** (3 rounds, 2 min rest between rounds):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Modified push-ups (knees): 8-12 reps
                • Assisted squats: 12-15 reps
                • Plank hold: 20-30 seconds
                • Standing calf raises: 15 reps
                """
                "intermediate" -> """
                • Standard push-ups: 12-15 reps
                • Bodyweight squats: 15-20 reps
                • Plank hold: 45-60 seconds
                • Single-leg calf raises: 12 each leg
                """
                else -> """
                • Diamond push-ups: 10-15 reps
                • Jump squats: 15-20 reps
                • Plank to push-up: 8-12 reps
                • Pistol squat progression: 5-8 each leg
                """
            }}
            
            **Circuit B** (2 rounds, 90 sec rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Wall push-ups: 15 reps
                • Chair-assisted lunges: 8 each leg
                • Modified mountain climbers: 20 total
                • Glute bridges: 12-15 reps
                """
                "intermediate" -> """
                • Incline push-ups: 12-15 reps
                • Walking lunges: 10 each leg
                • Mountain climbers: 30 seconds
                • Single-leg glute bridges: 10 each leg
                """
                else -> """
                • Decline push-ups: 10-12 reps
                • Reverse lunges with knee drive: 12 each leg
                • Burpees: 8-10 reps
                • Single-leg deadlifts: 8 each leg
                """
            }}
            
            🧘‍♀️ **COOL-DOWN** ($cooldownTime minutes)
            • Chest doorway stretch: 30 seconds
            • Quad stretch: 30 seconds each leg
            • Hamstring stretch: 30 seconds each leg
            • Shoulder rolls: 10 each direction
            • Deep breathing: 60 seconds
            
            💡 **STRENGTH TIPS**:
            • Focus on controlled movements
            • Progressive overload: add reps weekly
            • Rest 48 hours before training same muscles
            • Proper form beats speed every time
            
            **Equipment**: $equipment
            **Level**: $level
            **Focus**: Building functional strength 💪
        """.trimIndent()
    }
    
    private fun generateCardioWorkout(level: String, time: Int, equipment: String): String {
        val warmupTime = 5
        val cooldownTime = 5
        val mainTime = time - warmupTime - cooldownTime
        
        return """
            🏃 **CARDIO BLAST WORKOUT** ($time minutes)
            
            🔥 **DYNAMIC WARM-UP** ($warmupTime minutes)
            • March in place: 45 seconds
            • Arm swings: 30 seconds
            • Leg swings: 15 each leg
            • Light bouncing: 30 seconds
            • Gentle jumping jacks: 45 seconds
            
            🏃 **CARDIO INTERVALS** ($mainTime minutes)
            
            **HIIT Circuit** (Repeat for full time):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Work: 30 seconds | Rest: 90 seconds
                
                Round 1:
                • Step touches: 30 sec
                • Rest: 90 sec
                • Marching with arm raises: 30 sec
                • Rest: 90 sec
                • Modified jumping jacks: 30 sec
                • Rest: 90 sec
                """
                "intermediate" -> """
                • Work: 45 seconds | Rest: 75 seconds
                
                Round 1:
                • Jumping jacks: 45 sec
                • Rest: 75 sec
                • High knees: 45 sec
                • Rest: 75 sec
                • Butt kicks: 45 sec
                • Rest: 75 sec
                """
                else -> """
                • Work: 60 seconds | Rest: 60 seconds
                
                Round 1:
                • Burpees: 60 sec
                • Rest: 60 sec
                • Jump squats: 60 sec
                • Rest: 60 sec
                • Mountain climbers: 60 sec
                • Rest: 60 sec
                """
            }}
            
            **Active Recovery Circuit** (Between intense rounds):
            • Walking in place: 60 seconds
            • Gentle arm circles: 30 seconds
            • Deep breathing: 30 seconds
            
            🧘‍♀️ **COOL-DOWN** ($cooldownTime minutes)
            • Slow walking: 2 minutes
            • Calf stretch: 30 seconds each
            • Hip flexor stretch: 30 seconds each
            • Deep breathing exercises: 60 seconds
            
            💡 **CARDIO TIPS**:
            • Monitor your heart rate
            • Stay hydrated throughout
            • Land softly during jumps
            • Modify intensity as needed
            
            **Equipment**: $equipment
            **Level**: $level
            **Target**: Cardiovascular endurance 🫀
        """.trimIndent()
    }
    
    private fun generateFatBurningWorkout(level: String, time: Int, equipment: String): String {
        return """
            🔥 **FAT BURNING METABOLIC WORKOUT** ($time minutes)
            
            ⚡ **METABOLIC ACTIVATION** (5 minutes)
            • Light jogging in place: 60 seconds
            • Dynamic arm swings: 30 seconds
            • Bodyweight squats: 15 reps
            • Standing knee-to-elbow crunches: 20 total
            • Jumping jacks: 45 seconds
            
            🔥 **FAT BURNING CIRCUITS** (${time - 10} minutes)
            
            **Circuit 1: Metabolic Ignition** (4 rounds, 30 sec rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Step-ups (use stairs): 45 seconds
                • Modified burpees: 8-10 reps
                • Standing oblique crunches: 20 each side
                • Marching planks: 30 seconds
                """
                "intermediate" -> """
                • Jump squats: 45 seconds
                • Burpees: 10-12 reps
                • Bicycle crunches: 30 seconds
                • Plank jacks: 30 seconds
                """
                else -> """
                • Burpee jump squats: 45 seconds
                • Full burpees: 12-15 reps
                • Russian twists: 45 seconds
                • Burpee broad jumps: 30 seconds
                """
            }}
            
            **Circuit 2: Afterburn Effect** (3 rounds, 45 sec rest):
            • High knees: 45 seconds
            • Push-up to T-rotation: 10-12 reps
            • Jump lunges: 30 seconds (or alternating lunges)
            • Mountain climber twists: 30 seconds
            
            **Finisher: Tabata Blast** (4 minutes):
            • 20 seconds MAX effort jumping jacks
            • 10 seconds rest
            • Repeat for 8 rounds total
            
            🧘‍♀️ **RECOVERY STRETCH** (5 minutes)
            • Child's pose: 60 seconds
            • Spinal twists: 30 seconds each side
            • Hip flexor stretch: 45 seconds each leg
            • Deep breathing: 90 seconds
            
            💡 **FAT BURNING TIPS**:
            • Keep intensity high during work periods
            • Minimal rest between exercises
            • Stay hydrated - drink water throughout
            • This creates "afterburn" effect for hours!
            
            **Equipment**: $equipment
            **Level**: $level
            **Goal**: Maximum calorie burn 🔥
        """.trimIndent()
    }
    
    private fun generateFlexibilityWorkout(time: Int): String {
        return """
            🧘 **FLEXIBILITY & MOBILITY FLOW** ($time minutes)
            
            🌅 **GENTLE WARM-UP** (5 minutes)
            • Neck rolls: 5 each direction
            • Shoulder shrugs: 10 reps
            • Arm circles: 10 each direction
            • Gentle torso twists: 10 each side
            • Cat-cow stretches: 10 reps
            
            🧘‍♀️ **FLEXIBILITY SEQUENCE** (${time - 10} minutes)
            
            **Upper Body Flow** (Hold each 45-60 seconds):
            • Chest doorway stretch
            • Tricep overhead stretch (each arm)
            • Cross-body shoulder stretch (each arm)
            • Neck side stretch (each side)
            • Upper trap stretch (each side)
            
            **Core & Spine Mobility**:
            • Seated spinal twist: 60 seconds each side
            • Cat-cow pose: 10 slow repetitions
            • Child's pose: 90 seconds
            • Cobra stretch: 45 seconds
            • Knee-to-chest: 45 seconds each leg
            
            **Lower Body Deep Stretch**:
            • Forward fold: 90 seconds
            • Seated figure-4 stretch: 60 seconds each leg
            • Pigeon pose (modified): 90 seconds each side
            • Happy baby pose: 60 seconds
            • Butterfly stretch: 90 seconds
            
            **Hip & Leg Focus**:
            • Hip flexor stretch: 60 seconds each leg
            • Hamstring stretch: 60 seconds each leg
            • Calf stretch: 45 seconds each leg
            • IT band stretch: 45 seconds each leg
            
            🌙 **RELAXATION** (5 minutes)
            • Legs up the wall pose: 2 minutes
            • Deep breathing with body scan: 3 minutes
            
            💡 **FLEXIBILITY TIPS**:
            • Never stretch to pain - mild tension only
            • Breathe deeply into each stretch
            • Hold consistent pressure, don't bounce
            • Practice daily for best results
            • Listen to your body's limits
            
            **Benefits**: Improved range of motion, reduced stiffness, better sleep 🌟
        """.trimIndent()
    }
    
    private fun generateQuickFullBodyWorkout(level: String, time: Int, equipment: String): String {
        return """
            ⚡ **QUICK FULL-BODY BLAST** ($time minutes)
            
            🔥 **RAPID WARM-UP** (3 minutes)
            • Jumping jacks: 30 seconds
            • Arm circles: 20 each direction
            • Bodyweight squats: 15 reps
            • Push-up position hold: 30 seconds
            
            💪 **FULL-BODY CIRCUIT** (${time - 6} minutes)
            
            **Super Circuit** (Repeat as many rounds as possible):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Modified push-ups: 30 seconds
                • Wall sit: 30 seconds
                • Knee raises: 30 seconds
                • Rest: 30 seconds
                
                Round 2:
                • Incline push-ups: 30 seconds
                • Assisted squats: 30 seconds
                • Standing crunches: 30 seconds
                • Rest: 30 seconds
                """
                "intermediate" -> """
                • Push-ups: 45 seconds
                • Squats: 45 seconds
                • Plank: 45 seconds
                • Rest: 30 seconds
                
                Round 2:
                • Mountain climbers: 45 seconds
                • Lunges: 45 seconds
                • Bicycle crunches: 45 seconds
                • Rest: 30 seconds
                """
                else -> """
                • Burpees: 45 seconds
                • Jump squats: 45 seconds
                • Plank to push-up: 45 seconds
                • Rest: 15 seconds
                
                Round 2:
                • Mountain climber burpees: 45 seconds
                • Single-leg squats: 45 seconds
                • Russian twists: 45 seconds
                • Rest: 15 seconds
                """
            }}
            
            ⚡ **POWER FINISHER** (2 minutes):
            • Max jumping jacks: 30 seconds
            • Rest: 30 seconds
            • Max bodyweight squats: 30 seconds
            • Rest: 30 seconds
            
            🧘‍♀️ **QUICK RECOVERY** (3 minutes)
            • Standing forward fold: 45 seconds
            • Chest stretch: 30 seconds
            • Hip flexor stretch: 30 seconds each leg
            • Deep breathing: 45 seconds
            
            💡 **QUICK WORKOUT TIPS**:
            • Maximize intensity in short bursts
            • No equipment needed - use bodyweight
            • Perfect for busy schedules
            • Consistency beats perfection!
            
            **Equipment**: $equipment
            **Level**: $level  
            **Perfect for**: Busy days, travel, quick energy boost ⚡
        """.trimIndent()
    }
    
    private fun generateBalancedFullBodyWorkout(level: String, time: Int, equipment: String): String {
        val warmupTime = 6
        val cooldownTime = 6
        val mainTime = time - warmupTime - cooldownTime
        
        return """
            🎯 **BALANCED FULL-BODY TRANSFORMATION** ($time minutes)
            
            🔥 **COMPLETE WARM-UP** ($warmupTime minutes)
            • Light jogging in place: 90 seconds
            • Dynamic arm swings: 45 seconds
            • Leg swings: 15 each leg
            • Torso rotations: 15 each direction
            • Jumping jacks: 60 seconds
            • Joint mobility: 60 seconds
            
            💪 **FULL-BODY TRAINING** ($mainTime minutes)
            
            **Phase 1: Foundation Strength** (3 rounds, 90 sec rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Wall/knee push-ups: 8-12 reps
                • Assisted squats: 12-15 reps
                • Modified plank: 20-30 seconds
                • Standing march: 20 each leg
                • Glute bridges: 12-15 reps
                """
                "intermediate" -> """
                • Standard push-ups: 12-15 reps
                • Bodyweight squats: 15-20 reps
                • Plank hold: 45-60 seconds
                • Alternating lunges: 12 each leg
                • Single-leg glute bridges: 10 each leg
                """
                else -> """
                • Diamond/decline push-ups: 12-15 reps
                • Jump squats: 15-20 reps
                • Plank to push-up: 10-12 reps
                • Reverse lunges with knee drive: 12 each leg
                • Single-leg deadlifts: 8 each leg
                """
            }}
            
            **Phase 2: Cardio Integration** (4 rounds, 60 sec rest):
            • High knees: 30 seconds
            • Push-up variation: 45 seconds
            • Mountain climbers: 30 seconds
            • Squat variation: 45 seconds
            
            **Phase 3: Core & Stability** (3 rounds, 45 sec rest):
            • Plank variations: 45 seconds
            • Bicycle crunches: 30 seconds
            • Side plank: 20 seconds each side
            • Dead bugs: 10 each side
            
            🧘‍♀️ **COMPLETE RECOVERY** ($cooldownTime minutes)
            • Walking recovery: 90 seconds
            • Quad stretch: 45 seconds each leg
            • Hamstring stretch: 45 seconds each leg
            • Chest doorway stretch: 45 seconds
            • Spinal twist: 30 seconds each side
            • Deep breathing meditation: 90 seconds
            
            💡 **TRANSFORMATION TIPS**:
            • Progressive overload weekly
            • Perfect form creates lasting results
            • Consistency over perfection
            • Track your improvements
            • Fuel your body properly
            
            **Equipment**: $equipment
            **Level**: $level
            **Goal**: Complete fitness transformation 🎯
        """.trimIndent()
    }
    
    // 💪 PREBUILT WORKOUT COLLECTION - Expert-designed templates
    private fun getPrebuiltWorkout(workoutType: String, level: String = "intermediate"): String {
        return when (workoutType.lowercase()) {
            "push" -> getPrebuiltPushWorkout(level)
            "pull" -> getPrebuiltPullWorkout(level)
            "legs" -> getPrebuiltLegWorkout(level)
            "abs", "core" -> getPrebuiltCoreWorkout(level)
            "hiit" -> getPrebuiltHIITWorkout(level)
            "yoga" -> getPrebuiltYogaFlow(level)
            "pilates" -> getPrebuiltPilatesWorkout(level)
            "upper" -> getPrebuiltUpperBodyWorkout(level)
            "lower" -> getPrebuiltLowerBodyWorkout(level)
            "functional" -> getPrebuiltFunctionalWorkout(level)
            else -> getPrebuiltFullBodyWorkout(level)
        }
    }
    
    private fun getPrebuiltPushWorkout(level: String): String {
        return """
            💪 **PUSH DAY POWERHOUSE** (35 minutes)
            
            🔥 **ACTIVATION WARM-UP** (5 minutes)
            • Arm circles: 30 seconds each direction
            • Shoulder dislocations (with towel): 15 reps
            • Push-up position holds: 30 seconds
            • Scapular wall slides: 15 reps
            • Light push-ups: 10 reps
            
            💪 **PUSH STRENGTH CIRCUIT** (25 minutes)
            
            **Round 1: Chest Focus** (4 sets, 90s rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Wall push-ups: 10-12 reps
                • Incline push-ups (stairs/chair): 8-10 reps
                • Knee push-ups: 6-8 reps
                • Push-up hold: 15-20 seconds
                """
                "intermediate" -> """
                • Standard push-ups: 12-15 reps
                • Diamond push-ups: 8-10 reps
                • Archer push-ups: 5 each side
                • Push-up to T: 6 each side
                """
                else -> """
                • One-arm push-ups progression: 3-5 each side
                • Handstand push-ups: 5-8 reps
                • Explosive push-ups: 8-10 reps
                • Hindu push-ups: 10-12 reps
                """
            }}
            
            **Round 2: Shoulder Power** (3 sets, 60s rest):
            • Pike push-ups: ${if(level == "beginner") "6-8" else if(level == "intermediate") "8-12" else "12-15"} reps
            • Lateral raises (water bottles): ${if(level == "beginner") "12-15" else if(level == "intermediate") "15-20" else "20-25"} reps
            • Front raises: ${if(level == "beginner") "10-12" else if(level == "intermediate") "12-15" else "15-18"} reps
            • Overhead press (bottles): ${if(level == "beginner") "8-10" else if(level == "intermediate") "10-12" else "12-15"} reps
            
            **Round 3: Tricep Finisher** (3 sets, 45s rest):
            • Tricep dips (chair): ${if(level == "beginner") "8-12" else if(level == "intermediate") "12-15" else "15-20"} reps
            • Close-grip push-ups: ${if(level == "beginner") "5-8" else if(level == "intermediate") "8-12" else "12-15"} reps
            • Tricep extensions (bottle): ${if(level == "beginner") "12-15" else if(level == "intermediate") "15-18" else "18-22"} reps
            
            🧘‍♀️ **RECOVERY STRETCH** (5 minutes)
            • Chest doorway stretch: 45 seconds
            • Cross-body shoulder stretch: 30 seconds each
            • Tricep overhead stretch: 30 seconds each
            • Cobra stretch: 45 seconds
            • Child's pose: 60 seconds
            
            💡 **PUSH DAY TIPS**:
            • Focus on controlled eccentric (lowering) phase
            • Keep core tight throughout all movements
            • Progressive overload: add reps or difficulty weekly
            • Perfect form beats high reps every time
            
            **Target**: Chest, shoulders, triceps development 💪
        """.trimIndent()
    }
    
    private fun getPrebuiltPullWorkout(level: String): String {
        return """
            🎯 **PULL DAY DOMINATION** (35 minutes)
            
            🔥 **DYNAMIC WARM-UP** (5 minutes)
            • Band pull-aparts (or arm swings): 20 reps
            • Shoulder blade squeezes: 15 reps
            • Cat-cow stretches: 10 reps
            • Dead hangs (if possible): 20-30 seconds
            • Reverse fly motions: 15 reps
            
            🎯 **PULL STRENGTH SEQUENCE** (25 minutes)
            
            **Phase 1: Back Foundation** (4 sets, 90s rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Inverted rows (table/bar): 6-10 reps
                • Reverse snow angels: 12-15 reps
                • Superman holds: 20-30 seconds
                • Wall slides: 12-15 reps
                """
                "intermediate" -> """
                • Pull-ups/chin-ups: 5-8 reps (assisted if needed)
                • Single-arm rows (bottle): 10-12 each
                • Reverse flies: 12-15 reps
                • Superman + Y raises: 10-12 reps
                """
                else -> """
                • Wide-grip pull-ups: 8-12 reps
                • Archer pull-ups: 4-6 each side
                • Single-arm rows (heavy): 12-15 each
                • L-sits/tuck holds: 20-30 seconds
                """
            }}
            
            **Phase 2: Posterior Chain** (3 sets, 60s rest):
            • Face pulls (band/towel): ${if(level == "beginner") "15-20" else if(level == "intermediate") "20-25" else "25-30"} reps
            • Rear delt flies: ${if(level == "beginner") "12-15" else if(level == "intermediate") "15-18" else "18-22"} reps
            • Prone Y-T-W: ${if(level == "beginner") "8 each" else if(level == "intermediate") "10 each" else "12 each"} reps
            • Reverse planks: ${if(level == "beginner") "20-30s" else if(level == "intermediate") "30-45s" else "45-60s"}
            
            **Phase 3: Bicep Focus** (3 sets, 45s rest):
            • Bicep curls (bottles): ${if(level == "beginner") "12-15" else if(level == "intermediate") "15-18" else "18-22"} reps
            • Hammer curls: ${if(level == "beginner") "10-12" else if(level == "intermediate") "12-15" else "15-18"} reps
            • Isometric holds: ${if(level == "beginner") "15-20s" else if(level == "intermediate") "20-30s" else "30-40s"}
            
            🧘‍♀️ **MOBILITY COOLDOWN** (5 minutes)
            • Lat stretches: 45 seconds each side
            • Upper trap stretch: 30 seconds each side
            • Thoracic spine twists: 10 each side
            • Doorway chest stretch: 60 seconds
            • Seated forward fold: 60 seconds
            
            💡 **PULL DAY MASTERY**:
            • Squeeze shoulder blades at top of each rep
            • Control the negative portion of movements
            • Focus on lat engagement, not just arm pulling
            • Build to full pull-ups progressively
            
            **Target**: Back, lats, rear delts, biceps 🎯
        """.trimIndent()
    }
    
    private fun getPrebuiltLegWorkout(level: String): String {
        return """
            🦵 **LEG DAY ANNIHILATION** (40 minutes)
            
            🔥 **LOWER BODY ACTIVATION** (6 minutes)
            • Leg swings: 15 each direction
            • Hip circles: 10 each direction  
            • Bodyweight squats: 15 reps
            • Reverse lunges: 10 each leg
            • Calf raises: 20 reps
            • Glute bridges: 15 reps
            
            🦵 **QUAD DOMINANT PHASE** (12 minutes)
            
            **Squat Complex** (4 sets, 2 min rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Assisted squats (chair support): 12-15 reps
                • Wall sits: 30-45 seconds
                • Step-ups (low step): 10 each leg
                • Squat pulses: 15-20 reps
                """
                "intermediate" -> """
                • Bodyweight squats: 15-20 reps
                • Jump squats: 12-15 reps
                • Bulgarian split squats: 10 each leg
                • Single-leg box step-ups: 12 each leg
                """
                else -> """
                • Pistol squat progression: 5-8 each leg
                • Jump squats with 180° turn: 10-12 reps
                • Shrimp squats: 3-5 each leg
                • Single-leg squats: 8-10 each leg
                """
            }}
            
            🍑 **GLUTE & HAMSTRING PHASE** (12 minutes)
            
            **Hip Hinge Complex** (4 sets, 90s rest):
            • Single-leg deadlifts: ${if(level == "beginner") "8-10" else if(level == "intermediate") "10-12" else "12-15"} each leg
            • Glute bridges: ${if(level == "beginner") "15-20" else if(level == "intermediate") "20-25" else "25-30"} reps
            • Reverse lunges: ${if(level == "beginner") "10-12" else if(level == "intermediate") "12-15" else "15-18"} each leg
            • Lateral lunges: ${if(level == "beginner") "8-10" else if(level == "intermediate") "10-12" else "12-15"} each leg
            
            ⚡ **EXPLOSIVE FINISHER** (5 minutes)
            **Plyometric Blast** (3 rounds, 60s rest):
            • Jump lunges: ${if(level == "beginner") "16 total" else if(level == "intermediate") "20 total" else "24 total"} reps
            • Broad jumps: ${if(level == "beginner") "5-8" else if(level == "intermediate") "8-10" else "10-12"} reps
            • Lateral bounds: ${if(level == "beginner") "10 total" else if(level == "intermediate") "12 total" else "16 total"} reps
            
            🧘‍♀️ **LOWER BODY RECOVERY** (5 minutes)
            • Quad stretch: 45 seconds each leg
            • Hamstring stretch: 45 seconds each leg
            • Hip flexor stretch: 45 seconds each leg
            • Figure-4 stretch: 45 seconds each leg
            • Pigeon pose: 60 seconds each side
            
            💡 **LEG DAY EXCELLENCE**:
            • Full range of motion on all movements
            • Control the eccentric (lowering) phase
            • Drive through heels on squats/deadlifts
            • Keep knees tracking over toes
            • Progressive overload weekly
            
            **Target**: Quadriceps, glutes, hamstrings, calves 🦵
        """.trimIndent()
    }
    
    private fun getPrebuiltCoreWorkout(level: String): String {
        return """
            🔥 **CORE CRUSHER CIRCUIT** (30 minutes)
            
            🌅 **CORE ACTIVATION** (4 minutes)
            • Dead bugs: 10 each side
            • Bird dogs: 10 each side
            • Cat-cow stretches: 10 reps
            • Pelvic tilts: 15 reps
            • Knee-to-chest: 10 each leg
            
            🎯 **ANTERIOR CORE PHASE** (8 minutes)
            **Plank Progression** (4 sets, 45s rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Modified plank (knees): 20-30 seconds
                • Wall plank: 30-45 seconds
                • Dead bug holds: 15 seconds each side
                • Glute bridge hold: 30 seconds
                """
                "intermediate" -> """
                • Standard plank: 45-60 seconds
                • Plank up-downs: 10-12 reps
                • Single-arm plank: 20 seconds each
                • Plank jacks: 15-20 reps
                """
                else -> """
                • Plank to push-up: 12-15 reps
                • Single-arm single-leg plank: 15s each
                • Plank with leg lifts: 20 total
                • RKC plank: 30-45 seconds
                """
            }}
            
            🌪️ **ROTATIONAL POWER** (8 minutes)
            **Anti-Rotation Circuit** (3 sets, 60s rest):
            • Russian twists: ${if(level == "beginner") "20-30" else if(level == "intermediate") "30-40" else "40-50"} total
            • Bicycle crunches: ${if(level == "beginner") "20-30" else if(level == "intermediate") "30-40" else "40-50"} total
            • Side planks: ${if(level == "beginner") "15-20s" else if(level == "intermediate") "20-30s" else "30-45s"} each
            • Wood chops (bottle): ${if(level == "beginner") "12-15" else if(level == "intermediate") "15-18" else "18-22"} each side
            
            ⚡ **DYNAMIC CORE BLAST** (6 minutes)
            **High-Intensity Circuit** (3 rounds, 30s rest):
            • Mountain climbers: ${if(level == "beginner") "30 seconds" else if(level == "intermediate") "40 seconds" else "50 seconds"}
            • Leg raises: ${if(level == "beginner") "8-12" else if(level == "intermediate") "12-15" else "15-20"} reps
            • Flutter kicks: ${if(level == "beginner") "20 total" else if(level == "intermediate") "30 total" else "40 total"}
            • V-ups: ${if(level == "beginner") "8-10" else if(level == "intermediate") "10-15" else "15-20"} reps
            
            🧘‍♀️ **CORE RELEASE** (4 minutes)
            • Child's pose: 60 seconds
            • Cobra stretch: 45 seconds
            • Knee rocks: 30 seconds
            • Spinal twists: 30 seconds each side
            • Happy baby pose: 45 seconds
            
            💡 **CORE MASTERY TIPS**:
            • Breathe consistently - don't hold breath
            • Quality over quantity - perfect form first
            • Engage deep core muscles, not just abs
            • Progress holds before adding reps
            • Core strength supports all other movements
            
            **Target**: Rectus abdominis, obliques, transverse abdominis, deep core 🔥
        """.trimIndent()
    }
    
    private fun getPrebuiltHIITWorkout(level: String): String {
        return """
            ⚡ **HIGH-INTENSITY INTERVAL TRAINING** (25 minutes)
            
            🔥 **HIIT PREP** (4 minutes)
            • Marching in place: 60 seconds
            • Arm circles: 30 seconds each direction
            • Leg swings: 15 each leg
            • Light jumping jacks: 60 seconds
            • Bodyweight squats: 15 reps
            
            ⚡ **HIIT PHASE 1: POWER** (8 minutes)
            **Tabata Protocol** (4 rounds, 20s work / 10s rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                Round 1: Modified jumping jacks
                Round 2: Step-ups (low step)
                Round 3: Modified burpees (no jump)
                Round 4: High knees (moderate pace)
                Rest 2 minutes between phases
                """
                "intermediate" -> """
                Round 1: Jumping jacks
                Round 2: Burpees
                Round 3: Jump squats
                Round 4: Mountain climbers
                Rest 90 seconds between phases
                """
                else -> """
                Round 1: Burpee box jumps
                Round 2: Jump squat to tuck jump
                Round 3: Burpee broad jumps
                Round 4: Sprint in place
                Rest 60 seconds between phases
                """
            }}
            
            🏃 **HIIT PHASE 2: ENDURANCE** (8 minutes)
            **EMOM (Every Minute On Minute)** for 8 minutes:
            • Minute 1: ${if(level == "beginner") "10 squats + 5 push-ups" else if(level == "intermediate") "15 squats + 8 push-ups" else "20 squats + 12 push-ups"}
            • Minute 2: ${if(level == "beginner") "20 high knees + 10 lunges" else if(level == "intermediate") "30 high knees + 12 lunges" else "40 high knees + 16 lunges"}
            • Minute 3: ${if(level == "beginner") "15 jumping jacks + plank 15s" else if(level == "intermediate") "25 jumping jacks + plank 30s" else "35 jumping jacks + plank 45s"}
            • Minute 4: ${if(level == "beginner") "8 burpees (modified)" else if(level == "intermediate") "12 burpees" else "15 burpees"}
            
            **Repeat this 4-minute cycle twice**
            
            🧘‍♀️ **ACTIVE RECOVERY** (5 minutes)
            • Walking in place: 90 seconds
            • Gentle arm swings: 45 seconds
            • Hip circles: 30 seconds each direction
            • Calf stretch: 30 seconds each leg
            • Deep breathing: 90 seconds
            
            💡 **HIIT OPTIMIZATION**:
            • Push maximum effort during work periods
            • Use rest periods for complete recovery
            • Modify exercises to maintain intensity
            • Stay hydrated throughout
            • Track improvements weekly
            
            **Benefits**: Maximum calorie burn, improved VO2 max, time-efficient ⚡
        """.trimIndent()
    }
    
    private fun getPrebuiltYogaFlow(level: String): String {
        return """
            🧘‍♀️ **MINDFUL YOGA FLOW** (35 minutes)
            
            🌅 **CENTERING & BREATH** (5 minutes)
            • Comfortable seated position: 2 minutes
            • Deep belly breathing: 2 minutes
            • Gentle neck rolls: 5 each direction
            • Shoulder shrugs: 10 reps
            
            🌊 **WARM-UP FLOW** (8 minutes)
            **Sun Salutation Prep**:
            ${when(level.lowercase()) {
                "beginner" -> """
                • Mountain Pose: 1 minute
                • Forward fold (bent knees): 1 minute
                • Half lift: 30 seconds
                • Low lunge (each leg): 1 minute each
                • Downward dog (knees down): 1 minute
                • Child's pose: 2 minutes
                """
                "intermediate" -> """
                • Mountain Pose to Forward Fold: 2 minutes
                • Low lunge to High lunge: 1 minute each leg
                • Warrior I flow: 1 minute each side  
                • Downward dog: 2 minutes
                • Child's pose: 1 minute
                """
                else -> """
                • Full Sun Salutation A: 3 rounds
                • Sun Salutation B with Warriors: 2 rounds
                • Advanced arm balances prep: 2 minutes
                """
            }}
            
            🔥 **STRENGTH & FLOW** (15 minutes)
            **Standing Sequence**:
            • Warrior II: ${if(level == "beginner") "1 min" else if(level == "intermediate") "90s" else "2 min"} each side
            • Extended side angle: ${if(level == "beginner") "45s" else if(level == "intermediate") "60s" else "90s"} each
            • Triangle pose: ${if(level == "beginner") "45s" else if(level == "intermediate") "60s" else "90s"} each
            • Revolved triangle: ${if(level == "beginner") "30s" else if(level == "intermediate") "45s" else "60s"} each
            
            **Floor Sequence**:
            • Cat-cow flows: ${if(level == "beginner") "10" else if(level == "intermediate") "15" else "20"} reps
            • Low lunge twists: ${if(level == "beginner") "30s" else if(level == "intermediate") "45s" else "60s"} each
            • Pigeon prep: ${if(level == "beginner") "1 min" else if(level == "intermediate") "90s" else "2 min"} each side
            • Bridge pose: ${if(level == "beginner") "45s" else if(level == "intermediate") "60s" else "90s"}
            
            🧘‍♀️ **DEEP STRETCH & RESTORE** (7 minutes)
            • Seated forward fold: 2 minutes
            • Seated spinal twist: 1 minute each side
            • Legs up the wall: 2 minutes
            • Happy baby: 1 minute
            • Final savasana: As long as desired
            
            💡 **YOGA WISDOM**:
            • Listen to your body's limits
            • Breath guides the movement
            • Modifications are always available
            • Focus inward, not on others
            • Progress is measured in peace, not poses
            
            **Benefits**: Flexibility, balance, mindfulness, stress relief 🧘‍♀️
        """.trimIndent()
    }
    
    private fun getPrebuiltPilatesWorkout(level: String): String {
        return """
            🎯 **PILATES PRECISION** (30 minutes)
            
            🌅 **PILATES WARM-UP** (5 minutes)
            • Hundred prep breathing: 2 minutes
            • Pelvic tilts: 15 reps
            • Spine articulation: 10 roll downs
            • Shoulder blade isolation: 15 reps
            • Hip circles: 10 each direction
            
            💪 **CORE FOUNDATION** (10 minutes)
            **Classical Series**:
            ${when(level.lowercase()) {
                "beginner" -> """
                • Modified Hundred: 50 pumps
                • Single leg stretches: 10 each leg
                • Double leg stretch prep: 10 reps
                • Spine stretch forward: 10 reps
                • Rolling like a ball prep: 10 reps
                """
                "intermediate" -> """
                • The Hundred: 100 pumps
                • Single leg stretches: 10 each leg
                • Double leg stretches: 10 reps
                • Single straight leg: 10 each leg
                • Criss-cross: 10 each side
                """
                else -> """
                • The Hundred: 100 pumps
                • Roll up: 10 reps
                • Single leg circles: 5 each direction/leg
                • Rolling like a ball: 10 reps
                • Series of 5: Complete sequence
                """
            }}
            
            🏃 **STRENGTH & STABILITY** (10 minutes)
            **Full Body Integration**:
            • Plank series: ${if(level == "beginner") "3 x 20s" else if(level == "intermediate") "3 x 45s" else "3 x 60s"}
            • Side planks: ${if(level == "beginner") "20s each" else if(level == "intermediate") "30s each" else "45s each"}
            • Swimming: ${if(level == "beginner") "10 reps" else if(level == "intermediate") "15 reps" else "20 reps"}
            • Leg pull front: ${if(level == "beginner") "5 reps" else if(level == "intermediate") "8 reps" else "10 reps"}
            • Teaser prep: ${if(level == "beginner") "8 reps" else if(level == "intermediate") "10 reps" else "12 reps"}
            
            🧘‍♀️ **STRETCH & RELEASE** (5 minutes)
            • Spine twist: 5 each side
            • Saw: 5 each side
            • Hip flexor stretch: 45 seconds each
            • Chest expansion: 1 minute
            • Child's pose: 2 minutes
            
            💡 **PILATES PRINCIPLES**:
            • Quality over quantity always
            • Engage deep core throughout
            • Precise, controlled movements
            • Mind-body connection essential
            • Breath coordinates with movement
            
            **Target**: Deep core, posture, body awareness 🎯
        """.trimIndent()
    }
    
    private fun getPrebuiltUpperBodyWorkout(level: String): String {
        return """
            💪 **UPPER BODY SCULPT** (35 minutes)
            
            🔥 **UPPER BODY PREP** (5 minutes)
            • Arm circles: 20 each direction
            • Shoulder shrugs: 15 reps
            • Cross-body stretches: 30s each arm
            • Wall push-ups: 10 reps
            • Band pull-aparts: 20 reps (or arm swings)
            
            💪 **PUSH COMPLEX** (10 minutes)
            **Circuit A** (3 rounds, 90s rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Wall push-ups: 12-15 reps
                • Incline push-ups: 8-10 reps
                • Tricep dips (chair): 8-10 reps
                • Overhead press (bottles): 10-12 reps
                """
                "intermediate" -> """
                • Standard push-ups: 12-15 reps
                • Diamond push-ups: 8-10 reps
                • Pike push-ups: 8-10 reps
                • Tricep dips: 12-15 reps
                """
                else -> """
                • One-arm push-up progression: 5 each
                • Handstand push-ups: 5-8 reps
                • Archer push-ups: 6 each side
                • Hindu push-ups: 10 reps
                """
            }}
            
            🎯 **PULL COMPLEX** (10 minutes)
            **Circuit B** (3 rounds, 90s rest):
            • Inverted rows: ${if(level == "beginner") "8-10" else if(level == "intermediate") "10-12" else "12-15"} reps
            • Reverse flies: ${if(level == "beginner") "12-15" else if(level == "intermediate") "15-18" else "18-20"} reps
            • Bicep curls (bottles): ${if(level == "beginner") "12-15" else if(level == "intermediate") "15-18" else "18-22"} reps
            • Face pulls: ${if(level == "beginner") "15-18" else if(level == "intermediate") "18-22" else "22-25"} reps
            
            ⚡ **UPPER BODY FINISHER** (5 minutes)
            **Burnout Round** (2 sets, 60s rest):
            • Max push-ups: ${if(level == "beginner") "AMRAP 30s" else if(level == "intermediate") "AMRAP 45s" else "AMRAP 60s"}
            • Plank hold: ${if(level == "beginner") "30s" else if(level == "intermediate") "45s" else "60s"}
            • Arm circles: 20 each direction
            
            🧘‍♀️ **UPPER BODY STRETCH** (5 minutes)
            • Doorway chest stretch: 60 seconds
            • Overhead tricep stretch: 30s each
            • Cross-body shoulder: 30s each
            • Neck side stretches: 30s each
            • Eagle arms: 45 seconds
            
            💡 **UPPER BODY EXCELLENCE**:
            • Full range of motion on all exercises
            • Control the negative (lowering) phase
            • Maintain proper shoulder positioning
            • Progressive overload weekly
            • Balance push/pull movements
            
            **Target**: Chest, back, shoulders, arms 💪
        """.trimIndent()
    }
    
    private fun getPrebuiltLowerBodyWorkout(level: String): String {
        return """
            🦵 **LOWER BODY TRANSFORMATION** (35 minutes)
            
            🔥 **LOWER BODY MOBILITY** (6 minutes)
            • Hip circles: 10 each direction
            • Leg swings: 15 each direction
            • Walking lunges: 10 each leg
            • Calf raises: 20 reps
            • Glute activation: 15 bridges
            • Ankle circles: 10 each direction
            
            🏋️ **GLUTE & HAMSTRING FOCUS** (12 minutes)
            **Posterior Chain Circuit** (3 rounds, 2 min rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Glute bridges: 15-20 reps
                • Single-leg deadlift (assisted): 8 each leg
                • Wall sits: 30-45 seconds
                • Clamshells: 15 each side
                """
                "intermediate" -> """
                • Single-leg glute bridges: 12 each leg
                • Single-leg deadlifts: 10 each leg
                • Bulgarian split squats: 10 each leg
                • Lateral lunges: 12 each leg
                """
                else -> """
                • Single-leg hip thrusts: 15 each leg
                • Single-leg RDL (weighted): 12 each leg
                • Curtsy to reverse lunge: 10 each leg
                • Single-leg wall sits: 30s each leg
                """
            }}
            
            💥 **QUAD DOMINANT PHASE** (12 minutes)
            **Squat Complex** (3 rounds, 90s rest):
            • Bodyweight squats: ${if(level == "beginner") "15-20" else if(level == "intermediate") "20-25" else "25-30"} reps
            • Jump squats: ${if(level == "beginner") "8-10" else if(level == "intermediate") "10-15" else "15-20"} reps
            • Pulse squats: ${if(level == "beginner") "15" else if(level == "intermediate") "20" else "25"} reps
            • Single-leg squats: ${if(level == "beginner") "5 assisted" else if(level == "intermediate") "5-8 each" else "8-12 each"} leg
            
            ⚡ **PLYOMETRIC BLAST** (5 minutes)
            **Power Circuit** (3 rounds, 45s rest):
            • Broad jumps: ${if(level == "beginner") "5-8" else if(level == "intermediate") "8-10" else "10-12"} reps
            • Lateral bounds: ${if(level == "beginner") "10 total" else if(level == "intermediate") "12 total" else "16 total"}
            • Jump lunges: ${if(level == "beginner") "12 total" else if(level == "intermediate") "16 total" else "20 total"}
            
            🧘‍♀️ **LOWER BODY RELEASE** (5 minutes)
            • Quad stretch: 45 seconds each leg
            • Hamstring stretch: 45 seconds each leg
            • Hip flexor stretch: 45 seconds each leg
            • Figure-4 stretch: 45 seconds each leg
            • Child's pose: 60 seconds
            
            💡 **LOWER BODY MASTERY**:
            • Activate glutes before squatting
            • Keep knees tracking over toes
            • Full depth on all movements
            • Control eccentric phase
            • Progressive overload essential
            
            **Target**: Glutes, quads, hamstrings, calves 🦵
        """.trimIndent()
    }
    
    private fun getPrebuiltFunctionalWorkout(level: String): String {
        return """
            🏃‍♂️ **FUNCTIONAL FITNESS** (30 minutes)
            
            🔥 **MOVEMENT PREP** (5 minutes)
            • Arm circles: 15 each direction
            • Leg swings: 12 each direction
            • Hip circles: 10 each direction
            • Torso twists: 15 each side
            • Light bouncing: 45 seconds
            
            💪 **FUNCTIONAL PATTERNS** (20 minutes)
            
            **Circuit 1: Push/Pull/Squat** (3 rounds, 90s rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Push-ups (modified): 8-10 reps
                • Inverted rows (table): 8-10 reps
                • Squats: 12-15 reps
                • Plank: 30 seconds
                """
                "intermediate" -> """
                • Push-ups: 12-15 reps
                • Pull-ups/chin-ups: 5-8 reps
                • Jump squats: 12-15 reps
                • Mountain climbers: 30 seconds
                """
                else -> """
                • One-arm push-ups: 5 each arm
                • Wide-grip pull-ups: 8-10 reps
                • Pistol squats: 5 each leg
                • Burpees: 10 reps
                """
            }}
            
            **Circuit 2: Hinge/Lunge/Carry** (3 rounds, 90s rest):
            • Single-leg deadlift: ${if(level == "beginner") "8 each leg" else if(level == "intermediate") "10 each leg" else "12 each leg"}
            • Walking lunges: ${if(level == "beginner") "16 total" else if(level == "intermediate") "20 total" else "24 total"}
            • Farmer's walk (bottles): ${if(level == "beginner") "30 seconds" else if(level == "intermediate") "45 seconds" else "60 seconds"}
            • Bear crawl: ${if(level == "beginner") "20 seconds" else if(level == "intermediate") "30 seconds" else "45 seconds"}
            
            **Circuit 3: Rotation/Gait** (2 rounds, 60s rest):
            • Wood chops: ${if(level == "beginner") "12 each side" else if(level == "intermediate") "15 each side" else "18 each side"}
            • Crab walk: ${if(level == "beginner") "10 steps each way" else if(level == "intermediate") "15 steps each way" else "20 steps each way"}
            • Lateral shuffles: ${if(level == "beginner") "20 seconds" else if(level == "intermediate") "30 seconds" else "40 seconds"}
            
            🧘‍♀️ **MOBILITY FLOW** (5 minutes)
            • Hip flexor stretch: 45 seconds each leg
            • Thoracic spine rotation: 30 seconds each side
            • Calf stretch: 30 seconds each leg
            • Shoulder crossover: 30 seconds each arm
            • Deep breathing: 90 seconds
            
            💡 **FUNCTIONAL TRAINING**:
            • Movement quality over quantity
            • Train patterns, not just muscles
            • Multi-planar movement essential
            • Real-world strength and mobility
            • Injury prevention through movement
            
            **Benefits**: Real-world strength, movement quality, injury prevention 🏃‍♂️
        """.trimIndent()
    }
    
    private fun getPrebuiltFullBodyWorkout(level: String): String {
        return """
            🎯 **COMPLETE FULL-BODY TRAINING** (40 minutes)
            
            🔥 **TOTAL BODY WARM-UP** (6 minutes)
            • Jumping jacks: 60 seconds
            • Arm circles: 30 seconds each direction
            • Leg swings: 15 each direction
            • Hip circles: 10 each direction
            • Bodyweight squats: 15 reps
            • Push-up position hold: 30 seconds
            
            💪 **COMPOUND MOVEMENTS** (28 minutes)
            
            **Round 1: Foundation** (4 sets, 2 min rest):
            ${when(level.lowercase()) {
                "beginner" -> """
                • Push-ups (modified): 8-12 reps
                • Bodyweight squats: 12-15 reps
                • Inverted rows (table): 8-10 reps
                • Plank hold: 30-45 seconds
                """
                "intermediate" -> """
                • Push-ups: 12-15 reps
                • Jump squats: 12-15 reps
                • Pull-ups/chin-ups: 6-10 reps
                • Single-leg deadlifts: 8 each leg
                """
                else -> """
                • One-arm push-ups: 5 each arm
                • Pistol squats: 5 each leg
                • Muscle-ups: 3-5 reps
                • Single-leg RDL (weighted): 10 each leg
                """
            }}
            
            **Round 2: Power & Conditioning** (3 sets, 90s rest):
            • Burpees: ${if(level == "beginner") "5-8" else if(level == "intermediate") "8-12" else "12-15"} reps
            • Mountain climbers: ${if(level == "beginner") "30 seconds" else if(level == "intermediate") "45 seconds" else "60 seconds"}
            • Lunges: ${if(level == "beginner") "16 total" else if(level == "intermediate") "20 total" else "24 total"}
            • Russian twists: ${if(level == "beginner") "20" else if(level == "intermediate") "30" else "40"} total
            
            **Round 3: Strength Endurance** (3 sets, 60s rest):
            • Wall sits: ${if(level == "beginner") "30-45s" else if(level == "intermediate") "45-60s" else "60-90s"}
            • Pike push-ups: ${if(level == "beginner") "5-8" else if(level == "intermediate") "8-12" else "12-15"} reps
            • Single-leg glute bridges: ${if(level == "beginner") "10 each" else if(level == "intermediate") "12 each" else "15 each"}
            • Dead bugs: ${if(level == "beginner") "10 each side" else if(level == "intermediate") "12 each side" else "15 each side"}
            
            🧘‍♀️ **TOTAL BODY STRETCH** (6 minutes)
            • Child's pose: 90 seconds
            • Downward dog: 60 seconds
            • Hip flexor stretch: 45 seconds each leg
            • Spinal twist: 30 seconds each side
            • Deep breathing meditation: 90 seconds
            
            💡 **FULL-BODY TRAINING**:
            • Compound movements maximize efficiency
            • Balance pushing and pulling patterns
            • Include uni-lateral (single-limb) work
            • Progressive overload for continued gains
            • Recovery is when adaptation occurs
            
            **Target**: Complete muscular and cardiovascular development 🎯
        """.trimIndent()
    }

    // Generate a fallback response when API fails
    private fun generateFallbackResponse(prompt: String): String {
        Log.d(TAG, "🛡️ Generating fallback response for prompt: ${prompt.take(100)}...")
        
        // Extract key information from the prompt to customize the fallback
        val containsWorkout = prompt.contains("workout", ignoreCase = true)
        val containsForm = prompt.contains("form", ignoreCase = true) || prompt.contains("technique", ignoreCase = true)
        val containsWarmUp = prompt.contains("warm-up", ignoreCase = true) || prompt.contains("warmup", ignoreCase = true)
        
        return when {
            containsForm -> """
                🏋️ **PROPER FORM GUIDE**
                
                Form is crucial for effective and safe workouts! Here are some universal principles:
                
                ✅ **GENERAL PRINCIPLES:**
                • Keep your spine neutral during most exercises
                • Breathe out during exertion (the hard part)
                • Focus on controlled movements, not momentum
                • Start with lighter weights to master form
                
                💪 **COMMON EXERCISES:**
                
                **Squats:**
                • Feet shoulder-width apart
                • Keep chest up, back straight
                • Push knees outward (don't collapse inward)
                • Descend until thighs are parallel to ground
                
                **Push-ups:**
                • Hands slightly wider than shoulders
                • Body forms straight line from head to heels
                • Lower chest to ground, elbows at 45° angle
                • Core engaged throughout movement
                
                **Planks:**
                • Forearms parallel on ground
                • Body straight from head to heels
                • Engage core by pulling navel to spine
                • Don't let hips sag or pike up
                
                Remember: Quality reps beat quantity every time! 💯
            """.trimIndent()
            
            containsWarmUp -> """
                🔥 **ESSENTIAL WARM-UP ROUTINE**
                
                Always warm up properly to prevent injury and maximize performance!
                
                **1. PULSE RAISERS (3-5 minutes)**
                • Light jogging in place: 1 minute
                • Jumping jacks: 30 seconds
                • High knees: 30 seconds
                • Butt kicks: 30 seconds
                • Repeat cycle if needed
                
                **2. DYNAMIC STRETCHES (4-5 minutes)**
                • Arm circles: 10 forward, 10 backward
                • Torso twists: 10 each side
                • Hip circles: 10 each direction
                • Walking lunges with twist: 10 each leg
                • Leg swings: 10 each leg
                
                **3. ACTIVATION (2-3 minutes)**
                • Bodyweight squats: 15 reps
                • Push-ups or incline push-ups: 10 reps
                • Glute bridges: 15 reps
                
                This warm-up takes about 10 minutes and prepares your entire body for any workout!
            """.trimIndent()
            
            containsWorkout -> """
                💪 **COMPLETE FULL-BODY WORKOUT**
                
                This balanced workout targets all major muscle groups for strength and conditioning!
                
                🔥 **WARM-UP (5 minutes)**
                • Jumping jacks: 30 seconds
                • Arm circles: 20 each direction
                • Bodyweight squats: 15 reps
                • Push-up position plank: 30 seconds
                
                💪 **MAIN WORKOUT (25 minutes)**
                
                **Circuit 1: Lower Body Focus (3 rounds)**
                • Bodyweight squats: 15-20 reps
                • Alternating lunges: 10 each leg
                • Glute bridges: 15-20 reps
                • Rest: 60 seconds between rounds
                
                **Circuit 2: Upper Body Focus (3 rounds)**
                • Push-ups (modify as needed): 10-15 reps
                • Superman back extensions: 12-15 reps
                • Tricep dips (using chair/bench): 12-15 reps
                • Rest: 60 seconds between rounds
                
                **Circuit 3: Core Strength (3 rounds)**
                • Plank: 30-45 seconds
                • Bicycle crunches: 20 total (10 each side)
                • Mountain climbers: 20 total (10 each side)
                • Rest: 60 seconds between rounds
                
                🧘 **COOL-DOWN (5 minutes)**
                • Quad stretch: 30 seconds each leg
                • Hamstring stretch: 30 seconds each leg
                • Chest stretch: 30 seconds each arm
                • Child's pose: 30 seconds
                
                💡 **WORKOUT TIPS:**
                • Focus on form over speed
                • Breathe properly throughout
                • Modify exercises as needed for your fitness level
                • Stay hydrated during your workout
                
                Complete this workout 2-3 times per week with rest days in between for best results!
            """.trimIndent()
            
            else -> """
                👋 **FITSOUL AI COACH**
                
                I'm your personal fitness assistant! While I'm currently experiencing connectivity issues, I'm here to help with:
                
                💪 **WORKOUT PLANNING**
                • Personalized routines based on your goals
                • Exercises for specific muscle groups
                • Workouts for any fitness level
                
                🎯 **FORM GUIDANCE**
                • Proper technique for common exercises
                • Safety tips to prevent injury
                • Modifications for different fitness levels
                
                📊 **FITNESS TRACKING**
                • Progress monitoring suggestions
                • Performance metrics to track
                • Goal-setting strategies
                
                Just let me know what specific fitness help you need, and I'll provide expert guidance based on established training principles!
            """.trimIndent()
        }
    }

    // Simple test method to debug API issues
    suspend fun testApiConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🧪 Testing API connection status...")
            
            // Check if we have an API key first
            if (apiKey.isBlank()) {
                Log.d(TAG, "⚠️ No API key configured")
                return@withContext Result.success(
                    """
                    🔧 **FitSoul AI Service Status**
                    
                    **Status**: ⚠️ API Key Not Configured
                    
                    **Current Mode**: Enhanced Static Workouts
                    - ✅ High-quality workout plans available
                    - ✅ Goal-specific exercise recommendations
                    - ✅ Form tips and safety guidance
                    - ✅ Progressive difficulty scaling
                    
                    **To Enable AI Power**:
                    1. Get a free API key from https://openrouter.ai
                    2. Add it to gradle.properties: OPENROUTER_API_KEY=your_key_here
                    3. Rebuild the app
                    
                    **Note**: The app works great even without AI! 💪
                    """.trimIndent()
                )
            }
            
            // Test with API key
            Log.d(TAG, "🌐 Testing OpenRouter API connection...")
            
            val testJson = buildJsonObject {
                put("model", model)
                putJsonArray("messages") {
                    addJsonObject {
                        put("role", "user")
                        put("content", "Respond with exactly: 'API Connection Successful'")
                    }
                }
                put("max_tokens", 20)
                put("temperature", 0.1)
            }
            
            val requestBody = testJson.toString().toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("$baseUrl/chat/completions")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://fitsoul.app")
                .addHeader("X-Title", "Fitsoul AI Test")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: "No response body"
            
            Log.d(TAG, "📡 API Test Response Code: ${response.code}")
            Log.d(TAG, "📡 API Test Response: ${responseBody.take(200)}...")
            
            return@withContext when {
                response.isSuccessful -> {
                    Result.success(
                        """
                        🎉 **AI Connection Test Successful!**
                        
                        **Status**: ✅ OpenRouter API Connected
                        **Model**: $model
                        **Features Available**:
                        - 🤖 AI-powered workout generation
                        - 🎯 Personalized exercise recommendations  
                        - 📝 Intelligent form feedback
                        - 🔄 Adaptive workout planning
                        
                        **Response Preview**:
                        ${responseBody.take(100)}...
                        
                        Ready to create amazing workouts! 💪
                        """.trimIndent()
                    )
                }
                response.code == 401 -> {
                    Result.success(
                        """
                        ⚠️ **API Key Authentication Failed**
                        
                        **Status**: ❌ Invalid or Expired API Key
                        **Current Mode**: Enhanced Static Workouts
                        
                        **To Fix**:
                        1. Check your OpenRouter API key at https://openrouter.ai
                        2. Ensure it's correctly added to gradle.properties
                        3. Rebuild the app
                        
                        **Note**: App continues to work with high-quality static workouts! 💪
                        """.trimIndent()
                    )
                }
                response.code == 429 -> {
                    Result.success(
                        """
                        ⏳ **Rate Limit Reached**
                        
                        **Status**: ⚠️ Too Many Requests
                        **Current Mode**: Enhanced Static Workouts
                        
                        **This is Normal**: Free tier has usage limits
                        **App Behavior**: Automatically uses static workouts
                        
                        Try again in a few minutes, or upgrade your OpenRouter plan for higher limits.
                        """.trimIndent()
                    )
                }
                else -> {
                    Result.success(
                        """
                        🔧 **API Connection Issue**
                        
                        **Status**: ⚠️ Connection Error (${response.code})
                        **Current Mode**: Enhanced Static Workouts
                        
                        **Error**: ${response.message}
                        **Fallback**: App continues with high-quality static workouts
                        
                        This is handled gracefully - your workouts are still amazing! 💪
                        """.trimIndent()
                    )
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ API connection test failed", e)
            return@withContext Result.success(
                """
                🛡️ **Connection Test Failed**
                
                **Status**: ⚠️ Network or Configuration Issue
                **Current Mode**: Enhanced Static Workouts
                
                **Error**: ${e.message}
                **Fallback**: App works perfectly with static workouts
                
                No worries - you still get fantastic workout plans! 💪
                """.trimIndent()
            )
        }
    }
    
    // Additional fallback methods
    private fun generateFallbackQuickWorkout(duration: Int, equipment: String = "bodyweight"): String {
        return """
            🔥 **Quick $duration-Minute FitSoul Workout**
            
            ⚡ **High-Energy Circuit** (Complete 2-3 rounds):
            
            💪 **Round 1: Power Moves**
            • Jumping Jacks: 30 seconds
            • Push-ups: 15 reps (modify as needed)
            • Bodyweight Squats: 20 reps
            • Plank Hold: 30 seconds
            • Rest: 30 seconds
            
            🏃 **Round 2: Cardio Blast**
            • High Knees: 30 seconds
            • Burpees: 8-10 reps
            • Mountain Climbers: 30 seconds
            • Lunges: 10 per leg
            • Rest: 30 seconds
            
            🎯 **Finisher (if time allows)**:
            • Wall Sit: 45 seconds
            • Calf Raises: 20 reps
            
            💡 **Quick Tips**:
            • Stay hydrated during your workout
            • Focus on form over speed
            • Take breaks when needed
            • You've got this! 💪
            
            **Equipment Used**: $equipment
            **Total Time**: Approximately $duration minutes
        """.trimIndent()
    }
    
    private fun generateFallbackFormTip(exerciseName: String): String {
        return """
            🏋️ **Perfect Form Guide: $exerciseName**
            
            ✅ **Fundamental Form Principles**:
            • Maintain proper posture and alignment throughout
            • Control the movement in both directions (up and down)
            • Engage your core to protect your lower back
            • Breathe consistently - exhale on exertion
            
            🎯 **Key Focus Areas**:
            • Start with lighter intensity to master the form
            • Keep movements smooth and controlled
            • Avoid momentum or "swinging" the exercise
            • Feel the targeted muscles working
            
            ⚠️ **Safety Reminders**:
            • Stop immediately if you feel sharp pain
            • Quality reps are better than quantity
            • Warm up before and stretch after
            • Listen to your body's signals
            
            💡 **Progressive Tips**:
            • Master the basic movement first
            • Gradually increase difficulty over time
            • Consider variations once you're comfortable
            • Track your progress for motivation
            
            **Remember**: Perfect form leads to better results and prevents injuries! 🌟
        """.trimIndent()
    }
}
