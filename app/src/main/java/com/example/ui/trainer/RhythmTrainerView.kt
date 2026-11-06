package com.example.ui.trainer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainer.rhythm.RhythmNote
import com.example.trainer.rhythm.RhythmTrainerEngine
import com.example.trainer.rhythm.RhythmType
import com.example.trainer.rhythm.TapFeedback
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
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RhythmTrainerView(
    engine: RhythmTrainerEngine,
    modifier: Modifier = Modifier
) {
    val state by engine.state.collectAsState()

    val patterns = listOf(
        "Синкопы и восьмые",
        "Четверти и восьмые (Базовый)",
        "Триоли (Triplets)",
        "Пунктирный ритм и шестнадцатые",
        "Двухтактовый этюд (2 такта)"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Selector: Patterns vs Polyrhythm vs Silent Bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val modes = listOf(
                RhythmType.PATTERN_PRACTICE to "Паттерны",
                RhythmType.POLYRHYTHM to "Полиритмия",
                RhythmType.SILENT_BARS to "Скрытые такты"
            )

            modes.forEach { (type, title) ->
                val isSelected = state.type == type
                FilterChip(
                    selected = isSelected,
                    onClick = { engine.setRhythmType(type) },
                    label = { Text(title, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = CharcoalDarkest,
                        containerColor = CharcoalSurface,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) GoldPrimary else CharcoalBorder
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Tempo & Time Signature Controller
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AvTimer, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ТЕМП И РАЗМЕР",
                            style = MaterialTheme.typography.labelSmall.copy(color = GoldLight, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                    }

                    Text(
                        text = "${state.bpm} BPM • ${state.timeSignature}",
                        style = MaterialTheme.typography.titleSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                    )
                }

                Slider(
                    value = state.bpm.toFloat(),
                    onValueChange = { engine.setBpm(it.toInt()) },
                    valueRange = 40f..180f,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldPrimary,
                        activeTrackColor = GoldPrimary,
                        inactiveTrackColor = CharcoalBorder
                    )
                )
            }
        }

        when (state.type) {
            RhythmType.PATTERN_PRACTICE -> {
                // Pattern Selector Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "ВЫБЕРИТЕ РИТМИЧЕСКИЙ РИСУНОК:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(patterns) { p ->
                            val isSelected = state.currentPatternName == p
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) CharcoalSurfaceVariant else CharcoalDarkest,
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) GoldPrimary else CharcoalBorder)
                                ),
                                modifier = Modifier.clickable { engine.loadPattern(p) }
                            ) {
                                Text(
                                    text = p,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (isSelected) GoldLight else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }

                // Visual Pattern Timeline Box
                VisualPatternTimeline(
                    notes = state.notes,
                    totalBeats = state.totalPatternBeats,
                    currentProgress = state.currentBeatProgress,
                    isPlaying = state.isPlaying
                )

                // Interactive Tap Pad
                InteractiveTapPad(
                    state = state,
                    onStart = { engine.startPractice() },
                    onStop = { engine.stop() },
                    onTap = { engine.onUserTap() }
                )
            }

            RhythmType.POLYRHYTHM -> {
                PolyrhythmPracticeView(
                    state = state,
                    onSelectRatio = { a, b -> engine.setPolyrhythmRatio(a, b) },
                    onTogglePlay = {
                        if (state.isPlaying) engine.stop() else engine.startPractice()
                    }
                )
            }

            RhythmType.SILENT_BARS -> {
                SilentBarsPracticeView(
                    state = state,
                    onTogglePlay = {
                        if (state.isPlaying) engine.stop() else engine.startPractice()
                    }
                )
            }
        }
    }
}

