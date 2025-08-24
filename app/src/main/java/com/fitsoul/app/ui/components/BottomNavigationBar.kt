package com.fitsoul.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fitsoul.app.core.theme.FitsoulColors

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

@Composable
fun FitsoulBottomNavigationBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = "ai_coach",
            title = "AI Coach",
            icon = Icons.Outlined.Psychology,
            selectedIcon = Icons.Filled.Psychology
        ),
        BottomNavItem(
            route = "my_workouts",
            title = "My Workouts",
            icon = Icons.Outlined.FitnessCenter,
            selectedIcon = Icons.Filled.FitnessCenter
        ),
        BottomNavItem(
            route = "progress",
            title = "Progress",
            icon = Icons.AutoMirrored.Outlined.TrendingUp,
            selectedIcon = Icons.AutoMirrored.Filled.TrendingUp
        ),
        BottomNavItem(
            route = "profile",
            title = "Profile",
            icon = Icons.Outlined.Person,
            selectedIcon = Icons.Filled.Person
        )
    )
    
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    
    NavigationBar(
        modifier = modifier,
        containerColor = FitsoulColors.Surface,
        contentColor = FitsoulColors.OnSurface
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FitsoulColors.Primary,
                    selectedTextColor = FitsoulColors.Primary,
                    unselectedIconColor = FitsoulColors.TextSecondary,
                    unselectedTextColor = FitsoulColors.TextSecondary,
                    indicatorColor = FitsoulColors.Primary.copy(alpha = 0.1f)
                )
            )
        }
    }
}
