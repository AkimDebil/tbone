package com.example.data.settings

enum class AppTheme(val id: String) {
    CONCERT_DARK("concert_dark"),
    STUDIO_LIGHT("studio_light"),
    NEON_BRASS("neon_brass")
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    RU("ru", "Русский", "Русский"),
    EN("en", "English", "English"),
    ZH("zh", "Chinese", "中文")
}

data class UserSettings(
    val theme: AppTheme = AppTheme.CONCERT_DARK,
    val language: AppLanguage = AppLanguage.RU,
    val defaultInstrument: String = "Тромбон",
    val defaultTuningHz: Int = 442,
    val metronomeHaptic: Boolean = true,
    val metronomeVolume: Float = 0.9f
)