@Composable
private fun VisualPatternTimeline(
    notes: List<RhythmNote>,
    totalBeats: Double,
    currentProgress: Double,
    isPlaying: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .testTag("rhythm_timeline_card"),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            // Draw staff line and notes
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineY = size.height * 0.65f

                // Horizontal musical timeline line
                drawLine(
                    color = Color(0xFF383842),
                    start = Offset(0f, lineY),
                    end = Offset(size.width, lineY),
                    strokeWidth = 3f
                )

                // Draw note tick marks
                for (note in notes) {
                    val x = (note.beatOffset / totalBeats.coerceAtLeast(1.0)).toFloat() * size.width
                    drawLine(
                        color = Color(0xFFD4AF37),
                        start = Offset(x, lineY - 14f),
                        end = Offset(x, lineY + 14f),
                        strokeWidth = 4f
                    )
                }

                // Moving playhead cursor if playing
                if (isPlaying) {
                    val playheadX = (currentProgress / totalBeats.coerceAtLeast(1.0)).toFloat() * size.width
                    drawLine(
                        color = Color(0xFFF3E5AB),
                        start = Offset(playheadX, 0f),
                        end = Offset(playheadX, size.height),
                        strokeWidth = 5f
                    )
                }
            }

            // Note symbols row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                notes.forEach { note ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = note.displaySymbol,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                        Text(
                            text = "${note.durationBeats} д.",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InteractiveTapPad(
    state: com.example.trainer.rhythm.RhythmTrainerState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interactive_tap_pad_card"),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Count-in / Status Overlay
            if (state.isCountingIn) {
                Text(
                    text = "ОТСЧЁТ: ${state.countInBeat} из 4...",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "Слушайте метр и приготовьтесь стучать!",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            } else if (state.isPlaying) {
                // Real-time Feedback indicator
                state.lastTapFeedback?.let { fb ->
                    val color = Color(fb.feedback.colorHex)
                    Surface(
                        color = color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(color))
                    ) {
                        Text(
                            text = "${fb.feedback.label} (${if (fb.deltaMs > 0) "+" else ""}${fb.deltaMs} ms)",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = color,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                } ?: run {
                    Text(
                        text = "Стучите по кнопке в ритм нот!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = GoldLight, fontWeight = FontWeight.Bold)
                    )
                }
            } else if (state.finalScore != null) {
                // Final Score Banner
                val score = state.finalScore
                val rating = when {
                    score >= 90 -> "Мастер ритма! Отличный пульс"
                    score >= 75 -> "Хорошо! Уверенная координация"
                    else -> "Требует практики в медленном темпе"
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Точность: $score%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = if (score >= 80) SuccessGreen else GoldPrimary
                        )
                    )
                    Text(
                        text = rating,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            } else {
                Text(
                    text = "Нажмите «СТАРТ ТРЕНИРОВКИ» для отсчёта",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Giant Tap Pad Area
            Surface(
                onClick = onTap,
                enabled = state.isPlaying && !state.isCountingIn,
                shape = RoundedCornerShape(18.dp),
                color = if (state.isPlaying && !state.isCountingIn) GoldPrimary else CharcoalSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .testTag("rhythm_tap_pad")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "Tap Pad",
                        tint = if (state.isPlaying && !state.isCountingIn) CharcoalDarkest else TextMuted,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (state.isPlaying && !state.isCountingIn) "НАЖИМАЙТЕ ЗДЕСЬ В РИТМ!" else "ТАП-ПЛОЩАДКА",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (state.isPlaying && !state.isCountingIn) CharcoalDarkest else TextMuted,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Start / Stop Controller Button
            Button(
                onClick = { if (state.isPlaying) onStop() else onStart() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("toggle_rhythm_trainer_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isPlaying) AmberWood else GoldPrimary,
                    contentColor = CharcoalDarkest
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isPlaying) "ОСТАНОВИТЬ" else "СТАРТ ТРЕНИРОВКИ (4 ДОЛИ ОТСЧЁТА)",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun PolyrhythmPracticeView(
    state: com.example.trainer.rhythm.RhythmTrainerState,
    onSelectRatio: (Int, Int) -> Unit,
    onTogglePlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "ТРЕНИРОВКА ПОЛИРИТМИИ",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            )

            Text(
                text = "Развивает независимость полушарий и полифонический слух. Голос 1 — высокий щелчок, Голос 2 — низкий колокольчик.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Ratio Chips (3:2, 4:3, 5:4)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(3 to 2, 4 to 3, 5 to 4).forEach { (a, b) ->
                    val isSelected = state.polyrhythmRatio == (a to b)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectRatio(a, b) },
                        label = { Text("$a : $b", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = CharcoalDarkest,
                            containerColor = CharcoalSurfaceVariant,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            // Visual Circles for Voice A and Voice B
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Voice A Indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Голос 1 (${state.polyrhythmRatio.first})", style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 1..state.polyrhythmRatio.first) {
                            val isLit = state.isPlaying && state.polyBeatA == i
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isLit) GoldPrimary else CharcoalSurfaceVariant)
                                    .border(1.dp, GoldLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$i", fontSize = 11.sp, color = if (isLit) CharcoalDarkest else TextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Voice B Indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Голос 2 (${state.polyrhythmRatio.second})", style = MaterialTheme.typography.labelSmall.copy(color = AmberWood, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 1..state.polyrhythmRatio.second) {
                            val isLit = state.isPlaying && state.polyBeatB == i
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isLit) AmberWood else CharcoalSurfaceVariant)
                                    .border(1.dp, AmberWood, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$i", fontSize = 11.sp, color = if (isLit) CharcoalDarkest else TextSecondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Play/Pause Button
            Button(
                onClick = onTogglePlay,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalDarkest),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (state.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (state.isPlaying) "ОСТАНОВИТЬ" else "ЗАПУСТИТЬ ПОЛИРИТМИЮ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SilentBarsPracticeView(
    state: com.example.trainer.rhythm.RhythmTrainerState,
    onTogglePlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "РЕЖИМ «СКРЫТЫЕ ТАКТЫ»",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            )

            Text(
                text = "Метроном звучит 2 такта, затем замолкает на 2 такта. Ваша задача — сохранить внутренний пульс и проверить совпадение на возвращении звука!",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Silent indicator badge
            Surface(
                color = if (state.isCurrentlySilent) AmberWood.copy(alpha = 0.2f) else SuccessGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (state.isCurrentlySilent) AmberWood else SuccessGreen)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (state.isCurrentlySilent) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (state.isCurrentlySilent) AmberWood else SuccessGreen
                    )
                    Text(
                        text = if (state.isCurrentlySilent) "ТИШИНА (Держите внутренний пульс!)" else "ЗВУК (Сверяйте метр)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (state.isCurrentlySilent) AmberWood else SuccessGreen
                        )
                    )
                }
            }

            // Play/Pause Button
            Button(
                onClick = onTogglePlay,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalDarkest),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (state.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (state.isPlaying) "ОСТАНОВИТЬ ТЕСТ" else "НАЧАТЬ ТЕСТ ПУЛЬСА", fontWeight = FontWeight.Bold)
            }
        }
    }
}
