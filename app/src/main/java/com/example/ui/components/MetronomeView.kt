package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metronome.MetronomeEngine
import com.example.metronome.TimeSignature
import com.example.ui.theme.LocalVirtuosoColors
import com.example.util.localization.LocalVirtuosoStrings

@Composable
fun MetronomeView(
    metronomeEngine: MetronomeEngine,
    modifier: Modifier = Modifier
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current
    val state by metronomeEngine.state.collectAsState()

    val tempoName = getTempoMarking(state.bpm)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("metronome_card"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Title & Audio/Haptic Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (state.isPlaying) colors.primary else colors.textMuted)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.metronomeTitle.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = colors.primaryLight
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { metronomeEngine.toggleAudio() },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("metronome_audio_toggle")
                    ) {
                        Icon(
                            imageVector = if (state.isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Toggle Audio Click",
                            tint = if (state.isAudioEnabled) colors.primary else colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { metronomeEngine.toggleHaptic() },
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("metronome_haptic_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Toggle Haptic Vibrate",
                            tint = if (state.isHapticEnabled) colors.primary else colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visual Beat Indicator Lights
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (beat in 1..state.timeSignature.beatsPerBar) {
                    val isCurrent = state.isPlaying && state.currentBeat == beat
                    val isAccent = beat == 1

                    val targetColor = when {
                        isCurrent && isAccent -> colors.primary
                        isCurrent -> colors.accent
                        isAccent -> colors.accent.copy(alpha = 0.5f)
                        else -> colors.border
                    }

                    val color by animateColorAsState(
                        targetValue = targetColor,
                        animationSpec = tween(durationMillis = 80),
                        label = "beatColor"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isCurrent) 1.35f else 1f,
                        animationSpec = tween(durationMillis = 90),
                        label = "beatScale"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .scale(scale)
                            .size(if (isAccent) 20.dp else 16.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = 1.dp,
                                color = if (isAccent) colors.primaryLight else colors.border,
                                shape = CircleShape
                            )
                    )
                }
            }

            // BPM & Classical Tempo Name Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${state.bpm}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = colors.primary
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.metronomeBpm,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = tempoName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = colors.primaryLight.copy(alpha = 0.85f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // BPM Adjust Controls (-5, -1, slider, +1, +5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { metronomeEngine.changeBpmBy(-5) },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("metronome_minus_5"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceVariant),
                        shape = CircleShape,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("-5", fontSize = 12.sp, color = colors.textPrimary)
                    }
                    Button(
                        onClick = { metronomeEngine.changeBpmBy(-1) },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("metronome_minus_1"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceVariant),
                        shape = CircleShape,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "-1", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }

                // Smooth BPM Slider
                Slider(
                    value = state.bpm.toFloat(),
                    onValueChange = { metronomeEngine.setBpm(it.toInt()) },
                    valueRange = 30f..260f,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("metronome_bpm_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = colors.primary,
                        activeTrackColor = colors.primary,
                        inactiveTrackColor = colors.surfaceVariant
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = { metronomeEngine.changeBpmBy(1) },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("metronome_plus_1"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceVariant),
                        shape = CircleShape,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "+1", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                    Button(
                        onClick = { metronomeEngine.changeBpmBy(5) },
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("metronome_plus_5"),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.surfaceVariant),
                        shape = CircleShape,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text("+5", fontSize = 12.sp, color = colors.textPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Signature Chips (2/4, 3/4, 4/4, 6/8, 5/4)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeSignature.entries.forEach { ts ->
                    val isSelected = state.timeSignature == ts
                    FilterChip(
                        selected = isSelected,
                        onClick = { metronomeEngine.setTimeSignature(ts) },
                        label = {
                            Text(
                                text = ts.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = colors.background,
                            containerColor = colors.surfaceVariant,
                            labelColor = colors.textSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) colors.primary else colors.border
                        ),
                        modifier = Modifier.testTag("meter_${ts.label}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Actions: Tap Tempo & Play/Pause
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { metronomeEngine.tapTempo() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("metronome_tap_tempo"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surfaceVariant,
                        contentColor = colors.primary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.metronomeTapTempo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Button(
                    onClick = { metronomeEngine.togglePlay() },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(48.dp)
                        .testTag("metronome_play_toggle"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isPlaying) colors.accent else colors.primary,
                        contentColor = colors.background
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause Metronome" else "Start Metronome",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isPlaying) strings.metronomeStop else strings.metronomeStart,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

private fun getTempoMarking(bpm: Int): String {
    return when {
        bpm < 40 -> "Grave • Тяжело, торжественно (Very slow & solemn)"
        bpm in 40..45 -> "Largo • Широко, протяжно (Broadly)"
        bpm in 46..55 -> "Lento • Медленно (Slow)"
        bpm in 56..65 -> "Adagio • Спокойно, певуче (Slow & stately)"
        bpm in 66..76 -> "Andante • В темпе шага (Walking pace)"
        bpm in 77..92 -> "Andantino • Чуть подвижнее анданте"
        bpm in 93..108 -> "Moderato • Умеренно (Moderate)"
        bpm in 109..120 -> "Allegretto • Оживленно (Moderately fast)"
        bpm in 121..144 -> "Allegro • Быстро, весело (Fast & bright)"
        bpm in 145..168 -> "Vivace • Живо, энергично (Lively & fast)"
        bpm in 169..199 -> "Presto • Очень быстро (Very fast)"
        else -> "Prestissimo • Предельно стремительно (Extremely fast)"
    }
}
