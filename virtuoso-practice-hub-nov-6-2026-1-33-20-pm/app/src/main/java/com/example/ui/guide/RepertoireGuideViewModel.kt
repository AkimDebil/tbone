package com.example.ui.guide

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.RepertoireAiService
import com.example.data.VirtuosoDatabase
import com.example.data.model.RepertoireAnalysis
import com.example.data.repository.VirtuosoRepository
import com.example.data.settings.UserSettingsManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GuideUiState {
    data object Idle : GuideUiState
    data class Loading(val query: String) : GuideUiState
    data class Success(val analysis: RepertoireAnalysis, val isSaved: Boolean = false) : GuideUiState
    data class Error(val message: String) : GuideUiState
}

class RepertoireGuideViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VirtuosoRepository(VirtuosoDatabase.getDatabase(application))
    private val settingsManager = UserSettingsManager.getInstance(application)
    private val aiService = RepertoireAiService()

    private val _uiState = MutableStateFlow<GuideUiState>(GuideUiState.Idle)
    val uiState: StateFlow<GuideUiState> = _uiState.asStateFlow()

    private val _selectedInstrument = MutableStateFlow("Тромбон / Trombone")
    val selectedInstrument: StateFlow<String> = _selectedInstrument.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val availableInstruments = listOf(
        "Тромбон / Trombone",
        "Труба / Trumpet",
        "Валторна / French Horn",
        "Туба / Tuba",
        "Скрипка / Violin",
        "Альт / Viola",
        "Виолончель / Cello",
        "Контрабас / Double Bass",
        "Флейта / Flute",
        "Кларнет / Clarinet",
        "Гобой / Oboe",
        "Фагот / Bassoon",
        "Фортепиано / Piano"
    )

    val presetSuggestions = listOf(
        "David - Concertino Op. 4" to "Тромбон / Trombone",
        "Rimsky-Korsakov - Trombone Concerto" to "Тромбон / Trombone",
        "Henri Tomasi - Trombone Concerto" to "Тромбон / Trombone",
        "Haydn - Trumpet Concerto" to "Труба / Trumpet",
        "J.S. Bach - Cello Suite No. 1" to "Виолончель / Cello",
        "Tchaikovsky - Violin Concerto" to "Скрипка / Violin",
        "Mozart - Clarinet Concerto" to "Кларнет / Clarinet",
        "Ravel - Bolero" to "Все инструменты / Orchestral"
    )

    fun onQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onInstrumentSelected(instrument: String) {
        _selectedInstrument.value = instrument
    }

    fun selectPreset(query: String, instrument: String) {
        _searchQuery.value = query
        _selectedInstrument.value = instrument
        analyzePiece(query, instrument)
    }

    fun analyzePiece(query: String? = null, instrument: String? = null) {
        val q = query ?: _searchQuery.value.trim()
        val inst = instrument ?: _selectedInstrument.value

        if (q.isBlank()) return

        _searchQuery.value = q
        _selectedInstrument.value = inst
        _uiState.value = GuideUiState.Loading(q)

        val currentLang = settingsManager.settings.value.language

        viewModelScope.launch {
            try {
                val result = aiService.analyzePiece(q, inst, currentLang)
                _uiState.value = GuideUiState.Success(result, isSaved = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Fallback guarantee: never show an error card
                val fallback = com.example.ai.RepertoireKnowledgeBase.generateLocalAnalysis(q, inst, currentLang)
                _uiState.value = GuideUiState.Success(fallback, isSaved = false)
            }
        }
    }

    fun saveCurrentPieceToRepertoire(status: String = "Currently Learning", notes: String = "") {
        val current = _uiState.value
        if (current is GuideUiState.Success) {
            viewModelScope.launch {
                repository.savePieceToRepertoire(current.analysis, status, notes)
                _uiState.value = current.copy(isSaved = true)
            }
        }
    }

    fun displayAnalysis(analysis: RepertoireAnalysis) {
        _searchQuery.value = "${analysis.composer} - ${analysis.pieceTitle}"
        _selectedInstrument.value = analysis.instrument
        _uiState.value = GuideUiState.Success(analysis, isSaved = true)
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.value = GuideUiState.Idle
    }
}
