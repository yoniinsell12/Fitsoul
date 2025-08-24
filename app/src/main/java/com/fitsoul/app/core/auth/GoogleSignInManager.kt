package com.fitsoul.app.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel

object GoogleSignInManager {
    private var helper: GoogleSignInHelper? = null
    
    fun setHelper(googleSignInHelper: GoogleSignInHelper) {
        helper = googleSignInHelper
    }
    
    fun getHelper(): GoogleSignInHelper? = helper
}

@Composable
fun rememberGoogleSignInHelper(): GoogleSignInHelper? {
    return remember { GoogleSignInManager.getHelper() }
}
