package com.example.trainer.rhythm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

enum class RhythmType {
    PATTERN_PRACTICE,
    POLYRHYTHM,
    SILENT_BARS
}

enum class TapFeedback(val label: String, val colorHex: Long) {
    PERFECT("ИДЕАЛЬНО", 0xFF4CAF50),
    EARLY("СПЕШИТЕ", 0xFFE5A93C),
    LATE("ОПАЗДЫВАЕТЕ", 0xFFE57373),
    MISS("ПРОМАХ", 0xFF7E7E8A)
}

data class RhythmNote(
    val beatOffset: Double, // in quarter-note beats from start of pattern
    val durationBeats: Double,
    val displaySymbol: String // "♩", "♪", "♬", "𝅘𝅥𝅯"
)

data class TapResult(
    val noteIndex: Int,
    val deltaMs: Long,
    val feedback: TapFeedback
)

data class RhythmTrainerState(
    val type: RhythmType = RhythmType.PATTERN_PRACTICE,
    val isPlaying: Boolean = false,
    val isCountingIn: Boolean = false,
    val countInBeat: Int = 0, // 1 to 4
    val bpm: Int = 80,
    val timeSignature: String = "4/4",
    val beatsPerBar: Int = 4,
    val currentPatternName: String = "Синкопы и восьмые",
    val notes: List<RhythmNote> = emptyList(),
    val totalPatternBeats: Double = 4.0,
    val currentBeatProgress: Double = 0.0,
    val lastTapFeedback: TapResult? = null,
    val tapResults: List<TapResult> = emptyList(),
    val finalScore: Int? = null,
    // Polyrhythm
    val polyrhythmRatio: Pair<Int, Int> = 3 to 2, // e.g. 3 against 2
    val polyBeatA: Int = 0,
    val polyBeatB: Int = 0,
    // Silent bars
    val audibleBars: Int = 2,
    val silentBars: Int = 2,
    val currentBarIndex: Int = 0,
    val isCurrentlySilent: Boolean = false
)

class RhythmTrainerEngine(private val context: Context) {
    private val tag = "RhythmTrainerEngine"
    private val scope = CoroutineScope(Dispatchers.Default)
    private var loopJob: Job? = null

    private val _state = MutableStateFlow(RhythmTrainerState())
    val state: StateFlow<RhythmTrainerState> = _state.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private val sampleRate = 44100
    private val clickHighPcm by lazy { generateClickPcm(1900.0, 25) }
    private val clickLowPcm by lazy { generateClickPcm(950.0, 20) }
    private val cowbellPcm by lazy { generateClickPcm(600.0, 30) }

    // Timestamps for measuring tap precision
    private val targetNoteTimestamps = mutableListOf<Long>()
    private var patternStartTimeMs = 0L

    init {
        initAudioTrack()
        loadPattern("Синкопы и восьмые", "4/4")
    }

