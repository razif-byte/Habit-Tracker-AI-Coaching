package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.remote.AIEngineType
import com.example.ui.components.WatermarkBar
import com.example.ui.viewmodel.HabitViewModel
import com.example.util.CoachPersona
import com.example.util.LiveVoiceState
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AICoachScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingAI.collectAsStateWithLifecycle()
    val analysisReport by viewModel.aiAnalysisReport.collectAsStateWithLifecycle()
    val goalBreakdown by viewModel.goalBreakdownResult.collectAsStateWithLifecycle()

    val selectedEngine by viewModel.selectedAIEngine.collectAsStateWithLifecycle()
    val isLiveOpen by viewModel.isLiveSessionOpen.collectAsStateWithLifecycle()
    val liveState by viewModel.liveVoiceState.collectAsStateWithLifecycle()
    val liveAudioRms by viewModel.liveAudioRms.collectAsStateWithLifecycle()
    val liveTranscript by viewModel.liveInterimTranscript.collectAsStateWithLifecycle()
    val selectedPersona by viewModel.selectedPersona.collectAsStateWithLifecycle()
    val customOpenAiKey by viewModel.customOpenAiKey.collectAsStateWithLifecycle()
    val customGeminiKey by viewModel.customGeminiKey.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var showGoalBreakdownDialog by remember { mutableStateOf(false) }
    var targetGoalInput by remember { mutableStateOf("") }
    var showAnalysisDialog by remember { mutableStateOf(false) }
    var showApiKeyConfigDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.openLiveSession()
            viewModel.startListening()
        }
    }

    LaunchedEffect(messages.size, isGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "Bagaimana atasi rasa malas hari ini?",
        "Bina rutin pagi Habit Stacking 3 langkah",
        "Bagaimana pulihkan streak yang putus?",
        "Ajarkan saya Peraturan 2 Minit Atomic Habits"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    when (selectedEngine) {
                                        AIEngineType.GEMINI_FLASH -> Color(0xFF1E88E5)
                                        AIEngineType.CHATGPT_4O, AIEngineType.CHATGPT_MINI -> Color(0xFF10A37F)
                                        AIEngineType.GEMINI_LIVE -> Color(0xFF8E24AA)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (selectedEngine) {
                                    AIEngineType.GEMINI_FLASH -> Icons.Default.AutoAwesome
                                    AIEngineType.CHATGPT_4O, AIEngineType.CHATGPT_MINI -> Icons.Default.SmartToy
                                    AIEngineType.GEMINI_LIVE -> Icons.Default.Mic
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Coach Zenith",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (selectedEngine) {
                                                AIEngineType.GEMINI_FLASH -> Color(0xFF1E88E5)
                                                AIEngineType.CHATGPT_4O, AIEngineType.CHATGPT_MINI -> Color(0xFF10A37F)
                                                AIEngineType.GEMINI_LIVE -> Color(0xFF8E24AA)
                                            }
                                        )
                                )
                            }
                            Text(
                                text = "${selectedEngine.displayName} • ${selectedEngine.providerName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick Launch Gemini Live button
                    IconButton(
                        onClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                viewModel.openLiveSession()
                                viewModel.startListening()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8E24AA).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Buka Gemini Live",
                                tint = Color(0xFF8E24AA),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // API Key & Model Settings
                    IconButton(onClick = { showApiKeyConfigDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Tetapan AI & Kunci API",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Deep Analysis trigger button
                    IconButton(
                        onClick = {
                            viewModel.requestDeepAIAnalysis()
                            showAnalysisDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = "AI Analysis",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            // Chat Input Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                when (selectedEngine) {
                                    AIEngineType.GEMINI_FLASH -> "Tanya Coach (Gemini 3.5)..."
                                    AIEngineType.CHATGPT_4O -> "Tanya Coach (ChatGPT-4o)..."
                                    AIEngineType.CHATGPT_MINI -> "Tanya Coach (GPT-4o Mini)..."
                                    AIEngineType.GEMINI_LIVE -> "Tanya Coach (Gemini Live)..."
                                }
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("coach_chat_input"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Voice Mic or Send button
                    if (inputText.isBlank()) {
                        IconButton(
                            onClick = {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    viewModel.openLiveSession()
                                    viewModel.startListening()
                                } else {
                                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8E24AA))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Gemini Live Suara",
                                tint = Color.White
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank() && !isGenerating) {
                                    viewModel.sendChatMessage(inputText)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("send_chat_button"),
                            enabled = !isGenerating
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Hantar",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(70.dp)) // clearance for bottom navigation
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Watermark Bar
            WatermarkBar()

            // AI Model / Engine Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gemini 3.5 chip
                EngineSelectorPill(
                    title = "Gemini 3.5",
                    icon = Icons.Default.AutoAwesome,
                    selected = selectedEngine == AIEngineType.GEMINI_FLASH,
                    activeColor = Color(0xFF1E88E5),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setAIEngine(AIEngineType.GEMINI_FLASH) }
                )

                // ChatGPT-4o chip
                EngineSelectorPill(
                    title = "ChatGPT",
                    icon = Icons.Default.SmartToy,
                    selected = selectedEngine == AIEngineType.CHATGPT_4O || selectedEngine == AIEngineType.CHATGPT_MINI,
                    activeColor = Color(0xFF10A37F),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setAIEngine(AIEngineType.CHATGPT_4O) }
                )

                // Gemini Live chip
                EngineSelectorPill(
                    title = "Gemini Live",
                    icon = Icons.Default.Mic,
                    selected = selectedEngine == AIEngineType.GEMINI_LIVE,
                    activeColor = Color(0xFF8E24AA),
                    modifier = Modifier.weight(1.1f),
                    onClick = {
                        viewModel.openLiveSession()
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPermission) {
                            viewModel.startListening()
                        } else {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }

            // AI Tool Badges (Deep Analysis & Goal Breakdown triggers)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            viewModel.requestDeepAIAnalysis()
                            showAnalysisDialog = true
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Analisis Tabiat AI",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showGoalBreakdownDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pecahan Matlamat ➔ Tabiat",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Suggested Prompt Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.clickable {
                            viewModel.sendChatMessage(prompt)
                        }
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Message Bubble List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        onSpeak = { text -> viewModel.speakCoachMessage(text) },
                        onStopSpeak = { viewModel.stopSpeaking() }
                    )
                }

                if (isGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Coach Zenith (${selectedEngine.displayName}) sedang berfikir...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // --- GEMINI LIVE VOICE INTERACTIVE SESSION DIALOG ---
    if (isLiveOpen) {
        GeminiLiveVoiceDialog(
            voiceState = liveState,
            audioRms = liveAudioRms,
            interimTranscript = liveTranscript,
            selectedPersona = selectedPersona,
            onDismiss = { viewModel.closeLiveSession() },
            onToggleListen = {
                if (liveState == LiveVoiceState.LISTENING) {
                    viewModel.stopListening()
                } else {
                    viewModel.startListening()
                }
            },
            onStopSpeaking = { viewModel.stopSpeaking() },
            onSelectPersona = { persona -> viewModel.setPersona(persona) },
            onQuickSpokenPrompt = { prompt ->
                viewModel.sendChatMessage(prompt, engineOverride = AIEngineType.GEMINI_LIVE)
            }
        )
    }

    // --- API KEY & MODEL CONFIGURATION DIALOG ---
    if (showApiKeyConfigDialog) {
        ApiKeyConfigDialog(
            initialOpenAiKey = customOpenAiKey,
            initialGeminiKey = customGeminiKey,
            currentEngine = selectedEngine,
            onDismiss = { showApiKeyConfigDialog = false },
            onSave = { oKey, gKey, engine ->
                viewModel.saveApiKeys(oKey, gKey)
                viewModel.setAIEngine(engine)
                showApiKeyConfigDialog = false
            }
        )
    }

    // AI Deep Analysis Dialog
    if (showAnalysisDialog) {
        Dialog(onDismissRequest = { showAnalysisDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analisis Tabiat Mendalam",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showAnalysisDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isGenerating && analysisReport == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Text(
                            text = analysisReport ?: "Tekan Jana untuk menganalisis corak tabiat anda.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showAnalysisDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Tutup & Amalkan Cadangan")
                    }
                }
            }
        }
    }

    // Goal Breakdown Dialog
    if (showGoalBreakdownDialog) {
        Dialog(onDismissRequest = { showGoalBreakdownDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pecahan Matlamat",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showGoalBreakdownDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = targetGoalInput,
                        onValueChange = { targetGoalInput = it },
                        label = { Text("Apakah matlamat utama anda?") },
                        placeholder = { Text("cth. Lari maraton, Bina otot, Baca 12 buku") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (targetGoalInput.isNotBlank()) {
                                viewModel.requestGoalBreakdown(targetGoalInput)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = targetGoalInput.isNotBlank() && !isGenerating
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pecah ke Tabiat Atomik")
                    }

                    if (goalBreakdown != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = goalBreakdown ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(14.dp),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EngineSelectorPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) activeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.5.dp,
            if (selected) activeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                ),
                color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessageEntity,
    onSpeak: (String) -> Unit,
    onStopSpeak: () -> Unit
) {
    val isUser = message.sender == "user"
    var isSpeakingThis by remember { mutableStateOf(false) }

    val engineType = when (message.suggestionType) {
        "chatgpt_4o", "chatgpt_mini" -> AIEngineType.CHATGPT_4O
        "gemini_live" -> AIEngineType.GEMINI_LIVE
        else -> AIEngineType.GEMINI_FLASH
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when (engineType) {
                            AIEngineType.GEMINI_FLASH -> Color(0xFF1E88E5)
                            AIEngineType.CHATGPT_4O, AIEngineType.CHATGPT_MINI -> Color(0xFF10A37F)
                            AIEngineType.GEMINI_LIVE -> Color(0xFF8E24AA)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (engineType) {
                        AIEngineType.GEMINI_FLASH -> Icons.Default.AutoAwesome
                        AIEngineType.CHATGPT_4O, AIEngineType.CHATGPT_MINI -> Icons.Default.SmartToy
                        AIEngineType.GEMINI_LIVE -> Icons.Default.Mic
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Engine Header badge for Coach
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (engineType) {
                                            AIEngineType.GEMINI_FLASH -> Color(0xFF1E88E5).copy(alpha = 0.15f)
                                            AIEngineType.CHATGPT_4O, AIEngineType.CHATGPT_MINI -> Color(0xFF10A37F).copy(alpha = 0.15f)
                                            AIEngineType.GEMINI_LIVE -> Color(0xFF8E24AA).copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = engineType.badgeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (engineType) {
                                        AIEngineType.GEMINI_FLASH -> Color(0xFF1565C0)
                                        AIEngineType.CHATGPT_4O, AIEngineType.CHATGPT_MINI -> Color(0xFF0F766E)
                                        AIEngineType.GEMINI_LIVE -> Color(0xFF6A1B9A)
                                    },
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Audio Text-to-Speech Speak button
                        IconButton(
                            onClick = {
                                if (isSpeakingThis) {
                                    onStopSpeak()
                                    isSpeakingThis = false
                                } else {
                                    onSpeak(message.message)
                                    isSpeakingThis = true
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeakingThis) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.GraphicEq,
                                contentDescription = "Dengar Suara AI Coach",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

// --- GEMINI LIVE INTERACTIVE FULL VOICE DIALOG ---
@Composable
fun GeminiLiveVoiceDialog(
    voiceState: LiveVoiceState,
    audioRms: Float,
    interimTranscript: String,
    selectedPersona: CoachPersona,
    onDismiss: () -> Unit,
    onToggleListen: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSelectPersona: (CoachPersona) -> Unit,
    onQuickSpokenPrompt: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8E24AA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Gemini Live Voice",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Interaktif Suara Masa Nyata",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsing Interactive Audio Avatar
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glowing pulsing ring
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .scale(if (voiceState == LiveVoiceState.LISTENING || voiceState == LiveVoiceState.SPEAKING) pulseScale else 1.0f)
                            .clip(CircleShape)
                            .background(
                                when (voiceState) {
                                    LiveVoiceState.LISTENING -> Color(0xFFE91E63).copy(alpha = 0.2f)
                                    LiveVoiceState.SPEAKING -> Color(0xFF8E24AA).copy(alpha = 0.25f)
                                    LiveVoiceState.PROCESSING -> Color(0xFF1E88E5).copy(alpha = 0.2f)
                                    LiveVoiceState.IDLE -> Color.Gray.copy(alpha = 0.1f)
                                }
                            )
                    )

                    // Middle ring
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .clip(CircleShape)
                            .background(
                                when (voiceState) {
                                    LiveVoiceState.LISTENING -> Color(0xFFE91E63).copy(alpha = 0.4f)
                                    LiveVoiceState.SPEAKING -> Color(0xFF8E24AA).copy(alpha = 0.4f)
                                    LiveVoiceState.PROCESSING -> Color(0xFF1E88E5).copy(alpha = 0.4f)
                                    LiveVoiceState.IDLE -> Color.Gray.copy(alpha = 0.2f)
                                }
                            )
                    )

                    // Core Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF8E24AA), Color(0xFFE91E63))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (voiceState) {
                                LiveVoiceState.LISTENING -> Icons.Default.Mic
                                LiveVoiceState.SPEAKING -> Icons.AutoMirrored.Filled.VolumeUp
                                LiveVoiceState.PROCESSING -> Icons.Default.Psychology
                                LiveVoiceState.IDLE -> Icons.Default.GraphicEq
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Label
                Text(
                    text = when (voiceState) {
                        LiveVoiceState.LISTENING -> "🎙️ Mendengar suara anda..."
                        LiveVoiceState.PROCESSING -> "🧠 Memproses jawapan..."
                        LiveVoiceState.SPEAKING -> "🔊 Coach sedang bercakap..."
                        LiveVoiceState.IDLE -> "⏸️ Tekan mikrofon untuk mula bercakap"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = when (voiceState) {
                        LiveVoiceState.LISTENING -> Color(0xFFE91E63)
                        LiveVoiceState.SPEAKING -> Color(0xFF8E24AA)
                        LiveVoiceState.PROCESSING -> Color(0xFF1E88E5)
                        LiveVoiceState.IDLE -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Live Audio Waveform Canvas
                LiveWaveformVisualizer(
                    isActive = voiceState == LiveVoiceState.LISTENING || voiceState == LiveVoiceState.SPEAKING,
                    audioRms = audioRms,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Live Transcript Box
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (interimTranscript.isNotBlank()) {
                                "\"$interimTranscript\""
                            } else {
                                "Sebut soalan anda seperti: \"Bagaimana atasi malas hari ini?\""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Persona Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CoachPersona.entries.forEach { persona ->
                        val isSelected = selectedPersona == persona
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFF8E24AA).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF8E24AA) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.clickable { onSelectPersona(persona) }
                        ) {
                            Text(
                                text = persona.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color(0xFF8E24AA) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Spoken Prompts
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(listOf(
                        "Semak tabiat hari ini",
                        "Beri kata semangat 2 minit",
                        "Bagaimana atasi malas sekarang?"
                    )) { prompt ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.clickable { onQuickSpokenPrompt(prompt) }
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Voice Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stop Speech button
                    OutlinedButton(
                        onClick = onStopSpeaking,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Henti Suara", fontSize = 11.sp)
                    }

                    // Main Push-to-Talk / Toggle Listen button
                    Button(
                        onClick = onToggleListen,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (voiceState == LiveVoiceState.LISTENING) Color(0xFFE91E63) else Color(0xFF8E24AA)
                        )
                    ) {
                        Icon(
                            imageVector = if (voiceState == LiveVoiceState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (voiceState == LiveVoiceState.LISTENING) "Henti Mendengar" else "Mula Bercakap",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Live Waveform Visualizer on Canvas
@Composable
fun LiveWaveformVisualizer(
    isActive: Boolean,
    audioRms: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val amplitude = if (isActive) (audioRms.coerceIn(0.15f, 1.0f) * (height / 2.2f)) else 4f

        val path1 = Path()
        val path2 = Path()

        path1.moveTo(0f, centerY)
        path2.moveTo(0f, centerY)

        val step = 8f
        var x = 0f
        while (x <= width) {
            val progress = x / width
            val envelope = sin(progress * Math.PI).toFloat() // window envelope to taper at ends
            val y1 = centerY + sin((x * 0.04f) + phase).toFloat() * amplitude * envelope
            val y2 = centerY + sin((x * 0.05f) - phase).toFloat() * (amplitude * 0.7f) * envelope

            path1.lineTo(x, y1)
            path2.lineTo(x, y2)
            x += step
        }

        drawPath(
            path = path1,
            color = Color(0xFF8E24AA).copy(alpha = if (isActive) 0.85f else 0.25f),
            style = Stroke(width = 3.dp.toPx())
        )

        drawPath(
            path = path2,
            color = Color(0xFFE91E63).copy(alpha = if (isActive) 0.7f else 0.15f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

// --- API KEY & MODEL CONFIGURATION DIALOG ---
@Composable
fun ApiKeyConfigDialog(
    initialOpenAiKey: String,
    initialGeminiKey: String,
    currentEngine: AIEngineType,
    onDismiss: () -> Unit,
    onSave: (String, String, AIEngineType) -> Unit
) {
    var openAiKey by remember { mutableStateOf(initialOpenAiKey) }
    var geminiKey by remember { mutableStateOf(initialGeminiKey) }
    var selectedEngine by remember { mutableStateOf(currentEngine) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Konfigurasi Enjin AI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Pilih model lalai & masukkan kunci API peribadi jika ingin menggunakan kuota sendiri. Jika dikosongkan, enjin fallback pintar RazifApps@Nasadef® akan digunakan secara lancar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enjin Utama Aktif:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Engine Selector Radios
                AIEngineType.entries.forEach { engine ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedEngine == engine) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selectedEngine == engine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedEngine = engine }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (engine) {
                                    AIEngineType.GEMINI_FLASH -> Icons.Default.AutoAwesome
                                    AIEngineType.CHATGPT_4O, AIEngineType.CHATGPT_MINI -> Icons.Default.SmartToy
                                    AIEngineType.GEMINI_LIVE -> Icons.Default.Mic
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = engine.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = engine.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            }
                            if (selectedEngine == engine) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Dipilih",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // OpenAI API Key input
                OutlinedTextField(
                    value = openAiKey,
                    onValueChange = { openAiKey = it },
                    label = { Text("OpenAI API Key (sk-...)") },
                    placeholder = { Text("sk-proj-...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Gemini API Key input
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    label = { Text("Google Gemini API Key (AIzaSy...)") },
                    placeholder = { Text("AIzaSy...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "🔒 Privasi Terjamin: Kunci API disimpan secara selamat di dalam peranti (SharedPreferences) dan tidak dihantar ke mana-mana pelayan pihak ketiga kecuali terus ke API rasmi pembekal (Google / OpenAI).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp),
                        fontSize = 10.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onSave(openAiKey, geminiKey, selectedEngine) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Simpan & Guna Tetapan")
                }
            }
        }
    }
}
