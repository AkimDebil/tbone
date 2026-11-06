package com.example.trainer.tone

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sqrt

data class PitchPoint(
    val timestamp: Long,
    val centsOffset: Float,
    val noteName: String
)

data class PitchDetectorState(
    val isListening: Boolean = false,
    val isDemoMode: Boolean = false,
    val detectedFrequencyHz: Double = 0.0,
    val noteName: String = "--",
    val octave: Int = 0,
    val centsOffset: Float = 0f, // -50.0 to +50.0
    val rmsVolume: Float = 0f,
    val isSoundDetected: Boolean = false,
    val stabilityScorePercent: Int = 95,
    val stabilityFeedback: String = "Готов к прослушиванию",
    val recentPitchHistory: List<PitchPoint> = emptyList()
)

class PitchDetector(private val context: Context) {
    private val tag = "PitchDetector"
    private val scope = CoroutineScope(Dispatchers.Default)
    private var listeningJob: Job? = null
    private var audioRecord: AudioRecord? = null

    private val _state = MutableStateFlow(PitchDetectorState())
    val state: StateFlow<PitchDetectorState> = _state.asStateFlow()

    private val sampleRate = 22050 // Optimized for fast pitch tracking
    private val bufferSize = 4096

    private val noteNames = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")

    fun toggleListening(hasMicPermission: Boolean) {
        if (_state.value.isListening) {
            stop()
        } else {
            start(hasMicPermission)
        }
    }

    fun start(hasMicPermission: Boolean) {
        if (_state.value.isListening) return

        if (!hasMicPermission) {
            // Automatically switch to simulation mode if mic permission is unavailable
            startDemoSimulation()
            return
        }

        try {
            startMicrophoneRecording()
        } catch (e: Exception) {
            Log.w(tag, "Microphone unavailable in container, falling back to tone simulation", e)
            startDemoSimulation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startMicrophoneRecording() {
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuf.coerceAtLeast(bufferSize * 2)
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            startDemoSimulation()
            return
        }

        audioRecord?.startRecording()
        _state.update { it.copy(isListening = true, isDemoMode = false) }

        listeningJob = scope.launch {
            val audioData = ShortArray(bufferSize)

            while (isActive) {
                val read = audioRecord?.read(audioData, 0, bufferSize) ?: -1
                if (read > 0) {
                    processAudioBuffer(audioData, read)
                }
                delay(40L) // ~25 updates per second for smooth UI
            }
        }
    }

    fun startDemoSimulation() {
        stop()
        _state.update { it.copy(isListening = true, isDemoMode = true) }

        listeningJob = scope.launch {
            var simTime = 0.0
            val targetFreq = 174.61 // F3 trombone/cello reference
            while (isActive) {
                simTime += 0.05
                // Gentle realistic air stream waver (±4 cents)
                val waver = (kotlin.math.sin(simTime * 2.5) * 2.8 + kotlin.math.cos(simTime * 6.0) * 1.2).toFloat()
                val currentFreq = targetFreq * 2.0.pow(waver / 1200.0)

                updateWithPitch(currentFreq, 0.45f)
                delay(50L)
            }
        }
    }

    private fun processAudioBuffer(buffer: ShortArray, size: Int) {
        // Calculate RMS Volume
        var sumSquares = 0.0
        for (i in 0 until size) {
            sumSquares += buffer[i] * buffer[i]
        }
        val rms = sqrt(sumSquares / size) / 32768.0f

        if (rms < 0.012f) {
            // Silence / background noise
            _state.update { it.copy(isSoundDetected = false, rmsVolume = rms.toFloat()) }
            return
        }

        // Autocorrelation pitch detection
        val pitchHz = detectPitchAutocorrelation(buffer, size)
        if (pitchHz in 50.0..1200.0) {
            updateWithPitch(pitchHz, rms.toFloat())
        } else {
            _state.update { it.copy(isSoundDetected = false, rmsVolume = rms.toFloat()) }
        }
    }

    private fun updateWithPitch(frequencyHz: Double, rms: Float) {
        // Convert frequency to closest chromatic note and cents offset
        val a4 = 440.0
        val semitonesFromA4 = 12.0 * (log2(frequencyHz / a4))
        val roundedMidi = (semitonesFromA4 + 69.0).toInt()

        val noteIndex = ((roundedMidi % 12) + 12) % 12
        val octave = (roundedMidi / 12) - 1
        val noteName = noteNames[noteIndex]

        val exactFreq = a4 * 2.0.pow((roundedMidi - 69) / 12.0)
        val cents = (1200.0 * log2(frequencyHz / exactFreq)).toFloat().coerceIn(-50f, 50f)

        val point = PitchPoint(System.currentTimeMillis(), cents, noteName)

        val newHistory = (_state.value.recentPitchHistory + point).takeLast(45)

        // Calculate stability from standard deviation of recent cents
        val recentCents = newHistory.takeLast(15).map { it.centsOffset }
        val avg = recentCents.average()
        val variance = recentCents.map { (it - avg).pow(2) }.average()
        val stdDev = sqrt(variance)

        val score = (100 - (stdDev * 5.0)).toInt().coerceIn(60, 99)
        val feedback = when {
            stdDev < 2.5 -> "Идеальный центр звука (±2 цента)"
            stdDev < 5.0 -> "Уверенная опора дыхания"
            stdDev < 9.0 -> "Небольшие колебания амбушюра"
            else -> "Держите воздушный столб плотнее"
        }

        _state.update {
            it.copy(
                detectedFrequencyHz = frequencyHz,
                noteName = noteName,
                octave = octave,
                centsOffset = cents,
                rmsVolume = rms,
                isSoundDetected = true,
                stabilityScorePercent = score,
                stabilityFeedback = feedback,
                recentPitchHistory = newHistory
            )
        }
    }

    private fun detectPitchAutocorrelation(buffer: ShortArray, size: Int): Double {
        val minLag = sampleRate / 1000 // up to 1000 Hz
        val maxLag = sampleRate / 55   // down to 55 Hz (A1)

        var bestLag = -1
        var bestCorrelation = -1.0

        for (lag in minLag..maxLag.coerceAtMost(size / 2)) {
            var sum = 0.0
            for (i in 0 until (size - lag)) {
                sum += buffer[i] * buffer[i + lag]
            }
            if (sum > bestCorrelation) {
                bestCorrelation = sum
                bestLag = lag
            }
        }

        return if (bestLag > 0) sampleRate.toDouble() / bestLag else 0.0
    }

    fun stop() {
        listeningJob?.cancel()
        listeningJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (_: Exception) {}
        _state.update { it.copy(isListening = false, isSoundDetected = false) }
    }

    fun release() {
        stop()
    }
}
