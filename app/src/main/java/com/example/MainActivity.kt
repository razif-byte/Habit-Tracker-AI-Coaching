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
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Layers
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
import com.example.ui.screens.AppActivityMonitorScreen
import com.example.ui.screens.GamificationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SocialScreen
import com.example.ui.screens.TrendingHubScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HabitViewModel

enum class Screen(val route: String, val title: String, val icon: ImageVector) {
    HOME("home", "Habits", Icons.Default.CheckCircle),
    TRENDING("trending", "Trending", Icons.Default.Explore),
    MONITOR("monitor", "Tracker", Icons.Default.Layers),
    COACH("coach", "AI Coach", Icons.Default.Psychology),
    ANALYTICS("analytics", "Analytics", Icons.Default.Assessment)
}

class MainActivity : ComponentActivity() {

    private val habitViewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle incoming shared link or text from external apps (e.g. YouTube, Smule, Browser)
        handleIncomingIntent(intent)

        setContent {
            val userProfile by habitViewModel.userProfile.collectAsStateWithLifecycle()
            val isDark = userProfile?.isDarkMode ?: false
            val initialTab = intent?.getStringExtra("OPEN_TAB")

            MyApplicationTheme(darkTheme = isDark, dynamicColor = false) {
                MainAppScreen(viewModel = habitViewModel, initialTab = initialTab)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: android.content.Intent?) {
        if (intent?.action == android.content.Intent.ACTION_SEND && intent.type?.startsWith("text/") == true) {
            val sharedText = intent.getStringExtra(android.content.Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val isKaraoke = sharedText.contains("youtube", ignoreCase = true) || sharedText.contains("smule", ignoreCase = true)
                val category = if (isKaraoke) "MALAY_KARAOKE" else "UTILITY"
                val platform = if (sharedText.contains("smule", ignoreCase = true)) "SMULE" else if (sharedText.contains("youtube", ignoreCase = true)) "YOUTUBE" else "WEB"
                val title = if (isKaraoke) "Pautan Karaoke Dikongsi" else "Pautan Luar Dikongsi"

                habitViewModel.addTrendingLink(
                    title = title,
                    subtitle = sharedText.take(50),
                    url = sharedText,
                    category = category,
                    platform = platform
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: HabitViewModel, initialTab: String? = null) {
    val navController = rememberNavController()
    var currentScreen by remember {
        mutableStateOf(
            when (initialTab) {
                "trending" -> Screen.TRENDING
                "monitor" -> Screen.MONITOR
                else -> Screen.HOME
            }
        )
    }
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
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                maxLines = 1,
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
            startDestination = currentScreen.route,
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
            composable(Screen.TRENDING.route) {
                TrendingHubScreen(viewModel = viewModel)
            }
            composable(Screen.MONITOR.route) {
                AppActivityMonitorScreen(viewModel = viewModel)
            }
            composable(Screen.COACH.route) {
                AICoachScreen(viewModel = viewModel)
            }
            composable(Screen.ANALYTICS.route) {
                AnalyticsScreen(viewModel = viewModel)
            }
            composable("social") {
                SocialScreen(viewModel = viewModel)
            }
            composable("gamification") {
                GamificationScreen(viewModel = viewModel)
            }
            composable("profile") {
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
