package com.example.ui.trainer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.trainer.ear.EarTrainerEngine
import com.example.trainer.tone.DroneSynthesizer
import com.example.trainer.tone.PitchDetector
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
import java.util.Locale
import kotlin.math.abs

@Composable
fun ToneTrainerView(
    droneSynthesizer: DroneSynthesizer,
    pitchDetector: PitchDetector,
    earTrainer: EarTrainerEngine,
    modifier: Modifier = Modifier
) {
    var selectedSubSection by remember { mutableIntStateOf(0) } // 0: Бурдон, 1: Тюнер/Ровность, 2: Слух

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf(
                "Бурдон (Drone)",
                "Тюнер и ровность",
                "Тренажёр слуха"
            )

            tabs.forEachIndexed { index, title ->
                val isSelected = selectedSubSection == index
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSubSection = index },
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

        when (selectedSubSection) {
            0 -> DroneSynthesizerCard(drone = droneSynthesizer)
            1 -> TunerStabilityCard(detector = pitchDetector)
            2 -> EarTrainerCard(earTrainer = earTrainer)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DroneSynthesizerCard(drone: DroneSynthesizer) {
    val state by drone.state.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("drone_card"),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "БУРДОН ДЛЯ СТРОЯ И ДЛИННЫХ ТОНОВ",
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Text(
                        text = "Органный / виолончельный акустический тон",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Surface(
                    color = CharcoalSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f Гц", state.currentFrequency),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldLight, fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Note Selection Chips
            Text("Опорная нота:", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                drone.noteNames.forEach { note ->
                    val isSelected = state.noteName == note
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) GoldPrimary else CharcoalDarkest,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) GoldPrimary else CharcoalBorder)
                        ),
                        modifier = Modifier
                            .clickable { drone.selectNote(note) }
                            .testTag("drone_note_$note")
                    ) {
                        Text(
                            text = note,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CharcoalDarkest else TextPrimary
                            )
                        )
                    }
                }
            }

            // Octave & Tuning Pitch Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Octave selection
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Октава: ", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                    drone.octaves.forEach { oct ->
                        val isSelected = state.octave == oct
                        Text(
                            text = "$oct",
                            modifier = Modifier
                                .clickable { drone.selectOctave(oct) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GoldPrimary else TextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }

                // A4 frequency standard (440 vs 442)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf(440.0, 442.0).forEach { base ->
                        val isSelected = state.baseA4Freq == base
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) GoldPrimary else CharcoalSurfaceVariant,
                            modifier = Modifier
                                .clickable { drone.setBaseA4(base) }
                                .padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "${base.toInt()}Hz",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CharcoalDarkest else TextSecondary
                            )
                        }
                    }
                }
            }

            // Pure Fifth (Чистая квинта) Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Чистая квинта (Pure Fifth)", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold))
                    Text("Гармонический резонанс 3:2 над примой", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                }

                Switch(
                    checked = state.isPureFifthEnabled,
                    onCheckedChange = { drone.togglePureFifth() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CharcoalDarkest,
                        checkedTrackColor = GoldPrimary,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CharcoalSurfaceVariant
                    )
                )
            }

            // Play / Stop Button
            Button(
                onClick = { drone.togglePlay() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("toggle_drone_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isPlaying) AmberWood else GoldPrimary,
                    contentColor = CharcoalDarkest
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (state.isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isPlaying) "ВЫКЛЮЧИТЬ БУРДОН" else "ВКЛЮЧИТЬ БУРДОН (${state.noteName}${state.octave})",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun TunerStabilityCard(detector: PitchDetector) {
    val context = LocalContext.current
    val state by detector.state.collectAsState()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        detector.start(granted)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tuner_stability_card"),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ХРОМАТИЧЕСКИЙ ТЮНЕР И РОВНОСТЬ ТОНА",
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    )
                    Text(
                        text = if (state.isDemoMode) "Демо-симуляция ровности выдоха" else "Слушает микрофон устройства",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Surface(
                    onClick = { detector.startDemoSimulation() },
                    shape = RoundedCornerShape(8.dp),
                    color = CharcoalSurfaceVariant
                ) {
                    Text(
                        text = "Демо-тест",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldLight, fontSize = 11.sp)
                    )
                }
            }

            // Big Note & Cents Meter Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (state.isSoundDetected) state.noteName else "--",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = if (abs(state.centsOffset) <= 5) SuccessGreen else GoldPrimary
                    )
                )
                if (state.isSoundDetected) {
                    Text(
                        text = "${state.octave}",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
            }

            Text(
                text = if (state.isSoundDetected)
                    String.format(Locale.getDefault(), "%+.1f центов (%.1f Гц)", state.centsOffset, state.detectedFrequencyHz)
                else "Сыграйте ноту...",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (abs(state.centsOffset) <= 5) SuccessGreen else GoldLight
                )
            )

            // Needle Deviation Gauge (-50 to +50 cents)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
            ) {
                val midX = size.width / 2f
                val y = size.height / 2f

                // Horizontal track line
                drawLine(
                    color = Color(0xFF383842),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 3f
                )

                // Center in-tune tick (0 cents)
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(midX, y - 10f),
                    end = Offset(midX, y + 10f),
                    strokeWidth = 4f
                )

                // Target indicator needle
                if (state.isSoundDetected) {
                    val fraction = (state.centsOffset / 50f).coerceIn(-1f, 1f)
                    val needleX = midX + fraction * (size.width / 2f)

                    drawCircle(
                        color = if (abs(state.centsOffset) <= 5) Color(0xFF4CAF50) else Color(0xFFD4AF37),
                        radius = 8f,
                        center = Offset(needleX, y)
                    )
                }
            }

            // Real-time Scrolling Pitch Stability Graph
            Text(
                text = "ГРАФИК СТАБИЛЬНОСТИ АМБУШЮРА (ПОСЛЕДНИЕ 10 СЕКУНД):",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp, letterSpacing = 0.5.sp)
            )

            PitchStabilityCanvas(points = state.recentPitchHistory)

            // Stability Rating Badge
            Surface(
                color = CharcoalSurfaceVariant,
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Стабильность: ${state.stabilityScorePercent}%",
                        style = MaterialTheme.typography.titleSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = state.stabilityFeedback,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            // Mic Toggle Button
            Button(
                onClick = {
                    if (state.isListening) {
                        detector.stop()
                    } else {
                        if (hasPermission) {
                            detector.start(true)
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("toggle_mic_tuner_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isListening) AmberWood else GoldPrimary,
                    contentColor = CharcoalDarkest
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (state.isListening) Icons.Default.MicOff else Icons.Default.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isListening) "ОСТАНОВИТЬ ТЮНЕР" else "ВКЛЮЧИТЬ МИКРОФОН",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PitchStabilityCanvas(points: List<com.example.trainer.tone.PitchPoint>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(95.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalDarkest),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp)) {
            val midY = size.height / 2f

            // Zero cents center line
            drawLine(
                color = Color(0xFF4CAF50).copy(alpha = 0.5f),
                start = Offset(0f, midY),
                end = Offset(size.width, midY),
                strokeWidth = 2f
            )

            // ±15 cents tolerance lines
            val tol15Y = (15f / 50f) * (size.height / 2f)
            drawLine(
                color = Color(0xFF383842),
                start = Offset(0f, midY - tol15Y),
                end = Offset(size.width, midY - tol15Y),
                strokeWidth = 1f
            )
            drawLine(
                color = Color(0xFF383842),
                start = Offset(0f, midY + tol15Y),
                end = Offset(size.width, midY + tol15Y),
                strokeWidth = 1f
            )

            // Plot history points
            if (points.size >= 2) {
                val path = Path()
                val stepX = size.width / (points.size - 1).coerceAtLeast(1)

                points.forEachIndexed { i, pt ->
                    val x = i * stepX
                    val yFraction = (-pt.centsOffset / 50f).coerceIn(-1f, 1f)
                    val y = midY + yFraction * (size.height / 2f)

                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = Color(0xFFD4AF37),
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}

@Composable
private fun EarTrainerCard(earTrainer: EarTrainerEngine) {
    val state by earTrainer.state.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ear_trainer_card"),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header & Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ТРЕНАЖЁР СЛУХА: ИНТЕРВАЛЫ",
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Text(
                        text = "Определите интервал на слух",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Surface(
                    color = CharcoalSurfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Серия: ${state.scoreStreak} 🔥 (${state.totalCorrect}/${state.totalAnswered})",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldLight, fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Audio Replay Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { earTrainer.playInterval(isHarmonic = false) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceVariant, contentColor = GoldLight),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Мелодически", fontSize = 12.sp)
                }

                Button(
                    onClick = { earTrainer.playInterval(isHarmonic = true) },
                    modifier = Modifier.weight(1f).height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceVariant, contentColor = GoldLight),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Гармонически", fontSize = 12.sp)
                }
            }

            // 4 Answer Candidate Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.candidateIntervals.chunked(2).forEach { rowIntervals ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowIntervals.forEach { interval ->
                            val isSelected = state.selectedInterval == interval
                            val isTarget = interval == state.currentInterval
                            val isChecked = state.isAnswerChecked

                            val buttonColor = when {
                                isChecked && isTarget -> SuccessGreen
                                isChecked && isSelected && !state.isCorrect -> MaterialTheme.colorScheme.error
                                else -> CharcoalSurfaceVariant
                            }

                            Button(
                                onClick = { earTrainer.submitAnswer(interval) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = buttonColor,
                                    contentColor = if (isChecked && (isTarget || isSelected)) CharcoalDarkest else TextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(interval.nameRu, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(interval.shortCode, fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            // Explanation / Next Question
            if (state.isAnswerChecked) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (state.isCorrect) "Верно! ${state.currentInterval.nameRu}" else "Ошибка. Это был интервал: ${state.currentInterval.nameRu}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = if (state.isCorrect) SuccessGreen else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Ассоциация: ${state.currentInterval.mnemonicExample}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { earTrainer.generateNewQuestion() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalDarkest),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Следующий интервал", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
