package com.example.ui.studio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PracticeRoutineEntity
import com.example.ui.components.MetronomeView
import com.example.ui.theme.AmberWood
import com.example.ui.theme.BrassAccent
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalDarkest
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CharcoalSurfaceVariant
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PracticeStudioScreen(
    viewModel: PracticeStudioViewModel,
    modifier: Modifier = Modifier
) {
    val colors = com.example.ui.theme.LocalVirtuosoColors.current
    val strings = com.example.util.localization.LocalVirtuosoStrings.current

    val routineItems by viewModel.routineItems.collectAsState()
    val activeStage by viewModel.activeStage.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val showCelebration by viewModel.sessionCompletedCelebration.collectAsState()

    var showAddStageDialog by remember { mutableStateOf(false) }
    var isMetronomeExpanded by remember { mutableStateOf(true) }

    val completedCount = routineItems.count { it.isCompleted }
    val totalCount = routineItems.size.coerceAtLeast(1)
    val progressFraction = completedCount.toFloat() / totalCount

    val currentDateStr = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Studio Header Card
                StudioHeader(
                    currentDate = currentDateStr,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    progressFraction = progressFraction,
                    elapsedSeconds = timerState.elapsedSessionSeconds,
                    onResetRoutine = { viewModel.resetRoutine() }
                )
            }

            // Active Stage Hero & Countdown Timer
            item {
                activeStage?.let { stage ->
                    ActiveStageHeroCard(
                        stage = stage,
                        timerState = timerState,
                        onToggleTimer = { viewModel.toggleTimer() },
                        onResetTimer = { viewModel.resetTimer() },
                        onCompleteStage = { viewModel.markStageCompleted(stage.id, !stage.isCompleted) },
                        onNotesChanged = { viewModel.updateStageNotes(stage.id, it) }
                    )
                }
            }

            // Collapsible Metronome Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isMetronomeExpanded = !isMetronomeExpanded }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Метроном",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "КОНЦЕРТНЫЙ МЕТРОНОМ",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                            IconButton(onClick = { isMetronomeExpanded = !isMetronomeExpanded }) {
                                Icon(
                                    imageVector = if (isMetronomeExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Свернуть метроном",
                                    tint = TextSecondary
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isMetronomeExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            MetronomeView(
                                metronomeEngine = viewModel.metronomeEngine,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Routine Stage Checklist Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ПЛАН ЗАНЯТИЯ НА СЕГОДНЯ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            letterSpacing = 1.sp
                        )
                    )
                    Row {
                        TextButton(
                            onClick = { showAddStageDialog = true },
                            modifier = Modifier.testTag("add_stage_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить блок", tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Добавить блок", color = GoldPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Routine Stages List
            itemsIndexed(routineItems) { index, stage ->
                val isActive = activeStage?.id == stage.id
                RoutineStageItemCard(
                    index = index + 1,
                    stage = stage,
                    isActive = isActive,
                    onSelect = { viewModel.selectStage(stage) },
                    onToggleComplete = { viewModel.markStageCompleted(stage.id, !stage.isCompleted) }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { viewModel.restoreDefaultRoutine() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder)),
                        modifier = Modifier.testTag("restore_defaults_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Восстановить 60-мин комплекс", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Восстановить 60-мин классический комплекс", fontSize = 13.sp)
                    }
                }
                Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom nav
            }
        }

        // Add Custom Stage Dialog
        if (showAddStageDialog) {
            AddStageDialog(
                onDismiss = { showAddStageDialog = false },
                onAdd = { title, duration, bpm, timeSig ->
                    viewModel.addCustomStage(title, duration, bpm, timeSig)
                    showAddStageDialog = false
                }
            )
        }

        // Celebration Summary Dialog
        if (showCelebration) {
            CelebrationDialog(
                totalMinutes = timerState.elapsedSessionSeconds / 60,
                onDismiss = { viewModel.dismissCelebration() },
                onLogSession = { notes ->
                    viewModel.savePracticeSessionLog(notes)
                }
            )
        }
    }
}

@Composable
private fun StudioHeader(
    currentDate: String,
    completedCount: Int,
    totalCount: Int,
    progressFraction: Float,
    elapsedSeconds: Int,
    onResetRoutine: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("studio_header_card"),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "СТУДИЯ ПРАКТИКИ VIRTUOSO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                IconButton(
                    onClick = onResetRoutine,
                    modifier = Modifier.testTag("reset_routine_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Сбросить прогресс",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$completedCount из $totalCount этапов пройдено",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = GoldLight,
                        fontWeight = FontWeight.Medium
                    )
                )

                val elapsedMinutes = elapsedSeconds / 60
                val elapsedSecsRemaining = elapsedSeconds % 60
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Прошло времени",
                        tint = BrassAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "Прошло %02d:%02d", elapsedMinutes, elapsedSecsRemaining),
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GoldPrimary,
                trackColor = CharcoalBorder
            )
        }
    }
}

