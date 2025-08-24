package com.fitsoul.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitsoul.app.core.theme.FitsoulColors
import com.fitsoul.app.ui.viewmodel.AuthViewModel

data class ProfileMenuItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.observeAsState()
    val user = authState?.getUser()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val menuItems = listOf(
        ProfileMenuItem(
            title = "Personal Information",
            icon = Icons.Default.Person,
            onClick = { /* Navigate to personal info */ }
        ),
        ProfileMenuItem(
            title = "Fitness Goals",
            icon = Icons.Default.FitnessCenter,
            onClick = { /* Navigate to goals */ }
        ),
        ProfileMenuItem(
            title = "Workout History",
            icon = Icons.Default.History,
            onClick = { /* Navigate to history */ }
        ),
        ProfileMenuItem(
            title = "Achievements",
            icon = Icons.Default.EmojiEvents,
            onClick = { /* Navigate to achievements */ }
        ),
        ProfileMenuItem(
            title = "Settings",
            icon = Icons.Default.Settings,
            onClick = { /* Navigate to settings */ }
        ),
        ProfileMenuItem(
            title = "Help & Support",
            icon = Icons.Default.Help,
            onClick = { /* Navigate to help */ }
        ),
        ProfileMenuItem(
            title = "Privacy Policy",
            icon = Icons.Default.PrivacyTip,
            onClick = { /* Navigate to privacy */ }
        )
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(FitsoulColors.Background),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    FitsoulColors.Primary,
                                    FitsoulColors.Secondary
                                )
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.displayName?.firstOrNull()?.toString()?.uppercase() ?: "U",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // User Info
                        Text(
                            text = user?.displayName ?: "Fitness Enthusiast",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        user?.email?.let { email ->
                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStat(
                                value = "28",
                                label = "Workouts",
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStat(
                                value = "12.5",
                                label = "Hours",
                                modifier = Modifier.weight(1f)
                            )
                            ProfileStat(
                                value = "2,840",
                                label = "Calories",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        
        item {
            // Menu Items
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FitsoulColors.SurfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    menuItems.forEach { item ->
                        ProfileMenuItemRow(
                            item = item,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        item {
            // Sign Out Button
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = FitsoulColors.Error
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(FitsoulColors.Error, FitsoulColors.Error)
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for navigation
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Sign Out",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to sign out of your account?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSignOut()
                        showLogoutDialog = false
                    }
                ) {
                    Text(
                        text = "Sign Out",
                        color = FitsoulColors.Error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        text = "Cancel",
                        color = FitsoulColors.Primary
                    )
                }
            },
            containerColor = FitsoulColors.SurfaceVariant
        )
    }
}

@Composable
private fun ProfileStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ProfileMenuItemRow(
    item: ProfileMenuItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        onClick = item.onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = FitsoulColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = FitsoulColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            
            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = FitsoulColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}