package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.TrendingLinkEntity
import com.example.ui.viewmodel.HabitViewModel
import com.example.util.AppIntegrationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingHubScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val trendingLinks by viewModel.filteredTrendingLinks.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedTrendingCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.trendingSearchQuery.collectAsStateWithLifecycle()
    val isFeedRefreshing by viewModel.isFeedRefreshing.collectAsStateWithLifecycle()
    val feedSyncStatus by viewModel.feedSyncStatus.collectAsStateWithLifecycle()
    val lastFeedSyncTime by viewModel.lastFeedSyncTime.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "ALL" to "Semua",
        "MALAY_KARAOKE" to "🎤 Karaoke Melayu",
        "AI" to "🤖 AI Apps",
        "SOCIAL" to "📱 Social Apps",
        "UTILITY" to "🛠 Utility Apps"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "TRENDING & HUB MEDIA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Aplikasi & Karaoke Trending",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshTrendingFeeds() },
                        enabled = !isFeedRefreshing,
                        modifier = Modifier.testTag("btn_refresh_feeds")
                    ) {
                        if (isFeedRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Segarkan Suapan RSS/API"
                            )
                        }
                    }
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("btn_add_trending_link")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Pautan Baharu"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_custom_link")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Tambah Pautan", fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setTrendingSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_trending_search"),
                    placeholder = { Text("Cari lagu Melayu, AI, TikTok, Notion...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { (key, label) ->
                        val selected = selectedCategory == key
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setTrendingCategory(key) },
                            label = { Text(label, fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            // Live Network Feed Sync Section
            item {
                LiveFeedNetworkCard(
                    isRefreshing = isFeedRefreshing,
                    statusMessage = feedSyncStatus,
                    lastSyncTime = lastFeedSyncTime,
                    selectedCategory = selectedCategory,
                    onRefreshAll = { viewModel.refreshTrendingFeeds("ALL") },
                    onRefreshCategory = { viewModel.refreshTrendingFeeds(selectedCategory) }
                )
            }

            // Featured Hero Card for Malay Karaoke if category is ALL or MALAY_KARAOKE
            if (selectedCategory == "ALL" || selectedCategory == "MALAY_KARAOKE") {
                item {
                    MalayKaraokeHeroBanner(
                        onOpenYouTube = {
                            AppIntegrationHelper.launchKaraokeYouTube(
                                context = context,
                                query = "karaoke melayu lirik trending",
                                fallbackUrl = "https://www.youtube.com/results?search_query=karaoke+melayu+lirik"
                            )
                        },
                        onOpenSmule = {
                            AppIntegrationHelper.launchKaraokeSmule(
                                context = context,
                                songTitle = "Karaoke Melayu",
                                fallbackUrl = "https://www.smule.com/search?q=karaoke+melayu"
                            )
                        }
                    )
                }
            }

            // Link count status
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Senarai Pautan (${trendingLinks.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tekan kad untuk buka",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Trending Link Cards
            if (trendingLinks.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🔍 Tiada pautan dijumpai", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Cuba carian lain atau tekan '+ Tambah Pautan'", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(trendingLinks, key = { it.id }) { item ->
                    TrendingItemCard(
                        item = item,
                        onOpen = {
                            viewModel.recordTrendingClick(item.id)
                            if (item.category == "MALAY_KARAOKE") {
                                if (item.platform == "SMULE") {
                                    AppIntegrationHelper.launchKaraokeSmule(context, item.title, item.url)
                                } else {
                                    AppIntegrationHelper.launchKaraokeYouTube(context, item.title, item.url)
                                }
                            } else {
                                AppIntegrationHelper.launchExternalAppOrWeb(context, item.packageName, item.url)
                            }
                        },
                        onOpenAlternate = {
                            // If YouTube song, allow quick launch to Smule or vice versa
                            if (item.category == "MALAY_KARAOKE") {
                                val altUrl = "https://www.smule.com/search?q=" + item.title.replace(" ", "+")
                                AppIntegrationHelper.launchKaraokeSmule(context, item.title, altUrl)
                            } else {
                                AppIntegrationHelper.openPlayStore(context, item.packageName)
                            }
                        },
                        onShare = {
                            AppIntegrationHelper.shareContent(
                                context = context,
                                title = item.title,
                                message = "Lihat ${item.title} di Habit Coach Hub:",
                                url = item.url
                            )
                        },
                        onToggleFavorite = {
                            viewModel.toggleFavoriteTrending(item.id, !item.isFavorite)
                        },
                        onAddAsHabit = {
                            val category = when (item.category) {
                                "MALAY_KARAOKE" -> "Mindfulness"
                                "AI" -> "Learning"
                                "SOCIAL" -> "Productivity"
                                else -> "Productivity"
                            }
                            val habitTitle = when (item.category) {
                                "MALAY_KARAOKE" -> "Sesi Karaoke: ${item.title.take(18)}"
                                "AI" -> "Eksplorasi AI: ${item.title.take(18)}"
                                else -> "Gunakan ${item.title.take(18)}"
                            }
                            viewModel.createHabitFromTrending(
                                title = habitTitle,
                                category = category,
                                iconName = if (item.category == "MALAY_KARAOKE") "Mic" else "Bolt"
                            )
                        },
                        onDelete = {
                            if (item.isCustom) {
                                viewModel.deleteTrendingLink(item)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomTrendingLinkDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, sub, url, cat, plat, pkg ->
                viewModel.addTrendingLink(title, sub, url, cat, plat, pkg)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MalayKaraokeHeroBanner(
    onOpenYouTube: () -> Unit,
    onOpenSmule: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1D2D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE91E63).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color(0xFFFF4081),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Koleksi Karaoke Melayu Viral",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Lagu Bunga Angkasa, Isabella, Tiara & Minus One",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0C4DD)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Rehatkan minda dengan menyanyi santai! Nyanyi secara duet di Smule atau tonton lirik penuh di YouTube.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD4BED1),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenYouTube,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("YouTube Lirik", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onOpenSmule,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3355FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Smule Duet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TrendingItemCard(
    item: TrendingLinkEntity,
    onOpen: () -> Unit,
    onOpenAlternate: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddAsHabit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryBadgeColor = when (item.category) {
        "MALAY_KARAOKE" -> Color(0xFFE91E63)
        "AI" -> Color(0xFF10A37F)
        "SOCIAL" -> Color(0xFF0284C7)
        else -> Color(0xFF7C3AED)
    }

    val categoryLabel = when (item.category) {
        "MALAY_KARAOKE" -> "🎤 KARAOKE"
        "AI" -> "🤖 AI"
        "SOCIAL" -> "📱 SOCIAL"
        else -> "🛠 UTILITY"
    }

    val iconVector: ImageVector = when (item.category) {
        "MALAY_KARAOKE" -> Icons.Default.MusicNote
        "AI" -> Icons.Default.AutoAwesome
        "SOCIAL" -> Icons.Default.Explore
        else -> Icons.Default.Widgets
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("trending_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Category Icon Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(categoryBadgeColor.copy(alpha = 0.15f))
                        .border(1.dp, categoryBadgeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = categoryBadgeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = categoryBadgeColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = categoryLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = categoryBadgeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (item.platform.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = item.platform,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        val isFeedOrigin = item.subtitle.contains("Feed", ignoreCase = true) ||
                                item.subtitle.contains("Google News", ignoreCase = true) ||
                                item.subtitle.contains("HackerNews", ignoreCase = true) ||
                                item.subtitle.contains("Reddit", ignoreCase = true) ||
                                item.subtitle.contains("undian", ignoreCase = true) ||
                                item.url.contains("news.google") ||
                                item.url.contains("ycombinator")

                        if (isFeedOrigin) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RssFeed,
                                        contentDescription = null,
                                        modifier = Modifier.size(10.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "LIVE FEED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (item.subtitle.isNotBlank()) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Favorite & Delete Actions
                Row {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (item.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.isCustom) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpen,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = categoryBadgeColor)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    val btnLabel = if (item.category == "MALAY_KARAOKE") "Buka Lagu" else "Buka App"
                    Text(btnLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onAddAsHabit,
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+ Jadi Tabiat", fontSize = 11.sp, maxLines = 1)
                }

                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(38.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomTrendingLinkDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, sub: String, url: String, category: String, platform: String, packageName: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var packageName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("MALAY_KARAOKE") }
    var platform by remember { mutableStateOf("YOUTUBE") }

    val categoryOptions = listOf(
        "MALAY_KARAOKE" to "🎤 Karaoke Melayu",
        "AI" to "🤖 AI Apps",
        "SOCIAL" to "📱 Social Media",
        "UTILITY" to "🛠 Utility Apps"
    )

    val platformOptions = listOf("YOUTUBE", "SMULE", "PLAY_STORE", "WEB")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Tambah Pautan Trending",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kumpul link video karaoke, Smule, aplikasi AI, atau utility kegemaran anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tajuk (Cth: Bunga Angkasa / ChatGPT)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Penerangan Ringkas (Artis / Fungsi)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Pautan URL (YouTube, Smule, atau Web)") },
                    placeholder = { Text("https://...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                Text("Kategori:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categoryOptions) { (key, label) ->
                        FilterChip(
                            selected = category == key,
                            onClick = {
                                category = key
                                platform = if (key == "MALAY_KARAOKE") "YOUTUBE" else if (key == "AI" || key == "SOCIAL") "PLAY_STORE" else "WEB"
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                // Platform selector
                Text("Platform:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(platformOptions) { plat ->
                        FilterChip(
                            selected = platform == plat,
                            onClick = { platform = plat },
                            label = { Text(plat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = packageName,
                    onValueChange = { packageName = it },
                    label = { Text("Package Name Android (Pilihan)") },
                    placeholder = { Text("Cth: com.google.android.youtube") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onAdd(title, subtitle, url, category, platform, packageName)
                        },
                        enabled = title.isNotBlank() && url.isNotBlank()
                    ) {
                        Text("Simpan Pautan")
                    }
                }
            }
        }
    }
}

@Composable
fun LiveFeedNetworkCard(
    isRefreshing: Boolean,
    statusMessage: String?,
    lastSyncTime: Long,
    selectedCategory: String,
    onRefreshAll: () -> Unit,
    onRefreshCategory: () -> Unit
) {
    val timeFormatted = remember(lastSyncTime) {
        val diffSeconds = (System.currentTimeMillis() - lastSyncTime) / 1000
        when {
            diffSeconds < 60 -> "Sebentar tadi"
            diffSeconds < 3600 -> "${diffSeconds / 60} minit lalu"
            else -> "${diffSeconds / 3600} jam lalu"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RssFeed,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Suapan Web Langsung (RSS & API)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF10A37F).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "ONLINE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10A37F),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "Kemaskini: $timeFormatted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isRefreshing) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Menghubungi suapan Google News, Reddit, YouTube & Algolia...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (!statusMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Badges of sources
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FeedSourceBadge(icon = "🤖", label = "AI Feed", color = Color(0xFF10A37F))
                FeedSourceBadge(icon = "📱", label = "Social RSS", color = Color(0xFF0284C7))
                FeedSourceBadge(icon = "🛠", label = "Utility RSS", color = Color(0xFF7C3AED))
                FeedSourceBadge(icon = "🎤", label = "Karaoke RSS", color = Color(0xFFE91E63))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRefreshAll,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isRefreshing
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Segarkan Suapan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (selectedCategory != "ALL") {
                    OutlinedButton(
                        onClick = onRefreshCategory,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isRefreshing
                    ) {
                        Text(text = "Segarkan Kategori", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedSourceBadge(
    icon: String,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

