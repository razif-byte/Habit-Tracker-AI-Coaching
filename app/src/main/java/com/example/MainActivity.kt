package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.MilestoneCelebrationDialog
import com.example.ui.screens.AICoachScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.GamificationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.GeoVioletPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HabitViewModel

enum class Screen(val route: String, val title: String, val icon: ImageVector) {
    HOME("home", "Habits", Icons.Default.CheckCircle),
    ANALYTICS("analytics", "Analytics", Icons.Default.Assessment),
    COACH("coach", "AI Coach", Icons.Default.Psychology),
    GAMIFICATION("gamification", "Badges", Icons.Default.EmojiEvents),
    PROFILE("profile", "Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {

    private val habitViewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by habitViewModel.userProfile.collectAsStateWithLifecycle()
            val isDark = userProfile?.isDarkMode ?: false

            MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
                MainAppScreen(viewModel = habitViewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: HabitViewModel) {
    val navController = rememberNavController()
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val activeMilestone by viewModel.activeMilestone.collectAsStateWithLifecycle()

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Screen.values().forEach { screen ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentScreen != screen) {
                                currentScreen = screen
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.HOME.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.HOME.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.HOME.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCoach = {
                        currentScreen = Screen.COACH
                        navController.navigate(Screen.COACH.route)
                    }
                )
            }
            composable(Screen.ANALYTICS.route) {
                AnalyticsScreen(viewModel = viewModel)
            }
            composable(Screen.COACH.route) {
                AICoachScreen(viewModel = viewModel)
            }
            composable(Screen.GAMIFICATION.route) {
                GamificationScreen(viewModel = viewModel)
            }
            composable(Screen.PROFILE.route) {
                ProfileScreen(viewModel = viewModel)
            }
        }

        // Milestone Celebration Modal
        activeMilestone?.let { data ->
            MilestoneCelebrationDialog(
                data = data,
                onDismiss = { viewModel.dismissMilestone() }
            )
        }
    }
}
