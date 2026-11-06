package com.example.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSettingsManager(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private fun loadSettings(): UserSettings {
        val themeId = prefs.getString(KEY_THEME, AppTheme.CONCERT_DARK.id) ?: AppTheme.CONCERT_DARK.id
        val theme = AppTheme.entries.firstOrNull { it.id == themeId } ?: AppTheme.CONCERT_DARK

        val langCode = prefs.getString(KEY_LANGUAGE, AppLanguage.RU.code) ?: AppLanguage.RU.code
        val language = AppLanguage.entries.firstOrNull { it.code == langCode } ?: AppLanguage.RU

        val instrument = prefs.getString(KEY_INSTRUMENT, "Тромбон") ?: "Тромбон"
        val tuning = prefs.getInt(KEY_TUNING_HZ, 442)
        val haptic = prefs.getBoolean(KEY_METRONOME_HAPTIC, true)
        val volume = prefs.getFloat(KEY_METRONOME_VOLUME, 0.9f)

        return UserSettings(
            theme = theme,
            language = language,
            defaultInstrument = instrument,
            defaultTuningHz = tuning,
            metronomeHaptic = haptic,
            metronomeVolume = volume
        )
    }

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.id).apply()
        _settings.value = _settings.value.copy(theme = theme)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _settings.value = _settings.value.copy(language = language)
    }

    fun setDefaultInstrument(instrument: String) {
        prefs.edit().putString(KEY_INSTRUMENT, instrument).apply()
        _settings.value = _settings.value.copy(defaultInstrument = instrument)
    }

    fun setDefaultTuningHz(hz: Int) {
        prefs.edit().putInt(KEY_TUNING_HZ, hz).apply()
        _settings.value = _settings.value.copy(defaultTuningHz = hz)
    }

    fun setMetronomeHaptic(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_METRONOME_HAPTIC, enabled).apply()
        _settings.value = _settings.value.copy(metronomeHaptic = enabled)
    }

    fun setMetronomeVolume(volume: Float) {
        prefs.edit().putFloat(KEY_METRONOME_VOLUME, volume).apply()
        _settings.value = _settings.value.copy(metronomeVolume = volume)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = UserSettings()
    }

    companion object {
        private const val PREFS_NAME = "virtuoso_settings_prefs"
        private const val KEY_THEME = "key_theme"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_INSTRUMENT = "key_instrument"
        private const val KEY_TUNING_HZ = "key_tuning_hz"
        private const val KEY_METRONOME_HAPTIC = "key_metronome_haptic"
        private const val KEY_METRONOME_VOLUME = "key_metronome_volume"

        @Volatile
        private var INSTANCE: UserSettingsManager? = null

        fun getInstance(context: Context): UserSettingsManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserSettingsManager(context).also { INSTANCE = it }
            }
        }
    }
}
