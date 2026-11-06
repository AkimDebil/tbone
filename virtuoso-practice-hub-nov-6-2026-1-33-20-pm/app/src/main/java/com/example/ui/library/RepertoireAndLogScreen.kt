package com.example.ui.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PracticeLogEntity
import com.example.data.entity.SavedRepertoireEntity
import com.example.data.model.RepertoireAnalysis
import com.example.data.repository.PracticeStats
import com.example.ui.theme.LocalVirtuosoColors
import com.example.util.localization.LocalVirtuosoStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RepertoireAndLogScreen(
    viewModel: RepertoireAndLogViewModel,
    onNavigateToGuideAnalysis: (RepertoireAnalysis) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Репертуар, 1: Статистика и дневник

    val statusFilter by viewModel.statusFilter.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    val repertoireList by viewModel.filteredRepertoire.collectAsState()
    val practiceStats by viewModel.practiceStats.collectAsState()
    val practiceLogs by viewModel.practiceLogs.collectAsState()

    var showManualLogDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(8.dp))

            // Dual Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.background,
                contentColor = colors.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = strings.libraryTitle,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 15.sp
                        )
                    },
                    icon = {
                        Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = strings.statsAndLogTitle,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 15.sp
                        )
                    },
                    icon = {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Repertoire Library Content
                    RepertoireLibraryTab(
                        repertoireList = repertoireList,
                        statusFilter = statusFilter,
                        searchFilter = searchFilter,
                        onStatusFilterSelect = { viewModel.setStatusFilter(it) },
                        onSearchChange = { viewModel.setSearchFilter(it) },
                        onStudyGuide = { item ->
                            val analysis = viewModel.parsePieceToAnalysis(item)
                            onNavigateToGuideAnalysis(analysis)
                        },
                        onListen = { item ->
                            val analysis = viewModel.parsePieceToAnalysis(item)
                            openLink(context, analysis.getGeneralYouTubeSearchUrl())
                        },
                        onChangeStatus = { id, newStatus -> viewModel.updatePieceStatus(id, newStatus) },
                        onDelete = { viewModel.deletePiece(it) }
                    )
                }

                1 -> {
                    // Practice Stats & History Content
                    PracticeStatsTab(
                        stats = practiceStats,
                        logs = practiceLogs,
                        onLogManualSessionClick = { showManualLogDialog = true }
                    )
                }
            }
        }

        // Manual Session Log Dialog
        if (showManualLogDialog) {
            ManualLogSessionDialog(
                onDismiss = { showManualLogDialog = false },
                onConfirm = { minutes, title, notes ->
                    viewModel.logManualSession(minutes, title, notes)
                    showManualLogDialog = false
                    Toast.makeText(context, "Session saved!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun RepertoireLibraryTab(
    repertoireList: List<SavedRepertoireEntity>,
    statusFilter: String,
    searchFilter: String,
    onStatusFilterSelect: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onStudyGuide: (SavedRepertoireEntity) -> Unit,
    onListen: (SavedRepertoireEntity) -> Unit,
    onChangeStatus: (Long, String) -> Unit,
    onDelete: (SavedRepertoireEntity) -> Unit
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current
    val filters = listOf(strings.filterAll, strings.filterLearning, strings.filterPolishing, strings.filterMastered)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search & Filter Row
        item {
            OutlinedTextField(
                value = searchFilter,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("repertoire_library_search"),
                placeholder = { Text(strings.searchPlaceholder, fontSize = 13.sp, color = colors.textMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.primary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    cursorColor = colors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { f ->
                    val isSelected = statusFilter == f
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusFilterSelect(f) },
                        label = { Text(f, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = colors.background,
                            containerColor = colors.surface,
                            labelColor = colors.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) colors.primary else colors.border
                        )
                    )
                }
            }
        }

        if (repertoireList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = colors.accent, modifier = Modifier.size(44.dp))
                        Text(
                            text = strings.emptyRepertoireTitle,
                            style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = strings.emptyRepertoireDesc,
                            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(repertoireList) { piece ->
                SavedPieceCard(
                    piece = piece,
                    onStudyGuide = { onStudyGuide(piece) },
                    onListen = { onListen(piece) },
                    onChangeStatus = { newStatus -> onChangeStatus(piece.id, newStatus) },
                    onDelete = { onDelete(piece) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun SavedPieceCard(
    piece: SavedRepertoireEntity,
    onStudyGuide: () -> Unit,
    onListen: () -> Unit,
    onChangeStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current
    var isMenuOpen by remember { mutableStateOf(false) }

    val statusBadgeColor = when (piece.status) {
        "Mastered", "Выучено (Мастер)", "已经掌握" -> colors.success
        "Polishing", "Шлифовка", "精细打磨" -> colors.accent
        else -> colors.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_piece_${piece.id}"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Status Chip & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusBadgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(statusBadgeColor))
                ) {
                    Text(
                        text = piece.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusBadgeColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box {
                    IconButton(onClick = { isMenuOpen = !isMenuOpen }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = colors.textSecondary)
                    }

                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false },
                        modifier = Modifier.background(colors.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("${strings.changeStatusLabel} ${strings.filterLearning}", color = colors.textPrimary) },
                            onClick = {
                                onChangeStatus(strings.filterLearning)
                                isMenuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("${strings.changeStatusLabel} ${strings.filterPolishing}", color = colors.textPrimary) },
                            onClick = {
                                onChangeStatus(strings.filterPolishing)
                                isMenuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("${strings.changeStatusLabel} ${strings.filterMastered}", color = colors.textPrimary) },
                            onClick = {
                                onChangeStatus(strings.filterMastered)
                                isMenuOpen = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.deletePieceLabel, color = colors.error) },
                            onClick = {
                                onDelete()
                                isMenuOpen = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Composer
            Text(
                text = piece.composer,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = colors.primaryLight,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = piece.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            )

            Text(
                text = "${strings.instrumentLabel}: ${piece.instrument}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textSecondary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            )

            if (piece.userNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = colors.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = piece.userNotes,
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onStudyGuide,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("study_guide_${piece.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.background
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.studyGuideButton, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onListen,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("listen_saved_${piece.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surfaceVariant,
                        contentColor = colors.primaryLight
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(strings.listenButton, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PracticeStatsTab(
    stats: PracticeStats,
    logs: List<PracticeLogEntity>,
    onLogManualSessionClick: () -> Unit
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Statistics Cards Row
        item {
            StatsSummarySection(stats = stats)
        }

        // Action: Log Manual Session
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.sessionHistoryTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.primaryLight,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Button(
                    onClick = onLogManualSessionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.background),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp).testTag("log_practice_session_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.logSessionButton, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.EventNote, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(36.dp))
                        Text(strings.emptyRepertoireTitle, style = MaterialTheme.typography.titleSmall.copy(color = colors.textPrimary))
                    }
                }
            }
        } else {
            items(logs) { log ->
                PracticeLogItemCard(log = log)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StatsSummarySection(stats: PracticeStats) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("practice_stats_card"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = strings.performanceSummary,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Grid KPI metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Minutes This Week
                Surface(
                    color = colors.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.thisWeekMinutes, style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${stats.totalMinutesThisWeek}m",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = colors.textPrimary
                            )
                        )
                    }
                }

                // Metric 2: Streak
                Surface(
                    color = colors.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = colors.accent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.streakDays, style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${stats.streakDays}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = colors.accent
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 3: Total Sessions
                Surface(
                    color = colors.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(strings.totalSessions, style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${stats.totalSessions}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = colors.textPrimary
                            )
                        )
                    }
                }

                // Metric 4: Repertoire In Progress
                Surface(
                    color = colors.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(strings.inProgressCount, style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${stats.learningCount + stats.polishingCount}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = colors.primaryLight
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeLogItemCard(log: PracticeLogEntity) {
    val colors = LocalVirtuosoColors.current
    val dateFormatted = remember(log.timestamp) {
        SimpleDateFormat("d MMMM yyyy • HH:mm", Locale.getDefault()).format(Date(log.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = log.routineTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
                    )
                }

                Surface(
                    color = colors.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.primary))
                ) {
                    Text(
                        text = "${log.durationMinutes} min",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primaryLight,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            if (log.sessionNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = log.sessionNotes,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ManualLogSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (duration: Int, title: String, notes: String) -> Unit
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    var title by remember { mutableStateOf("Practice Session") }
    var durationStr by remember { mutableStateOf("45") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(strings.logDialogTitle, fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = colors.primaryLight)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(strings.logSessionTitleField) },
                    modifier = Modifier.fillMaxWidth().testTag("manual_log_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        cursorColor = colors.primary
                    )
                )

                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { durationStr = it.filter { c -> c.isDigit() } },
                    label = { Text(strings.logDurationField) },
                    modifier = Modifier.fillMaxWidth().testTag("manual_log_duration_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        cursorColor = colors.primary
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.logNotesField) },
                    modifier = Modifier.fillMaxWidth().testTag("manual_log_notes_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        cursorColor = colors.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationStr.toIntOrNull() ?: 45
                    onConfirm(duration, title, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.background),
                modifier = Modifier.testTag("confirm_manual_log_button")
            ) {
                Text(strings.saveConfirm, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel, color = colors.textSecondary)
            }
        },
        containerColor = colors.surface
    )
}

private fun openLink(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
