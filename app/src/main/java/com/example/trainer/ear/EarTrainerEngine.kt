package com.example.trainer.ear

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

data class MusicalInterval(
    val nameRu: String,
    val semitones: Int,
    val shortCode: String,
    val mnemonicExample: String
)

val ALL_INTERVALS = listOf(
    MusicalInterval("Малая секунда", 1, "м.2", "«Челюсти», полутон"),
    MusicalInterval("Большая секунда", 2, "б.2", "Начало гаммы, целый тон"),
    MusicalInterval("Малая терция", 3, "м.3", "Грустное минорное созвучие"),
    MusicalInterval("Большая терция", 4, "б.3", "Светлое мажорное трезвучие"),
    MusicalInterval("Чистая кварта", 5, "ч.4", "Гимн, зов горна"),
    MusicalInterval("Тритон", 6, "Тритон", "Дьявол в музыке, напряжение"),
    MusicalInterval("Чистая квинта", 7, "ч.5", "«Звёздные войны», бурдон"),
    MusicalInterval("Малая секста", 8, "м.6", "«Love Story», томление"),
    MusicalInterval("Большая секста", 9, "б.6", "«В лесу родилась ёлочка»"),
    MusicalInterval("Малая септима", 10, "м.7", "Доминантсептаккорд"),
    MusicalInterval("Большая септима", 11, "б.7", "Острое тяготение к октаве"),
    MusicalInterval("Чистая октава", 12, "ч.8", "Полное звуковое слияние")
)

data class EarTrainerState(
    val isPlaying: Boolean = false,
    val currentInterval: MusicalInterval = ALL_INTERVALS[6], // Default: ч.5
    val rootMidiNote: Int = 53, // F3
    val candidateIntervals: List<MusicalInterval> = emptyList(),
    val isAnswerChecked: Boolean = false,
    val selectedInterval: MusicalInterval? = null,
    val isCorrect: Boolean = false,
    val scoreStreak: Int = 0,
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0
)

class EarTrainerEngine {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _state = MutableStateFlow(EarTrainerState())
    val state: StateFlow<EarTrainerState> = _state.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private val sampleRate = 44100

    init {
        initAudioTrack()
        generateNewQuestion()
    }

    private fun initAudioTrack() {
        try {
            val minBuf = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBuf.coerceAtLeast(sampleRate / 2))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            audioTrack?.play()
        } catch (_: Exception) {}
    }

    fun generateNewQuestion() {
        val target = ALL_INTERVALS.random()
        val root = Random.nextInt(48, 60) // C3 to B3

        // Pick 4 candidate options including target
        val others = ALL_INTERVALS.filter { it != target }.shuffled().take(3)
        val candidates = (others + target).shuffled()

        _state.update {
            it.copy(
                currentInterval = target,
                rootMidiNote = root,
                candidateIntervals = candidates,
                isAnswerChecked = false,
                selectedInterval = null
            )
        }

        playInterval()
    }

    fun playInterval(isHarmonic: Boolean = false) {
        scope.launch {
            _state.update { it.copy(isPlaying = true) }
            val rootMidi = _state.value.rootMidiNote
            val intervalSemitones = _state.value.currentInterval.semitones
            val targetMidi = rootMidi + intervalSemitones

            val rootFreq = 440.0 * 2.0.pow((rootMidi - 69) / 12.0)
            val targetFreq = 440.0 * 2.0.pow((targetMidi - 69) / 12.0)

            if (isHarmonic) {
                // Both notes together
                playTone(rootFreq, targetFreq, 1400)
            } else {
                // Melodic: Root first, then target
                playTone(rootFreq, null, 700)
                delay(120)
                playTone(targetFreq, null, 900)
            }
            _state.update { it.copy(isPlaying = false) }
        }
    }

    fun submitAnswer(chosen: MusicalInterval) {
        if (_state.value.isAnswerChecked) return

        val correct = chosen == _state.value.currentInterval
        _state.update {
            val newStreak = if (correct) it.scoreStreak + 1 else 0
            val newTotal = it.totalAnswered + 1
            val newCorrect = if (correct) it.totalCorrect + 1 else it.totalCorrect

            it.copy(
                isAnswerChecked = true,
                selectedInterval = chosen,
                isCorrect = correct,
                scoreStreak = newStreak,
                totalAnswered = newTotal,
                totalCorrect = newCorrect
            )
        }
    }

    private fun playTone(freq1: Double, freq2: Double?, durationMs: Int) {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ShortArray(totalSamples)

        val inc1 = 2.0 * PI * freq1 / sampleRate
        val inc2 = if (freq2 != null) 2.0 * PI * freq2 / sampleRate else 0.0

        var phase1 = 0.0
        var phase2 = 0.0

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            // Attack and release envelope
            val envelope = when {
                progress < 0.1 -> progress / 0.1
                progress > 0.85 -> (1.0 - progress) / 0.15
                else -> 1.0
            }

            val s1 = sin(phase1)
            val s2 = if (freq2 != null) sin(phase2) else 0.0
            val combined = ((s1 * 0.7 + s2 * 0.5) * envelope * 24000.0).toInt().coerceIn(-32768, 32767)

            pcm[i] = combined.toShort()
            phase1 += inc1
            phase2 += inc2
        }

        audioTrack?.write(pcm, 0, totalSamples)
    }

    fun release() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (_: Exception) {}
    }
}
