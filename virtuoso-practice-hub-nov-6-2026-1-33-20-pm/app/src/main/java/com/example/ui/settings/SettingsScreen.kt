package com.example.ui.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.settings.AppLanguage
import com.example.data.settings.AppTheme
import com.example.ui.theme.LocalVirtuosoColors
import com.example.util.localization.LocalVirtuosoStrings

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current
    val settings by viewModel.settings.collectAsState()

    var showResetConfirm by remember { mutableStateOf(false) }
    var instrumentMenuOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Screen Header
                SettingsHeaderCard()
            }

            // 1. UI THEMES SECTION
            item {
                ThemeSelectionSection(
                    currentTheme = settings.theme,
                    onThemeSelected = { viewModel.setTheme(it) }
                )
            }

            // 2. LANGUAGE SWITCHER SECTION
            item {
                LanguageSelectionSection(
                    currentLanguage = settings.language,
                    onLanguageSelected = { viewModel.setLanguage(it) }
                )
            }

            // 3. INSTRUMENT & TUNING STANDARD
            item {
                InstrumentAndTuningSection(
                    selectedInstrument = settings.defaultInstrument,
                    availableInstruments = viewModel.availableInstruments,
                    isInstrumentMenuOpen = instrumentMenuOpen,
                    onToggleInstrumentMenu = { instrumentMenuOpen = !instrumentMenuOpen },
                    onSelectInstrument = {
                        viewModel.setDefaultInstrument(it)
                        instrumentMenuOpen = false
                    },
                    currentTuningHz = settings.defaultTuningHz,
                    onTuningChanged = { viewModel.setDefaultTuningHz(it) }
                )
            }

            // 4. METRONOME PREFERENCES
            item {
                MetronomePreferencesSection(
                    hapticEnabled = settings.metronomeHaptic,
                    onToggleHaptic = { viewModel.setMetronomeHaptic(it) },
                    volume = settings.metronomeVolume,
                    onVolumeChange = { viewModel.setMetronomeVolume(it) }
                )
            }

            // 5. OFFLINE MASTERWORKS & HYBRID AI PROXY CARD
            item {
                OfflineArchitectureCard()
            }

            // 6. RESET DEFAULTS & ABOUT
            item {
                ResetAndAboutSection(
                    onResetClick = { showResetConfirm = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        // Reset Confirmation Dialog
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                title = {
                    Text(
                        text = strings.resetDefaultsConfirmTitle,
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                text = {
                    Text(
                        text = strings.resetDefaultsConfirmDesc,
                        color = colors.textPrimary,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetToDefaults()
                            showResetConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.background
                        )
                    ) {
                        Text(strings.confirm, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirm = false }) {
                        Text(strings.cancel, color = colors.textSecondary)
                    }
                },
                containerColor = colors.surface
            )
        }
    }
}

@Composable
private fun SettingsHeaderCard() {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_header_card"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = colors.primary.copy(alpha = 0.15f),
                shape = CircleShape,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                )
                Text(
                    text = strings.settingsSubtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectionSection(
    currentTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_theme_section"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ColorLens, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.appearanceSection,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Theme Options Cards
            val themes = listOf(
                Triple(AppTheme.CONCERT_DARK, strings.themeConcertDark, Color(0xFF101012) to Color(0xFFD4AF37)),
                Triple(AppTheme.STUDIO_LIGHT, strings.themeStudioLight, Color(0xFFF8F6F0) to Color(0xFFB8860B)),
                Triple(AppTheme.NEON_BRASS, strings.themeNeonBrass, Color(0xFF0A0A0C) to Color(0xFFFF8C00))
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                themes.forEach { (theme, label, palette) ->
                    val isSelected = currentTheme == theme
                    val (bgCol, accentCol) = palette

                    Surface(
                        color = if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("theme_option_${theme.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Theme Mini Color Swatch
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(bgCol)
                                        .border(2.dp, accentCol, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(accentCol)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.primary else colors.textPrimary
                                    )
                                )
                            }

                            if (isSelected) {
                                Surface(
                                    color = colors.primary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            tint = colors.background,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguageSelectionSection(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_language_section"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.languageSection,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppLanguage.entries.forEach { lang ->
                    val isSelected = currentLanguage == lang
                    FilterChip(
                        selected = isSelected,
                        onClick = { onLanguageSelected(lang) },
                        label = {
                            Text(
                                text = "${lang.nativeName} (${lang.code.uppercase()})",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                        modifier = Modifier
                            .weight(1f)
                            .testTag("lang_chip_${lang.code}")
                    )
                }
            }
        }
    }
}

@Composable
private fun InstrumentAndTuningSection(
    selectedInstrument: String,
    availableInstruments: List<String>,
    isInstrumentMenuOpen: Boolean,
    onToggleInstrumentMenu: () -> Unit,
    onSelectInstrument: (String) -> Unit,
    currentTuningHz: Int,
    onTuningChanged: (Int) -> Unit
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_instrument_section"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.defaultInstrumentSection,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Instrument Dropdown Button
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    color = colors.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleInstrumentMenu() }
                        .testTag("select_instrument_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedInstrument,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Icon(Icons.Default.Tune, contentDescription = null, tint = colors.primary)
                    }
                }

                DropdownMenu(
                    expanded = isInstrumentMenuOpen,
                    onDismissRequest = onToggleInstrumentMenu,
                    modifier = Modifier.background(colors.surface)
                ) {
                    availableInstruments.forEach { inst ->
                        DropdownMenuItem(
                            text = { Text(inst, color = colors.textPrimary) },
                            onClick = { onSelectInstrument(inst) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tuning Standard Calibration (440 vs 442)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = strings.tuningStandardSection,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "A4 = $currentTuningHz Hz",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(440, 442).forEach { hz ->
                        val isSelected = currentTuningHz == hz
                        FilterChip(
                            selected = isSelected,
                            onClick = { onTuningChanged(hz) },
                            label = { Text("$hz Hz", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetronomePreferencesSection(
    hapticEnabled: Boolean,
    onToggleHaptic: (Boolean) -> Unit,
    volume: Float,
    onVolumeChange: (Float) -> Unit
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_metronome_section"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Speed, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.metronomePreferencesSection,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Haptic Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Vibration, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.metronomeHaptic, color = colors.textPrimary, fontSize = 14.sp)
                }

                Switch(
                    checked = hapticEnabled,
                    onCheckedChange = onToggleHaptic,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.background,
                        checkedTrackColor = colors.primary,
                        uncheckedThumbColor = colors.textMuted,
                        uncheckedTrackColor = colors.surfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Volume Slider
            Text(
                text = "Громкость щелчка / Volume: ${(volume * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
            )
            Slider(
                value = volume,
                onValueChange = onVolumeChange,
                valueRange = 0.1f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = colors.primary,
                    activeTrackColor = colors.primary,
                    inactiveTrackColor = colors.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun OfflineArchitectureCard() {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_offline_card"),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(18.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.offlineDatabaseSection,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.accent,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.offlineDatabaseDesc,
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary, lineHeight = 18.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = colors.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hybrid AI Proxy • 0ms Local Presets • 100% Offline Resilient",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ResetAndAboutSection(
    onResetClick: () -> Unit
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onResetClick,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(colors.error.copy(alpha = 0.5f))),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("reset_defaults_button")
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.resetDefaultsButton, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = strings.appVersionLabel,
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted, fontSize = 12.sp)
            )
        }
    }
}
