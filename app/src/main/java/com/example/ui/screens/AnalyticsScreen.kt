package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BehaviorTrendReportCard
import com.example.ui.components.CategoryDistributionCard
import com.example.ui.components.MissedHabitsPatternCard
import com.example.ui.components.MonthlyHeatmapGrid
import com.example.ui.components.MoodCorrelationCard
import com.example.ui.components.WatermarkBar
import com.example.ui.components.WeeklyCompletionChart
import com.example.ui.theme.Amber500
import com.example.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.allHabits.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()
    val allMoodLogs by viewModel.allMoodLogs.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val totalLogs = allLogs.count { it.isCompleted }
    val totalPossibleIn7Days = (habits.size * 7).coerceAtLeast(1)
    val past7DaysCompleted = allLogs.take(habits.size * 7).count { it.isCompleted }
    val consistencyRate = ((past7DaysCompleted.toFloat() / totalPossibleIn7Days.toFloat()) * 100).toInt().coerceIn(0, 100)

    val bestStreak = habits.maxOfOrNull { it.bestStreak } ?: 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Advanced Analytics",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Watermark Bar
            item {
                WatermarkBar()
            }

            // Consistency Score Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "7-DAY CONSISTENCY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (consistencyRate >= 80) "Elite Momentum 🔥" else if (consistencyRate >= 50) "Solid Progress ⚡" else "Building Rhythm 🌱",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "High habit adherence locks in automated neurological habit loops.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { consistencyRate / 100f },
                                modifier = Modifier.size(68.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 7.dp,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text(
                                text = "$consistencyRate%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Lifetime Stats 4-Grid Card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatTile(
                        title = "Check-ins",
                        value = "${userProfile?.totalHabitsCompleted ?: totalLogs}",
                        icon = Icons.Default.CheckCircle,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "Best Streak",
                        value = "${bestStreak}d",
                        icon = Icons.Default.LocalFireDepartment,
                        iconColor = Amber500,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "Active",
                        value = "${habits.size}",
                        icon = Icons.Default.Assessment,
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        title = "Total XP",
                        value = "${userProfile?.xp ?: 0}",
                        icon = Icons.Default.Stars,
                        iconColor = Amber500,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 1. Feature: Mood vs. Habit Completion Correlation Module
            item {
                MoodCorrelationCard(
                    moodLogs = allMoodLogs,
                    habitLogs = allLogs,
                    habits = habits
                )
            }

            // 2. Feature: Drop-off & Missed Habit Pattern Diagnostic
            item {
                MissedHabitsPatternCard(
                    allLogs = allLogs,
                    habits = habits
                )
            }

            // 3. Feature: Personalized Trend & Longitudinal Report
            item {
                BehaviorTrendReportCard(
                    habits = habits,
                    totalCompleted = userProfile?.totalHabitsCompleted ?: totalLogs
                )
            }

            // Weekly Completion Bar Chart
            item {
                WeeklyCompletionChart(
                    allLogs = allLogs,
                    totalHabitsCount = habits.size
                )
            }

            // 28-Day Heatmap Grid
            item {
                MonthlyHeatmapGrid(allLogs = allLogs)
            }

            // Category Distribution
            item {
                CategoryDistributionCard(habits = habits)
            }
        }
    }
}

@Composable
fun StatTile(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
