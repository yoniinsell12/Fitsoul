package com.fitsoul.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import com.fitsoul.app.core.auth.GoogleSignInHelper
import com.fitsoul.app.core.auth.GoogleSignInManager
import com.fitsoul.app.core.navigation.FitsoulNavigation
import com.fitsoul.app.core.theme.FitsoulTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    
    @Inject
    lateinit var googleSignInHelper: GoogleSignInHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "MainActivity onCreate - Enhanced Fitsoul startup")

        try {
            // Make app edge-to-edge for modern UI
            WindowCompat.setDecorFitsSystemWindows(window, false)
            Log.d(TAG, "Edge-to-edge setup completed")
            
            // Initialize Google Sign-In
            initializeGoogleSignIn()

            setContent {
                FitsoulTheme {
                    FitsoulNavigation()
                }
            }
            
            Log.d(TAG, "MainActivity onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Critical error in MainActivity onCreate", e)
            // Could implement a fallback activity or crash gracefully
            finish()
        }
    }
    
    private fun initializeGoogleSignIn() {
        try {
            val webClientId = getString(R.string.default_web_client_id)
            googleSignInHelper.initialize(this, webClientId)
            GoogleSignInManager.setHelper(googleSignInHelper)
            Log.d(TAG, "Google Sign-In initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Google Sign-In", e)
        }
    }
}

@Composable
private fun ErrorScreen(error: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Fitsoul",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = "Something went wrong during startup:",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Please restart the app or contact support if the problem persists.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
