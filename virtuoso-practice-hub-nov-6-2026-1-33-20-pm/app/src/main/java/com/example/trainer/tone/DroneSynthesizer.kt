package com.example.trainer.tone

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

data class DroneState(
    val isPlaying: Boolean = false,
    val noteName: String = "F",
    val octave: Int = 3,
    val baseA4Freq: Double = 440.0, // 440, 442, 443
    val isPureFifthEnabled: Boolean = true,
    val volume: Float = 0.8f,
    val currentFrequency: Double = 174.61 // F3 at A440
)

class DroneSynthesizer {
    private val tag = "DroneSynthesizer"
    private val scope = CoroutineScope(Dispatchers.Default)
    private var playbackJob: Job? = null

    private val _state = MutableStateFlow(DroneState())
    val state: StateFlow<DroneState> = _state.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private val sampleRate = 44100

    val noteNames = listOf("C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B")
    val octaves = listOf(2, 3, 4)

    init {
        updateFrequency()
    }

    private fun initAudioTrack() {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate / 2)

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
                .setBufferSizeInBytes(minBuf)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(tag, "AudioTrack initialization failed", e)
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

        _state.update { it.copy(isPlaying = true) }

        playbackJob = scope.launch {
            val bufferSize = 2048
            val pcm = ShortArray(bufferSize)
            var phaseRoot = 0.0
            var phaseFifth = 0.0

            while (isActive) {
                val currentState = _state.value
                val rootFreq = currentState.currentFrequency
                val fifthFreq = rootFreq * 1.5 // Pure perfect 5th (3:2 harmonic ratio)
                val hasFifth = currentState.isPureFifthEnabled
                val vol = currentState.volume

                val phaseIncRoot = 2.0 * PI * rootFreq / sampleRate
                val phaseIncFifth = 2.0 * PI * fifthFreq / sampleRate

                for (i in 0 until bufferSize) {
                    // Cello / Organ warmth: Root fundamental + gentle 2nd harmonic (octave) + 3rd harmonic (12th)
                    val sRoot = sin(phaseRoot) * 0.65 +
                            sin(phaseRoot * 2.0) * 0.22 +
                            sin(phaseRoot * 3.0) * 0.13

                    val sFifth = if (hasFifth) {
                        (sin(phaseFifth) * 0.55 + sin(phaseFifth * 2.0) * 0.2) * 0.55
                    } else 0.0

                    val combined = ((sRoot + sFifth) * vol * 26000.0).toInt().coerceIn(-32768, 32767)
                    pcm[i] = combined.toShort()

                    phaseRoot += phaseIncRoot
                    if (phaseRoot > 2.0 * PI) phaseRoot -= 2.0 * PI

                    if (hasFifth) {
                        phaseFifth += phaseIncFifth
                        if (phaseFifth > 2.0 * PI) phaseFifth -= 2.0 * PI
                    }
                }

                audioTrack?.write(pcm, 0, bufferSize)
            }
        }
    }

    fun stop() {
        playbackJob?.cancel()
        playbackJob = null
        _state.update { it.copy(isPlaying = false) }
    }

    fun selectNote(note: String) {
        _state.update { it.copy(noteName = note) }
        updateFrequency()
    }

    fun selectOctave(oct: Int) {
        _state.update { it.copy(octave = oct.coerceIn(2, 4)) }
        updateFrequency()
    }

    fun setBaseA4(freq: Double) {
        _state.update { it.copy(baseA4Freq = freq) }
        updateFrequency()
    }

    fun togglePureFifth() {
        _state.update { it.copy(isPureFifthEnabled = !it.isPureFifthEnabled) }
    }

    fun setVolume(vol: Float) {
        _state.update { it.copy(volume = vol.coerceIn(0f, 1f)) }
    }

    private fun updateFrequency() {
        val s = _state.value
        val noteIndex = noteNames.indexOf(s.noteName)
        if (noteIndex == -1) return

        // Semitones from A4 (A4 is note 9 in octave 4)
        val midiNote = (s.octave + 1) * 12 + noteIndex
        val a4Midi = 69 // A4
        val semitoneDiff = midiNote - a4Midi

        val freq = s.baseA4Freq * (2.0.pow(semitoneDiff / 12.0))
        _state.update { it.copy(currentFrequency = freq) }
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