@Composable
private fun ActiveStageHeroCard(
    stage: PracticeRoutineEntity,
    timerState: TimerState,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onCompleteStage: () -> Unit,
    onNotesChanged: (String) -> Unit
) {
    val remainingSecs = timerState.remainingSeconds
    val minutes = remainingSecs / 60
    val seconds = remainingSecs % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    var notesText by remember(stage.id) { mutableStateOf(stage.sessionNotes) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_stage_hero"),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(24.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (timerState.isRunning) GoldPrimary else CharcoalBorder
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stage Type & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CharcoalSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
                ) {
                    Text(
                        text = stage.stageType.replace("_", " "),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = BrassAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stage.targetBpm} BPM • ${stage.timeSignature}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stage Title & Subtitle
            Text(
                text = stage.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            if (stage.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stage.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Giant Countdown Clock
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 56.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = if (timerState.isRunning) GoldPrimary else TextPrimary,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.testTag("countdown_timer_text")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timer Controls: Play/Pause, Reset, Mark Completed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onResetTimer,
                    modifier = Modifier
                        .size(44.dp)
                        .background(CharcoalSurfaceVariant, CircleShape)
                        .testTag("reset_stage_timer_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Stage Timer", tint = TextSecondary)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onToggleTimer,
                    modifier = Modifier
                        .height(52.dp)
                        .width(150.dp)
                        .testTag("toggle_stage_timer_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (timerState.isRunning) AmberWood else GoldPrimary,
                        contentColor = CharcoalDarkest
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timerState.isRunning) "Pause" else "Start",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (timerState.isRunning) "ПАУЗА" else "СТАРТ",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onCompleteStage,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (stage.isCompleted) SuccessGreen.copy(alpha = 0.2f) else CharcoalSurfaceVariant,
                            CircleShape
                        )
                        .testTag("complete_stage_icon_button")
                ) {
                    Icon(
                        imageVector = if (stage.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Отметить завершённым",
                        tint = if (stage.isCompleted) SuccessGreen else GoldLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Session Notes
            OutlinedTextField(
                value = notesText,
                onValueChange = {
                    notesText = it
                    onNotesChanged(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("stage_notes_input"),
                placeholder = {
                    Text("Заметки занятия (такты, цели штриха, темп)...", fontSize = 13.sp, color = TextMuted)
                },
                leadingIcon = {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CharcoalBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = GoldPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun RoutineStageItemCard(
    index: Int,
    stage: PracticeRoutineEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onToggleComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("stage_item_${stage.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) CharcoalSurfaceVariant else CharcoalSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isActive) GoldPrimary else CharcoalBorder
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stage Index Circle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (stage.isCompleted) SuccessGreen.copy(alpha = 0.2f) else CharcoalDarkest)
                    .border(1.dp, if (stage.isCompleted) SuccessGreen else GoldDark, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (stage.isCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = SuccessGreen, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stage.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = if (stage.isCompleted) TextMuted else TextPrimary,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold
                    )
                )
                if (stage.sessionNotes.isNotBlank()) {
                    Text(
                        text = stage.sessionNotes,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = CharcoalDarkest,
                shape = RoundedCornerShape(8.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
            ) {
                Text(
                    text = "${stage.durationMinutes}m",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldLight,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun AddStageDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, duration: Int, bpm: Int, timeSig: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var durationStr by remember { mutableStateOf("15") }
    var bpmStr by remember { mutableStateOf("100") }
    var timeSig by remember { mutableStateOf("4/4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Добавить блок практики", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = GoldLight)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название (напр. Оркестровые трудности)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_stage_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        cursorColor = GoldPrimary
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Время (мин)") },
                        modifier = Modifier.weight(1f).testTag("add_stage_duration_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            cursorColor = GoldPrimary
                        )
                    )

                    OutlinedTextField(
                        value = bpmStr,
                        onValueChange = { bpmStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Темп (BPM)") },
                        modifier = Modifier.weight(1f).testTag("add_stage_bpm_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            cursorColor = GoldPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val duration = durationStr.toIntOrNull() ?: 15
                        val bpm = bpmStr.toIntOrNull() ?: 100
                        onAdd(title.trim(), duration, bpm, timeSig)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalDarkest),
                modifier = Modifier.testTag("confirm_add_stage_button")
            ) {
                Text("Добавить", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        },
        containerColor = CharcoalSurface
    )
}

@Composable
private fun CelebrationDialog(
    totalMinutes: Int,
    onDismiss: () -> Unit,
    onLogSession: (notes: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Celebration,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "Браво! Занятие завершено",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Вы успешно выполнили все этапы сегодняшнего классического плана (~$totalMinutes минут практики). Регулярность рождает виртуозность!",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Итоги и впечатления от занятия") },
                    placeholder = { Text("Напр. Освоена каденция концерта Давида, чистая двойная атака.") },
                    modifier = Modifier.fillMaxWidth().testTag("celebration_notes_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        cursorColor = GoldPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onLogSession(notes) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalDarkest),
                modifier = Modifier.testTag("save_session_log_button")
            ) {
                Text("Зафиксировать в журнале", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Готово", color = TextSecondary)
            }
        },
        containerColor = CharcoalSurface
    )
}