    private fun initAudioTrack() {
        try {
            val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf.coerceAtLeast(sampleRate / 4))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(tag, "AudioTrack init failed", e)
        }
    }

    fun setRhythmType(type: RhythmType) {
        stop()
        _state.update { it.copy(type = type, finalScore = null, tapResults = emptyList()) }
        if (type == RhythmType.PATTERN_PRACTICE) {
            loadPattern("Синкопы и восьмые", _state.value.timeSignature)
        }
    }

    fun setBpm(newBpm: Int) {
        _state.update { it.copy(bpm = newBpm.coerceIn(40, 200)) }
    }

    fun setTimeSignature(sig: String) {
        val beats = when (sig) {
            "3/4" -> 3
            "6/8" -> 6
            "5/4" -> 5
            else -> 4
        }
        _state.update { it.copy(timeSignature = sig, beatsPerBar = beats) }
        loadPattern(_state.value.currentPatternName, sig)
    }

    fun loadPattern(patternName: String, timeSignature: String = _state.value.timeSignature) {
        val notes = when (patternName) {
            "Четверти и восьмые (Базовый)" -> listOf(
                RhythmNote(0.0, 1.0, "♩"),
                RhythmNote(1.0, 0.5, "♪"),
                RhythmNote(1.5, 0.5, "♪"),
                RhythmNote(2.0, 1.0, "♩"),
                RhythmNote(3.0, 1.0, "♩")
            )
            "Триоли (Triplets)" -> listOf(
                RhythmNote(0.0, 0.333, "𝅘𝅥𝅯"),
                RhythmNote(0.333, 0.333, "𝅘𝅥𝅯"),
                RhythmNote(0.666, 0.334, "𝅘𝅥𝅯"),
                RhythmNote(1.0, 1.0, "♩"),
                RhythmNote(2.0, 0.333, "𝅘𝅥𝅯"),
                RhythmNote(2.333, 0.333, "𝅘𝅥𝅯"),
                RhythmNote(2.666, 0.334, "𝅘𝅥𝅯"),
                RhythmNote(3.0, 1.0, "♩")
            )
            "Пунктирный ритм и шестнадцатые" -> listOf(
                RhythmNote(0.0, 0.75, "♩."),
                RhythmNote(0.75, 0.25, "𝅘𝅥𝅯"),
                RhythmNote(1.0, 0.25, "♬"),
                RhythmNote(1.25, 0.25, "♬"),
                RhythmNote(1.5, 0.25, "♬"),
                RhythmNote(1.75, 0.25, "♬"),
                RhythmNote(2.0, 1.0, "♩"),
                RhythmNote(3.0, 1.0, "♩")
            )
            "Двухтактовый этюд (2 такта)" -> listOf(
                RhythmNote(0.0, 1.0, "♩"),
                RhythmNote(1.0, 0.5, "♪"),
                RhythmNote(1.5, 0.5, "♪"),
                RhythmNote(2.0, 1.0, "♩"),
                RhythmNote(3.0, 1.0, "♩"),
                RhythmNote(4.0, 0.5, "♪"),
                RhythmNote(4.5, 0.5, "♪"),
                RhythmNote(5.0, 0.5, "♪"),
                RhythmNote(5.5, 0.5, "♪"),
                RhythmNote(6.0, 1.0, "♩"),
                RhythmNote(7.0, 1.0, "♩")
            )
            else -> listOf( // "Синкопы и восьмые"
                RhythmNote(0.0, 0.5, "♪"),
                RhythmNote(0.5, 1.0, "♩"), // Syncopation!
                RhythmNote(1.5, 0.5, "♪"),
                RhythmNote(2.0, 0.5, "♪"),
                RhythmNote(2.5, 0.5, "♪"),
                RhythmNote(3.0, 1.0, "♩")
            )
        }

        val totalBeats = if (patternName.contains("Двухтактовый")) 8.0 else _state.value.beatsPerBar.toDouble()
        _state.update {
            it.copy(
                currentPatternName = patternName,
                notes = notes,
                totalPatternBeats = totalBeats,
                finalScore = null,
                tapResults = emptyList(),
                lastTapFeedback = null
            )
        }
    }

    fun startPractice() {
        if (_state.value.isPlaying) return

        targetNoteTimestamps.clear()
        _state.update {
            it.copy(
                isPlaying = true,
                isCountingIn = true,
                countInBeat = 0,
                finalScore = null,
                tapResults = emptyList(),
                lastTapFeedback = null
            )
        }

        loopJob = scope.launch {
            val beatIntervalMs = (60_000.0 / _state.value.bpm).toLong()

            // 1. COUNT-IN (4 BEATS)
            for (beat in 1..4) {
                _state.update { it.copy(countInBeat = beat) }
                playClick(beat == 1)
                triggerHaptic(beat == 1)
                delay(beatIntervalMs)
            }

            _state.update { it.copy(isCountingIn = false, countInBeat = 0) }

            when (_state.value.type) {
                RhythmType.PATTERN_PRACTICE -> runPatternLoop(beatIntervalMs)
                RhythmType.POLYRHYTHM -> runPolyrhythmLoop(beatIntervalMs)
                RhythmType.SILENT_BARS -> runSilentBarsLoop(beatIntervalMs)
            }
        }
    }

    private suspend fun runPatternLoop(beatIntervalMs: Long) {
        val pattern = _state.value.notes
        val totalBeats = _state.value.totalPatternBeats
        patternStartTimeMs = System.currentTimeMillis()

        // Populate expected target note timestamps
        targetNoteTimestamps.clear()
        for (note in pattern) {
            val offsetMs = (note.beatOffset * beatIntervalMs).toLong()
            targetNoteTimestamps.add(patternStartTimeMs + offsetMs)
        }

        val patternDurationMs = (totalBeats * beatIntervalMs).toLong()
        val startTime = System.currentTimeMillis()

        // Play pattern metronome beats while user taps
        var beatIndex = 0
        while (currentCoroutineContext().isActive && System.currentTimeMillis() - startTime < patternDurationMs) {
            val elapsedMs = System.currentTimeMillis() - startTime
            val beatProgress = (elapsedMs.toDouble() / beatIntervalMs).coerceIn(0.0, totalBeats)

            _state.update { it.copy(currentBeatProgress = beatProgress) }

            val currentBeat = (elapsedMs / beatIntervalMs).toInt()
            if (currentBeat > beatIndex && currentBeat < totalBeats) {
                beatIndex = currentBeat
                val isDownbeat = (beatIndex % _state.value.beatsPerBar) == 0
                playClick(isDownbeat)
            }

            delay(15L)
        }

        // Finish pattern evaluation
        finishPatternEvaluation()
    }

    private suspend fun runPolyrhythmLoop(beatIntervalMs: Long) {
        val (voiceA, voiceB) = _state.value.polyrhythmRatio
        // Voice A has voiceA beats, Voice B has voiceB beats in 1 cycle
        val cycleDurationMs = (beatIntervalMs * voiceB).toLong()
        val startTime = System.currentTimeMillis()

        var lastTickA = -1
        var lastTickB = -1

        while (currentCoroutineContext().isActive && System.currentTimeMillis() - startTime < cycleDurationMs * 4) { // 4 cycles
            val elapsed = System.currentTimeMillis() - startTime
            val progressInCycle = (elapsed % cycleDurationMs).toDouble() / cycleDurationMs

            val tickA = (progressInCycle * voiceA).toInt()
            val tickB = (progressInCycle * voiceB).toInt()

            if (tickA != lastTickA) {
                lastTickA = tickA
                playClick(tickA == 0) // High click
                triggerHaptic(tickA == 0)
                _state.update { it.copy(polyBeatA = tickA + 1) }
            }

            if (tickB != lastTickB) {
                lastTickB = tickB
                playCowbell() // Lower cowbell
                _state.update { it.copy(polyBeatB = tickB + 1) }
            }

            delay(10L)
        }

        stop()
    }

    private suspend fun runSilentBarsLoop(beatIntervalMs: Long) {
        val beatsPerBar = _state.value.beatsPerBar
        val audibleBars = _state.value.audibleBars
        val silentBars = _state.value.silentBars
        val totalBars = audibleBars + silentBars

        for (bar in 0 until totalBars * 2) {
            val barInCycle = bar % totalBars
            val isSilent = barInCycle >= audibleBars

            _state.update {
                it.copy(
                    currentBarIndex = barInCycle + 1,
                    isCurrentlySilent = isSilent
                )
            }

            for (beat in 1..beatsPerBar) {
                if (!isSilent) {
                    playClick(beat == 1)
                    triggerHaptic(beat == 1)
                }
                delay(beatIntervalMs)
            }
        }

        stop()
    }

    fun onUserTap() {
        if (!_state.value.isPlaying || _state.value.isCountingIn) return

        val now = System.currentTimeMillis()
        if (targetNoteTimestamps.isEmpty()) return

        // Find closest target note
        var minDiff = Long.MAX_VALUE
        var closestIndex = -1

        for (i in targetNoteTimestamps.indices) {
            val diff = now - targetNoteTimestamps[i]
            if (abs(diff) < abs(minDiff)) {
                minDiff = diff
                closestIndex = i
            }
        }

        if (closestIndex != -1 && abs(minDiff) < 300) {
            val feedback = when {
                abs(minDiff) <= 35 -> TapFeedback.PERFECT
                minDiff < -35 -> TapFeedback.EARLY
                else -> TapFeedback.LATE
            }

            val result = TapResult(
                noteIndex = closestIndex,
                deltaMs = minDiff,
                feedback = feedback
            )

            _state.update {
                it.copy(
                    lastTapFeedback = result,
                    tapResults = it.tapResults + result
                )
            }

            // Auditory tap confirmation
            playTapBlip(feedback == TapFeedback.PERFECT)
        }
    }

    private fun finishPatternEvaluation() {
        val totalNotes = _state.value.notes.size
        val results = _state.value.tapResults

        var score = 0
        if (results.isNotEmpty()) {
            val perfectHits = results.count { it.feedback == TapFeedback.PERFECT }
            val nearHits = results.count { it.feedback == TapFeedback.EARLY || it.feedback == TapFeedback.LATE }

            val rawScore = ((perfectHits * 100.0 + nearHits * 65.0) / totalNotes.coerceAtLeast(1)).toInt()
            score = rawScore.coerceIn(0, 100)
        }

        _state.update {
            it.copy(
                isPlaying = false,
                finalScore = score
            )
        }
    }

    fun setPolyrhythmRatio(a: Int, b: Int) {
        _state.update { it.copy(polyrhythmRatio = a to b) }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
        _state.update {
            it.copy(
                isPlaying = false,
                isCountingIn = false,
                countInBeat = 0,
                currentBeatProgress = 0.0,
                isCurrentlySilent = false
            )
        }
    }

    private fun playClick(isHigh: Boolean) {
        try {
            val pcm = if (isHigh) clickHighPcm else clickLowPcm
            audioTrack?.write(pcm, 0, pcm.size, AudioTrack.WRITE_NON_BLOCKING)
        } catch (_: Exception) {}
    }

    private fun playCowbell() {
        try {
            audioTrack?.write(cowbellPcm, 0, cowbellPcm.size, AudioTrack.WRITE_NON_BLOCKING)
        } catch (_: Exception) {}
    }

    private fun playTapBlip(isPerfect: Boolean) {
        try {
            val freq = if (isPerfect) 1400.0 else 500.0
            val pcm = generateClickPcm(freq, 15)
            audioTrack?.write(pcm, 0, pcm.size, AudioTrack.WRITE_NON_BLOCKING)
        } catch (_: Exception) {}
    }

    private fun triggerHaptic(isAccent: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val effect = VibrationEffect.createOneShot(if (isAccent) 30L else 15L, if (isAccent) 200 else 100)
                vm?.defaultVibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v?.vibrate(VibrationEffect.createOneShot(if (isAccent) 25L else 12L, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            }
        } catch (_: Exception) {}
    }

    private fun generateClickPcm(frequency: Double, durationMs: Int): ByteArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ByteArray(totalSamples * 2)
        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 140.0)
            val sample = (sin(2.0 * PI * frequency * t) * decay * 28000.0).toInt().coerceIn(-32768, 32767)
            pcm[i * 2] = (sample and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sample shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    fun release() {
        stop()
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {}
    }
}
