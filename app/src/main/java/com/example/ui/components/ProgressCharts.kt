package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.HabitLogEntity
import com.example.ui.theme.Amber500
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Violet500
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WeeklyCompletionChart(
    allLogs: List<HabitLogEntity>,
    totalHabitsCount: Int,
    modifier: Modifier = Modifier
) {
    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Calculate logs for each of the last 7 days ending today
    val calendar = Calendar.getInstance()
    val weeklyData = mutableListOf<Pair<String, Float>>() // Day Label -> Completion Ratio (0f to 1f)

    for (i in 6 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        val dateStr = dateFormat.format(cal.time)
        val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)

        val completedOnDate = allLogs.count { it.dateString == dateStr && it.isCompleted }
        val ratio = if (totalHabitsCount > 0) {
            (completedOnDate.toFloat() / totalHabitsCount.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
        weeklyData.add(Pair(dayOfWeek, ratio))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Activity",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Last 7 Days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bar chart row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { (label, ratio) ->
                    val animatedRatio by animateFloatAsState(
                        targetValue = if (ratio > 0f) ratio else 0.08f,
                        animationSpec = tween(durationMillis = 600),
                        label = "barRatio"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        Text(
                            text = "${(ratio * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (ratio > 0f) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((80 * animatedRatio).dp.coerceAtLeast(6.dp))
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (ratio >= 0.8f) Emerald500
                                    else if (ratio > 0f) Emerald400.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyHeatmapGrid(
    allLogs: List<HabitLogEntity>,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()

    // Generate 28 days (4 weeks x 7 days)
    val past28Days = (27 downTo 0).map { i ->
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -i)
        val dateStr = dateFormat.format(c.time)
        val count = allLogs.count { it.dateString == dateStr && it.isCompleted }
        Pair(dateStr, count)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Habit Consistency Heatmap",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "28-Day Grid",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 rows of 7 cells
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val index = row * 7 + col
                            val item = past28Days.getOrNull(index)
                            val count = item?.second ?: 0

                            val cellColor = when {
                                count >= 3 -> Emerald500
                                count == 2 -> Emerald400.copy(alpha = 0.8f)
                                count == 1 -> Emerald400.copy(alpha = 0.4f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellColor),
                                contentAlignment = Alignment.Center
                            ) {
                                if (count > 0) {
                                    Text(
                                        text = "$count",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        fontSize = 11.sp,
                                        color = if (count >= 2) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Heatmap Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Less", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    Emerald400.copy(alpha = 0.4f),
                    Emerald400.copy(alpha = 0.8f),
                    Emerald500
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                Text("More", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun CategoryDistributionCard(
    habits: List<HabitEntity>,
    modifier: Modifier = Modifier
) {
    val categoryCounts = habits.groupBy { it.category }.mapValues { it.value.size }
    val total = habits.size.coerceAtLeast(1)

    val categoryColors = mapOf(
        "Health" to Cyan500,
        "Fitness" to Emerald500,
        "Mindfulness" to Indigo500,
        "Learning" to Violet500,
        "Productivity" to Amber500
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "Category Balance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            categoryCounts.forEach { (cat, count) ->
                val ratio = count.toFloat() / total.toFloat()
                val color = categoryColors[cat] ?: Emerald500

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$count habits (${(ratio * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}
