package com.example.ui.guide

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamousRecording
import com.example.data.model.RecommendedWork
import com.example.data.model.RepertoireAnalysis
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RepertoireGuideScreen(
    viewModel: RepertoireGuideViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedInstrument by viewModel.selectedInstrument.collectAsState()

    val colors = com.example.ui.theme.LocalVirtuosoColors.current
    val strings = com.example.util.localization.LocalVirtuosoStrings.current

    var showSaveDialog by remember { mutableStateOf(false) }
    var isInstrumentMenuOpen by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(colors.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // AI Guide Header
                GuideHeaderCard()
            }

            // Search Bar & Instrument Selector
            item {
                SearchSection(
                    searchQuery = searchQuery,
                    selectedInstrument = selectedInstrument,
                    availableInstruments = viewModel.availableInstruments,
                    isMenuOpen = isInstrumentMenuOpen,
                    onQueryChange = { viewModel.onQueryChanged(it) },
                    onInstrumentSelect = {
                        viewModel.onInstrumentSelected(it)
                        isInstrumentMenuOpen = false
                    },
                    onMenuToggle = { isInstrumentMenuOpen = !isInstrumentMenuOpen },
                    onAnalyze = { viewModel.analyzePiece() },
                    onClear = { viewModel.clearSearch() }
                )
            }

            // Preset Quick-Inspiration Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = strings.quickPresetsTitle.uppercase() + " (0 ms):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.primaryLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(viewModel.presetSuggestions) { (piece, inst) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colors.surface,
                                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(colors.border)),
                                modifier = Modifier
                                    .clickable { viewModel.selectPreset(piece, inst) }
                                    .testTag("preset_${piece.take(10)}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "$piece ($inst)",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic Content based on State
            when (val state = uiState) {
                is GuideUiState.Idle -> {
                    item {
                        IdlePlaceholder()
                    }
                }

                is GuideUiState.Loading -> {
                    item {
                        LoadingCard(query = state.query)
                    }
                }

                is GuideUiState.Error -> {
                    item {
                        ErrorCard(
                            errorMessage = state.message,
                            onRetry = { viewModel.analyzePiece() }
                        )
                    }
                }

                is GuideUiState.Success -> {
                    item {
                        AnalysisResultView(
                            analysis = state.analysis,
                            isSaved = state.isSaved,
                            onSaveClick = { showSaveDialog = true },
                            onOpenLink = { url -> openWebOrYouTubeLink(context, url) }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Bottom nav padding
            }
        }

        // Save Piece Modal
        if (showSaveDialog && uiState is GuideUiState.Success) {
            val analysis = (uiState as GuideUiState.Success).analysis
            SaveToRepertoireDialog(
                pieceTitle = analysis.pieceTitle,
                composer = analysis.composer,
                onDismiss = { showSaveDialog = false },
                onConfirmSave = { status, notes ->
                    viewModel.saveCurrentPieceToRepertoire(status, notes)
                    showSaveDialog = false
                    Toast.makeText(context, "Сохранено в «$status»!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun GuideHeaderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guide_header_card"),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GoldPrimary.copy(alpha = 0.15f))
                    .border(1.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "ИИ-ГИД ПО РЕПЕРТУАРУ",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = "Музыковедческий анализ, тонкости стиля и ссылки на эталонные записи.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }
    }
}

@Composable
private fun SearchSection(
    searchQuery: String,
    selectedInstrument: String,
    availableInstruments: List<String>,
    isMenuOpen: Boolean,
    onQueryChange: (String) -> Unit,
    onInstrumentSelect: (String) -> Unit,
    onMenuToggle: () -> Unit,
    onAnalyze: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Text Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("repertoire_search_input"),
                placeholder = {
                    Text("Напр. Ferdinand David - Concertino Op. 4", fontSize = 14.sp, color = TextMuted)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = onClear) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = CharcoalBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = GoldPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            // Instrument Selector Row + Analyze Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Instrument Picker Dropdown
                Box {
                    Surface(
                        onClick = onMenuToggle,
                        shape = RoundedCornerShape(12.dp),
                        color = CharcoalSurfaceVariant,
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder)),
                        modifier = Modifier.height(44.dp).testTag("instrument_selector_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = null, tint = BrassAccent, modifier = Modifier.size(16.dp))
                            Text(
                                text = selectedInstrument,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = onMenuToggle,
                        modifier = Modifier.background(CharcoalSurface)
                    ) {
                        availableInstruments.forEach { inst ->
                            DropdownMenuItem(
                                text = { Text(inst, color = TextPrimary) },
                                onClick = { onInstrumentSelect(inst) }
                            )
                        }
                    }
                }

                // Analyze Button
                Button(
                    onClick = onAnalyze,
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("analyze_piece_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = CharcoalDarkest
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "АНАЛИЗ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun IdlePlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.HistoryEdu,
                contentDescription = null,
                tint = GoldDark,
                modifier = Modifier.size(54.dp)
            )
            Text(
                text = "Откройте шедевры классической музыки",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            )
            Text(
                text = "Введите любое произведение или выберите быстрый пресет выше для моментального музыковедческого анализа, советов по штрихам и кликабельных ссылок на легендарные исполнения.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingCard(query: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("loading_analysis_card"),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = GoldPrimary,
                trackColor = CharcoalBorder,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Анализируем произведение...",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            )
            Text(
                text = "Синтезируем исторический контекст, технические нюансы и эталонные аудиозаписи для «$query»...",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorCard(errorMessage: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = GoldDark, modifier = Modifier.size(40.dp))
            Text(
                text = "Уведомление",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalDarkest),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Повторить", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AnalysisResultView(
    analysis: RepertoireAnalysis,
    isSaved: Boolean,
    onSaveClick: () -> Unit,
    onOpenLink: (url: String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hero Piece Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("piece_analysis_hero"),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = analysis.composer,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = analysis.pieceTitle,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Инструмент: ${analysis.instrument}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }

                    // Save to Repertoire Button
                    Button(
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) SuccessGreen.copy(alpha = 0.2f) else GoldPrimary,
                            contentColor = if (isSaved) SuccessGreen else CharcoalDarkest
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_to_repertoire_button")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Check else Icons.Default.Bookmark,
                            contentDescription = "Сохранить",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSaved) "В библиотеке" else "В репертуар",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Listen to full piece on YouTube button
                Button(
                    onClick = { onOpenLink(analysis.getGeneralYouTubeSearchUrl()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("listen_piece_youtube_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CharcoalSurfaceVariant,
                        contentColor = GoldLight
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Слушать произведение на YouTube", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Section 1: Era & Historical Style
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ЭПОХА И МУЗЫКАЛЬНЫЙ СТИЛЬ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = analysis.eraAndStyle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        lineHeight = 22.sp
                    )
                )
            }
        }

        // Section 2: Performance & Technical Tips
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ИСПОЛНИТЕЛЬСКИЕ И ТЕХНИЧЕСКИЕ СОВЕТЫ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            letterSpacing = 1.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                analysis.technicalTips.forEachIndexed { i, tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = tip,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                lineHeight = 21.sp
                            )
                        )
                    }
                }
            }
        }

        // Section 3: Famous Interpretations to Study
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ЭТАЛОННЫЕ ЗАПИСИ МАСТЕРОВ (3 ИСПОЛНЕНИЯ)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                analysis.famousRecordings.forEachIndexed { idx, recording ->
                    FamousRecordingItem(
                        index = idx + 1,
                        recording = recording,
                        composer = analysis.composer,
                        pieceTitle = analysis.pieceTitle,
                        onOpen = onOpenLink
                    )
                    if (idx < analysis.famousRecordings.size - 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // Section 4: Recommended Reference Listening
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(20.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Headphones, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "РОДСТВЕННЫЕ ПРОИЗВЕДЕНИЯ ДЛЯ СЛУХОВОГО ОПЫТА",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                analysis.recommendedListening.forEachIndexed { idx, work ->
                    RecommendedWorkItem(
                        index = idx + 1,
                        work = work,
                        onOpen = onOpenLink
                    )
                    if (idx < analysis.recommendedListening.size - 1) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FamousRecordingItem(
    index: Int,
    recording: FamousRecording,
    composer: String,
    pieceTitle: String,
    onOpen: (String) -> Unit
) {
    Surface(
        color = CharcoalSurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = recording.performer,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                // Clickable YouTube Search Intent Button
                Button(
                    onClick = {
                        val url = recording.getYouTubeSearchUrl(composer, pieceTitle)
                        onOpen(url)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalDarkest),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("listen_rec_${recording.performer.take(6)}")
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Слушать", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (recording.orchestraOrAccompanist.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = recording.orchestraOrAccompanist,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GoldLight,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }

            if (recording.highlight.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = recording.highlight,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun RecommendedWorkItem(
    index: Int,
    work: RecommendedWork,
    onOpen: (String) -> Unit
) {
    Surface(
        color = CharcoalSurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CharcoalBorder)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = work.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "автор: ${work.composer}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = GoldLight,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Clickable YouTube Search Intent Button
                Button(
                    onClick = {
                        val url = work.getYouTubeSearchUrl()
                        onOpen(url)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalDarkest),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("listen_work_${work.title.take(6)}")
                ) {
                    Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Слушать", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (work.whyListen.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = work.whyListen,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun SaveToRepertoireDialog(
    pieceTitle: String,
    composer: String,
    onDismiss: () -> Unit,
    onConfirmSave: (status: String, notes: String) -> Unit
) {
    val statuses = listOf("В работе (Учу)", "Шлифовка", "Выучено (Мастер)")
    var selectedStatus by remember { mutableStateOf("В работе (Учу)") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Сохранить в Мой репертуар", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, color = GoldLight)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "$composer: $pieceTitle",
                    style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold)
                )

                Text(
                    text = "Выберите статус в библиотеке:",
                    style = MaterialTheme.typography.labelMedium.copy(color = GoldLight, fontWeight = FontWeight.Bold)
                )

                statuses.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = status }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = GoldPrimary,
                                unselectedColor = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = status, color = TextPrimary)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Личные цели и комментарии") },
                    placeholder = { Text("Напр. Готовлю к весеннему конкурсу/прослушиванию") },
                    modifier = Modifier.fillMaxWidth().testTag("save_repertoire_notes_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        cursorColor = GoldPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmSave(selectedStatus, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = CharcoalDarkest),
                modifier = Modifier.testTag("confirm_save_repertoire_button")
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold)
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

/**
 * Opens dynamic search URL in YouTube app or external browser via ACTION_VIEW Intent.
 */
private fun openWebOrYouTubeLink(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
