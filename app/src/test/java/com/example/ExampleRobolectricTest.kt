package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.RepertoireKnowledgeBase
import com.example.data.model.FamousRecording
import com.example.data.model.RecommendedWork
import com.example.data.settings.AppLanguage
import com.example.data.settings.AppTheme
import com.example.data.settings.UserSettingsManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Virtuoso", appName)
    }

    @Test
    fun `verify dynamic youtube search urls for listening and recordings`() {
        val work = RecommendedWork(
            title = "Romanza Appassionata",
            composer = "Carl Maria von Weber",
            whyListen = "Romantic lyricism"
        )
        val url = work.getYouTubeSearchUrl()
        assertTrue("URL must start with youtube results search query", url.startsWith("https://www.youtube.com/results?search_query="))
        assertTrue("URL must contain encoded composer", url.contains("Carl+Maria+von+Weber") || url.contains("Weber"))
        assertTrue("URL must contain classical performance query", url.contains("classical+performance"))

        val rec = FamousRecording(
            performer = "Christian Lindberg",
            orchestraOrAccompanist = "Bamberg Symphony",
            highlight = "Iconic interpretation"
        )
        val recUrl = rec.getYouTubeSearchUrl("Ferdinand David", "Concertino Op. 4")
        assertTrue("Recording URL must be a valid youtube search url", recUrl.startsWith("https://www.youtube.com/results?search_query="))
        assertTrue("Recording URL must contain soloist name", recUrl.contains("Christian+Lindberg"))
    }

    @Test
    fun `verify user settings persistence and multi-theme switcher`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val settingsManager = UserSettingsManager(context)

        // Default state
        val initial = settingsManager.settings.value
        assertEquals(AppTheme.CONCERT_DARK, initial.theme)
        assertEquals(AppLanguage.RU, initial.language)

        // Switch theme
        settingsManager.setTheme(AppTheme.STUDIO_LIGHT)
        assertEquals(AppTheme.STUDIO_LIGHT, settingsManager.settings.value.theme)

        settingsManager.setTheme(AppTheme.NEON_BRASS)
        assertEquals(AppTheme.NEON_BRASS, settingsManager.settings.value.theme)

        // Switch language
        settingsManager.setLanguage(AppLanguage.EN)
        assertEquals(AppLanguage.EN, settingsManager.settings.value.language)

        settingsManager.setLanguage(AppLanguage.ZH)
        assertEquals(AppLanguage.ZH, settingsManager.settings.value.language)

        // Tuning standard
        settingsManager.setDefaultTuningHz(440)
        assertEquals(440, settingsManager.settings.value.defaultTuningHz)
    }

    @Test
    fun `verify hybrid offline-first masterworks catalog lookups across languages`() {
        // Preset lookup for Bach in English
        val bachEn = RepertoireKnowledgeBase.findPreset("Bach", AppLanguage.EN)
        assertNotNull("Bach preset must exist", bachEn)
        assertTrue(bachEn!!.composer.contains("Bach"))
        assertTrue(bachEn.pieceTitle.contains("Cello"))

        // Preset lookup for Tchaikovsky in Chinese
        val tchaikZh = RepertoireKnowledgeBase.findPreset("柴可夫斯基", AppLanguage.ZH)
        assertNotNull("Tchaikovsky Chinese preset must exist", tchaikZh)
        assertTrue(tchaikZh!!.pieceTitle.contains("小提琴"))

        // Preset lookup for David in Russian
        val davidRu = RepertoireKnowledgeBase.findPreset("Давид", AppLanguage.RU)
        assertNotNull("David Russian preset must exist", davidRu)
        assertTrue(davidRu!!.composer.contains("Давид"))
    }

    @Test
    fun `verify algorithmic local fallback resilience for rare or custom search`() {
        val custom = RepertoireKnowledgeBase.generateLocalAnalysis(
            query = "Krysztof Penderecki - Capriccio",
            instrument = "Tuba",
            language = AppLanguage.EN
        )
        assertNotNull(custom)
        assertEquals("Capriccio", custom.pieceTitle)
        assertEquals("Krysztof Penderecki", custom.composer)
        assertEquals("Tuba", custom.instrument)
        assertTrue(custom.technicalTips.isNotEmpty())
        assertTrue(custom.recommendedListening.isNotEmpty())
        assertTrue(custom.famousRecordings.isNotEmpty())
    }
}
