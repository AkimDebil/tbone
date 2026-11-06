package com.example.ui.studio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VirtuosoDatabase
import com.example.data.entity.PracticeRoutineEntity
import com.example.data.repository.VirtuosoRepository
import com.example.metronome.MetronomeEngine
import com.example.metronome.TimeSignature
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class TimerState(
    val isRunning: Boolean = false,
    val remainingSeconds: Int = 15 * 60,
    val totalSeconds: Int = 15 * 60,
    val activeStageId: Long = 0,
    val elapsedSessionSeconds: Int = 0
)

class PracticeStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VirtuosoRepository(VirtuosoDatabase.getDatabase(application))
    val metronomeEngine = MetronomeEngine(application)

    val routineItems: StateFlow<List<PracticeRoutineEntity>> = repository.routineItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _activeStage = MutableStateFlow<PracticeRoutineEntity?>(null)
    val activeStage: StateFlow<PracticeRoutineEntity?> = _activeStage.asStateFlow()

    private val _sessionCompletedCelebration = MutableStateFlow(false)
    val sessionCompletedCelebration: StateFlow<Boolean> = _sessionCompletedCelebration.asStateFlow()

    private val _lastLoggedMinutes = MutableStateFlow(0)
    val lastLoggedMinutes: StateFlow<Int> = _lastLoggedMinutes.asStateFlow()

    private var countdownJob: Job? = null

    init {
        // Monitor routine items to select the first uncompleted or current active stage
        viewModelScope.launch {
            routineItems.collect { items ->
                if (items.isNotEmpty()) {
                    val current = _activeStage.value
                    if (current == null || items.none { it.id == current.id }) {
                        // Pick first uncompleted stage or the first item
                        val target = items.firstOrNull { !it.isCompleted } ?: items.first()
                        selectStage(target)
                    } else {
                        // Refresh active stage from latest database values
                        val updated = items.find { it.id == current.id }
                        _activeStage.value = updated
                    }
                }
            }
        }
    }

    fun selectStage(stage: PracticeRoutineEntity) {
        if (_activeStage.value?.id == stage.id) return

        pauseTimer()
        _activeStage.value = stage
        val durationSecs = stage.durationMinutes * 60
        _timerState.update {
            it.copy(
                isRunning = false,
                remainingSeconds = durationSecs,
                totalSeconds = durationSecs,
                activeStageId = stage.id
            )
        }

        // Sync metronome BPM to stage default
        metronomeEngine.setBpm(stage.targetBpm)
        when (stage.timeSignature) {
            "2/4" -> metronomeEngine.setTimeSignature(TimeSignature.TWO_FOUR)
            "3/4" -> metronomeEngine.setTimeSignature(TimeSignature.THREE_FOUR)
            "6/8" -> metronomeEngine.setTimeSignature(TimeSignature.SIX_EIGHT)
            else -> metronomeEngine.setTimeSignature(TimeSignature.FOUR_FOUR)
        }
    }

    fun toggleTimer() {
        if (_timerState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return

        _timerState.update { it.copy(isRunning = true) }
        countdownJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _timerState.update { current ->
                    val newRemaining = (current.remainingSeconds - 1).coerceAtLeast(0)
                    val newElapsed = current.elapsedSessionSeconds + 1

                    if (newRemaining <= 0) {
                        // Timer completed for stage
                        pauseTimer()
                        onCurrentStageTimerFinished()
                    }

                    current.copy(
                        remainingSeconds = newRemaining,
                        elapsedSessionSeconds = newElapsed
                    )
                }
            }
        }
    }

    fun pauseTimer() {
        countdownJob?.cancel()
        countdownJob = null
        _timerState.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        pauseTimer()
        val stage = _activeStage.value ?: return
        val totalSecs = stage.durationMinutes * 60
        _timerState.update {
            it.copy(
                remainingSeconds = totalSecs,
                totalSeconds = totalSecs
            )
        }
    }

    private fun onCurrentStageTimerFinished() {
        val current = _activeStage.value ?: return
        markStageCompleted(current.id, true)
    }

    fun markStageCompleted(stageId: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.updateCompletionStatus(stageId, completed)

            val items = routineItems.value
            val nextStage = items.firstOrNull { it.id != stageId && !it.isCompleted }

            if (nextStage != null) {
                selectStage(nextStage)
            } else {
                // All stages complete!
                if (completed) {
                    _sessionCompletedCelebration.value = true
                }
            }
        }
    }

    fun updateStageNotes(stageId: Long, notes: String) {
        viewModelScope.launch {
            repository.updateRoutineNotes(stageId, notes)
        }
    }

    fun addCustomStage(title: String, durationMinutes: Int, bpm: Int, timeSig: String) {
        viewModelScope.launch {
            val count = routineItems.value.size
            val newEntity = PracticeRoutineEntity(
                stageType = "CUSTOM",
                title = title,
                subtitle = "Custom practice focus block",
                durationMinutes = durationMinutes.coerceAtLeast(1),
                targetBpm = bpm.coerceIn(30, 260),
                timeSignature = timeSig,
                sessionNotes = "",
                isCompleted = false,
                orderIndex = count
            )
            repository.addRoutineItem(newEntity)
        }
    }

    fun deleteStage(stage: PracticeRoutineEntity) {
        viewModelScope.launch {
            repository.deleteRoutineItem(stage)
        }
    }

    fun resetRoutine() {
        viewModelScope.launch {
            pauseTimer()
            metronomeEngine.stop()
            repository.resetRoutineCompletions()
            val first = routineItems.value.firstOrNull()
            if (first != null) {
                selectStage(first)
            }
        }
    }

    fun restoreDefaultRoutine() {
        viewModelScope.launch {
            pauseTimer()
            metronomeEngine.stop()
            repository.restoreDefaultRoutine()
        }
    }

    fun dismissCelebration() {
        _sessionCompletedCelebration.value = false
    }

    fun savePracticeSessionLog(notes: String) {
        viewModelScope.launch {
            val totalSecs = _timerState.value.elapsedSessionSeconds
            val minutes = (totalSecs / 60).coerceAtLeast(15) // Log at least the routine duration if testing
            val completedCount = routineItems.value.count { it.isCompleted }

            repository.logCompletedSession(
                durationMinutes = minutes,
                stagesCompletedCount = completedCount,
                routineTitle = "Daily Studio Routine",
                sessionNotes = notes.ifBlank { "Completed structured practice routine: Warm-up, Technique, Repertoire & Sight-Reading." }
            )

            _lastLoggedMinutes.value = minutes
            _sessionCompletedCelebration.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        metronomeEngine.release()
    }
}
