package com.fitsoul.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.ui.components.FitsoulPrimaryButton

@Composable
fun PhoneAuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (!isCodeSent) "Phone Verification" else "Enter Code",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = FitsoulColors.Primary,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        if (!isCodeSent) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("+1234567890") }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            FitsoulPrimaryButton(
                text = if (isLoading) "Sending Code..." else "Send Code",
                onClick = {
                    isLoading = true
                    // Simulate code sent for demo
                    isCodeSent = true
                    isLoading = false
                },
                enabled = !isLoading && phoneNumber.isNotBlank()
            )
        } else {
            Text(
                text = "We sent a verification code to $phoneNumber",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("Verification Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("123456") }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            FitsoulPrimaryButton(
                text = if (isLoading) "Verifying..." else "Verify",
                onClick = {
                    isLoading = true
                    // Simulate verification success for demo
                    onAuthSuccess()
                },
                enabled = !isLoading && verificationCode.length == 6
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(
                onClick = {
                    isCodeSent = false
                    verificationCode = ""
                }
            ) {
                Text(
                    text = "Resend Code",
                    color = FitsoulColors.Primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onNavigateBack) {
            Text(
                text = "Back",
                color = FitsoulColors.Primary
            )
        }
    }
}
