package com.example.metronome

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

enum class TimeSignature(val beatsPerBar: Int, val label: String) {
    TWO_FOUR(2, "2/4"),
    THREE_FOUR(3, "3/4"),
    FOUR_FOUR(4, "4/4"),
    SIX_EIGHT(6, "6/8")
}

data class MetronomeState(
    val isPlaying: Boolean = false,
    val bpm: Int = 100,
    val timeSignature: TimeSignature = TimeSignature.FOUR_FOUR,
    val currentBeat: Int = 1, // 1 to beatsPerBar
    val isDownbeat: Boolean = true,
    val isAudioEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true
)

class MetronomeEngine(private val context: Context) {
    private val tag = "MetronomeEngine"

    private val _state = MutableStateFlow(MetronomeState())
    val state: StateFlow<MetronomeState> = _state.asStateFlow()

    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // Tap tempo tracking
    private val tapTimestamps = mutableListOf<Long>()

    // PCM click samples pre-generated
    private val sampleRate = 44100
    private val highClickPcm: ByteArray by lazy { generateClickPcm(frequency = 1760.0, durationMs = 28, sampleRate = sampleRate) }
    private val lowClickPcm: ByteArray by lazy { generateClickPcm(frequency = 880.0, durationMs = 20, sampleRate = sampleRate) }

    private var audioTrack: AudioTrack? = null

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate / 4)

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
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize AudioTrack for metronome", e)
        }
    }

    fun togglePlay() {
        if (_state.value.isPlaying) {
            stop()
        } else {
            start()
        }
    }

    fun start() {
        if (_state.value.isPlaying) return

        if (audioTrack == null || audioTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
            initAudioTrack()
        }

        _state.update { it.copy(isPlaying = true, currentBeat = 1, isDownbeat = true) }

        playbackJob = scope.launch {
            while (isActive) {
                val currentState = _state.value
                val beatIntervalMs = (60_000.0 / currentState.bpm).toLong()

                val beat = currentState.currentBeat
                val isAccent = beat == 1

                // Audio click
                if (currentState.isAudioEnabled) {
                    playClick(isAccent)
                }

                // Haptic vibration
                if (currentState.isHapticEnabled) {
                    triggerHaptic(isAccent)
                }

                val startTime = System.currentTimeMillis()

                // Advance beat for next tick
                val nextBeat = if (beat >= currentState.timeSignature.beatsPerBar) 1 else beat + 1
                _state.update {
                    it.copy(
                        currentBeat = beat,
                        isDownbeat = isAccent
                    )
                }

                val elapsed = System.currentTimeMillis() - startTime
                val sleepTime = (beatIntervalMs - elapsed).coerceAtLeast(5L)
                delay(sleepTime)

                _state.update { it.copy(currentBeat = nextBeat, isDownbeat = nextBeat == 1) }
            }
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        _state.update { it.copy(isPlaying = false, currentBeat = 1, isDownbeat = true) }
    }

    fun setBpm(newBpm: Int) {
        val clamped = newBpm.coerceIn(30, 260)
        _state.update { it.copy(bpm = clamped) }
    }

    fun changeBpmBy(delta: Int) {
        setBpm(_state.value.bpm + delta)
    }

    fun setTimeSignature(signature: TimeSignature) {
        _state.update { it.copy(timeSignature = signature, currentBeat = 1) }
    }

    fun toggleAudio() {
        _state.update { it.copy(isAudioEnabled = !it.isAudioEnabled) }
    }

    fun toggleHaptic() {
        _state.update { it.copy(isHapticEnabled = !it.isHapticEnabled) }
    }

    fun tapTempo() {
        val now = System.currentTimeMillis()
        if (tapTimestamps.isNotEmpty() && now - tapTimestamps.last() > 2500) {
            tapTimestamps.clear()
        }
        tapTimestamps.add(now)
        if (tapTimestamps.size > 5) {
            tapTimestamps.removeAt(0)
        }
        if (tapTimestamps.size >= 2) {
            val intervals = mutableListOf<Long>()
            for (i in 1 until tapTimestamps.size) {
                intervals.add(tapTimestamps[i] - tapTimestamps[i - 1])
            }
            val avgInterval = intervals.average()
            if (avgInterval > 0) {
                val calculatedBpm = (60_000.0 / avgInterval).toInt().coerceIn(30, 260)
                setBpm(calculatedBpm)
            }
        }
    }

    private fun playClick(isHigh: Boolean) {
        try {
            val pcm = if (isHigh) highClickPcm else lowClickPcm
            audioTrack?.write(pcm, 0, pcm.size, AudioTrack.WRITE_NON_BLOCKING)
        } catch (e: Exception) {
            Log.e(tag, "Audio click write failed", e)
        }
    }

    private fun triggerHaptic(isAccent: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val duration = if (isAccent) 35L else 18L
                val amplitude = if (isAccent) VibrationEffect.DEFAULT_AMPLITUDE else 80
                val effect = VibrationEffect.createOneShot(duration, amplitude)
                vibratorManager?.vibrate(CombinedVibration.createParallel(effect))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val duration = if (isAccent) 30L else 15L
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createOneShot(duration, if (isAccent) 200 else 100)
                    vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(duration)
                }
            }
        } catch (_: Exception) {
            // Haptics might be silent/disabled on some devices
        }
    }

    private fun generateClickPcm(frequency: Double, durationMs: Int, sampleRate: Int): ByteArray {
        val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val pcm = ByteArray(totalSamples * 2)

        for (i in 0 until totalSamples) {
            val time = i.toDouble() / sampleRate
            // Exponential decay envelope for crisp percussion woodblock sound
            val decay = exp(-time * 160.0)
            val sampleValue = (sin(2.0 * PI * frequency * time) * decay * 28000.0).toInt().coerceIn(-32768, 32767)

            pcm[i * 2] = (sampleValue and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sampleValue shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    fun release() {
        stop()
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            Log.e(tag, "Error releasing AudioTrack", e)
        }
    }
}
