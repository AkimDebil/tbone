package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.settings.AppLanguage
import com.example.data.settings.AppTheme
import com.example.data.settings.UserSettings
import com.example.data.settings.UserSettingsManager
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = UserSettingsManager.getInstance(application)
    val settings: StateFlow<UserSettings> = settingsManager.settings

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

    fun setTheme(theme: AppTheme) {
        settingsManager.setTheme(theme)
    }

    fun setLanguage(language: AppLanguage) {
        settingsManager.setLanguage(language)
    }

    fun setDefaultInstrument(instrument: String) {
        settingsManager.setDefaultInstrument(instrument)
    }

    fun setDefaultTuningHz(hz: Int) {
        settingsManager.setDefaultTuningHz(hz)
    }

    fun setMetronomeHaptic(enabled: Boolean) {
        settingsManager.setMetronomeHaptic(enabled)
    }

    fun setMetronomeVolume(volume: Float) {
        settingsManager.setMetronomeVolume(volume)
    }

    fun resetToDefaults() {
        settingsManager.resetToDefaults()
    }
}
