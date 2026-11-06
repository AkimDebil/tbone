package com.example.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VirtuosoDatabase
import com.example.data.entity.PracticeLogEntity
import com.example.data.entity.SavedRepertoireEntity
import com.example.data.model.RepertoireAnalysis
import com.example.data.repository.PracticeStats
import com.example.data.repository.VirtuosoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RepertoireAndLogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VirtuosoRepository(VirtuosoDatabase.getDatabase(application))

    private val _statusFilter = MutableStateFlow("Все")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _searchFilter = MutableStateFlow("")
    val searchFilter: StateFlow<String> = _searchFilter.asStateFlow()

    val practiceStats: StateFlow<PracticeStats> = repository.practiceStats
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            PracticeStats(0, 0, 0, 0, 0, 0)
        )

    val practiceLogs: StateFlow<List<PracticeLogEntity>> = repository.practiceLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRepertoire: StateFlow<List<SavedRepertoireEntity>> = combine(
        repository.savedRepertoire,
        _statusFilter,
        _searchFilter
    ) { list, filter, query ->
        list.filter { item ->
            val matchesFilter = when (filter) {
                "Все", "All", "全部" -> true
                "В работе (Учу)", "Currently Learning", "正在练习" ->
                    item.status == "В работе (Учу)" || item.status == "Currently Learning" || item.status == "正在练习"
                "Шлифовка", "Polishing", "精细打磨" ->
                    item.status == "Шлифовка" || item.status == "Polishing" || item.status == "精细打磨"
                "Выучено (Мастер)", "Mastered", "已经掌握" ->
                    item.status == "Выучено (Мастер)" || item.status == "Mastered" || item.status == "已经掌握"
                else -> item.status.equals(filter, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.composer.contains(query, ignoreCase = true) ||
                    item.instrument.contains(query, ignoreCase = true)

            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun setSearchFilter(query: String) {
        _searchFilter.value = query
    }

    fun updatePieceStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateRepertoireStatus(id, newStatus)
        }
    }

    fun deletePiece(item: SavedRepertoireEntity) {
        viewModelScope.launch {
            repository.deleteRepertoire(item)
        }
    }

    fun logManualSession(
        durationMinutes: Int,
        routineTitle: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.logCompletedSession(
                durationMinutes = durationMinutes,
                stagesCompletedCount = 1,
                routineTitle = routineTitle.ifBlank { "Personal Practice Session" },
                sessionNotes = notes
            )
        }
    }

    fun parsePieceToAnalysis(item: SavedRepertoireEntity): RepertoireAnalysis {
        return VirtuosoRepository.parseRepertoireAnalysis(item)
    }
}
