package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.HabitEntity
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Rose500
import com.example.ui.theme.Violet500

data class HabitTemplate(
    val title: String,
    val category: String,
    val target: Int,
    val unit: String,
    val time: String,
    val icon: String,
    val color: String
)

@Composable
fun AddEditHabitDialog(
    initialHabit: HabitEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        category: String,
        targetCount: Int,
        unit: String,
        reminderTime: String,
        frequencyDays: String,
        iconName: String,
        colorHex: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialHabit?.title ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }
    var category by remember { mutableStateOf(initialHabit?.category ?: "Health") }
    var targetCount by remember { mutableStateOf((initialHabit?.targetCount ?: 1).toString()) }
    var unit by remember { mutableStateOf(initialHabit?.unit ?: "times") }
    var reminderTime by remember { mutableStateOf(initialHabit?.reminderTime ?: "08:00") }
    var selectedIcon by remember { mutableStateOf(initialHabit?.iconName ?: "LocalDrink") }
    var selectedColor by remember { mutableStateOf(initialHabit?.colorHex ?: "#10B981") }

    val categories = listOf("Health", "Fitness", "Mindfulness", "Learning", "Productivity")
    val units = listOf("times", "mins", "glasses", "pages", "steps")
    val colors = listOf("#10B981", "#06B6D4", "#6366F1", "#8B5CF6", "#F43F5E", "#F59E0B")
    val icons = listOf(
        "LocalDrink" to Icons.Default.LocalDrink,
        "FitnessCenter" to Icons.Default.FitnessCenter,
        "SelfImprovement" to Icons.Default.SelfImprovement,
        "MenuBook" to Icons.Default.MenuBook,
        "Work" to Icons.Default.Work
    )

    val templates = listOf(
        HabitTemplate("Hydrate (2L Water)", "Health", 8, "glasses", "08:30", "LocalDrink", "#06B6D4"),
        HabitTemplate("HIIT & Core Workout", "Fitness", 30, "mins", "07:00", "FitnessCenter", "#10B981"),
        HabitTemplate("Read Non-Fiction", "Learning", 15, "pages", "21:00", "MenuBook", "#8B5CF6"),
        HabitTemplate("Mindful Breathing", "Mindfulness", 10, "mins", "07:30", "SelfImprovement", "#6366F1")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_edit_habit_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialHabit == null) "Create New Habit" else "Edit Habit",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Quick Templates for new habits
                if (initialHabit == null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Quick Starter Presets",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        templates.take(2).forEach { t ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        title = t.title
                                        category = t.category
                                        targetCount = t.target.toString()
                                        unit = t.unit
                                        reminderTime = t.time
                                        selectedIcon = t.icon
                                        selectedColor = t.color
                                    }
                            ) {
                                Text(
                                    text = t.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Name") },
                    placeholder = { Text("e.g. Daily Meditation, 20 Pushups") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("habit_name_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target & Unit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = targetCount,
                        onValueChange = { targetCount = it },
                        label = { Text("Target") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.4f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        placeholder = { Text("mins, glasses") },
                        modifier = Modifier.weight(0.6f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reminder Time
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text("Daily Reminder Time (HH:mm)") },
                    placeholder = { Text("08:00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Color Selection
                Text(
                    text = "Accent Color",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    colors.forEach { hex ->
                        val col = parseHexColor(hex)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(col)
                                .border(
                                    width = if (selectedColor == hex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(
                                title,
                                description,
                                category,
                                targetCount.toIntOrNull() ?: 1,
                                unit,
                                reminderTime,
                                "Mon,Tue,Wed,Thu,Fri,Sat,Sun",
                                selectedIcon,
                                selectedColor
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_habit_button"),
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                ) {
                    Text(
                        text = if (initialHabit == null) "Create Habit" else "Save Changes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}
