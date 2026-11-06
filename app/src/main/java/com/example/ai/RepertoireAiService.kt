package com.example.ai

import android.util.Log
import com.example.data.model.FamousRecording
import com.example.data.model.RecommendedWork
import com.example.data.model.RepertoireAnalysis
import com.example.data.settings.AppLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "RepertoireAiService"

class RepertoireAiService {

    // Model configured with gemini-2.5-flash to prevent 503 high-demand errors
    private val model by lazy {
        try {
            val repertoireSchema = Schema.obj(
                properties = mapOf(
                    "pieceTitle" to Schema.string(description = "Piece Title"),
                    "composer" to Schema.string(description = "Composer Name"),
                    "instrument" to Schema.string(description = "Instrument"),
                    "eraAndStyle" to Schema.string(description = "Historical era, style, and character"),
                    "technicalTips" to Schema.array(
                        items = Schema.string(),
                        description = "Pedagogical tips: breathing, articulation, intonation, tempo"
                    ),
                    "recommendedListening" to Schema.array(
                        items = Schema.obj(
                            properties = mapOf(
                                "title" to Schema.string(description = "Work Title"),
                                "composer" to Schema.string(description = "Composer"),
                                "whyListen" to Schema.string(description = "Why helpful to listen")
                            )
                        ),
                        description = "4-5 recommended works of the same era"
                    ),
                    "famousRecordings" to Schema.array(
                        items = Schema.obj(
                            properties = mapOf(
                                "performer" to Schema.string(description = "Master soloist name"),
                                "orchestraOrAccompanist" to Schema.string(description = "Orchestra or accompanist"),
                                "highlight" to Schema.string(description = "Interpretation and tone qualities")
                            )
                        ),
                        description = "3 landmark recordings"
                    )
                )
            )

            val config = generationConfig {
                responseMimeType = "application/json"
                responseSchema = repertoireSchema
            }

            Firebase.ai.generativeModel(
                modelName = "gemini-2.5-flash",
                generationConfig = config
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Gemini model", e)
            null
        }
    }

    suspend fun analyzePiece(
        pieceQuery: String,
        instrument: String,
        language: AppLanguage = AppLanguage.RU
    ): RepertoireAnalysis {
        val trimmedQuery = pieceQuery.trim()

        // 1. INSTANT LOCAL PRESET CHECK (0ms latency, 100% offline ready)
        val preset = RepertoireKnowledgeBase.findPreset(trimmedQuery, language)
        if (preset != null) {
            return preset
        }

        // 2. REMOTE GEMINI INFERENCE WITH AUTOMATIC LOCAL FALLBACK
        try {
            val generativeModel = model
            if (generativeModel != null) {
                val langInstruction = when (language) {
                    AppLanguage.RU -> "Отвечайте на русском языке."
                    AppLanguage.EN -> "Respond in English."
                    AppLanguage.ZH -> "请用中文回答。"
                }

                val prompt = """
                    You are a distinguished classical musicologist and conservatory professor.
                    Analyze the following classical piece for the instrument "$instrument":
                    Piece / Composer: "$trimmedQuery"
                    $langInstruction
                    
                    Provide:
                    1. Era & Style: Historical context, musical period, and character.
                    2. Performance & Technical Tips: Articulation, breathing/bowing, intonation traps, style nuances.
                    3. Recommended Listening: 4-5 key reference works of the same era/composer.
                    4. 3 famous historical recordings with performers and distinctive qualities.
                """.trimIndent()

                val response = withTimeout(12_000L) {
                    generativeModel.generateContent(prompt)
                }
                val jsonText = response.text
                if (!jsonText.isNullOrBlank()) {
                    return parseAnalysisJson(jsonText, trimmedQuery, instrument, language)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Remote Gemini API unavailable or high demand. Gracefully routing to local musicology engine.", e)
        }

        // 3. ROCK-SOLID LOCAL FALLBACK: Never show error card, always return complete rich analysis
        return RepertoireKnowledgeBase.generateLocalAnalysis(trimmedQuery, instrument, language)
    }

    private fun parseAnalysisJson(
        jsonString: String,
        queryFallback: String,
        instrumentFallback: String,
        language: AppLanguage
    ): RepertoireAnalysis {
        val json = JSONObject(jsonString)
        val title = json.optString("pieceTitle", queryFallback)
        val composer = json.optString("composer", "Classical Master")
        val instrument = json.optString("instrument", instrumentFallback)
        val eraAndStyle = json.optString("eraAndStyle", "Classical Tradition")

        val tipsList = mutableListOf<String>()
        val tipsArray = json.optJSONArray("technicalTips")
        if (tipsArray != null) {
            for (i in 0 until tipsArray.length()) {
                tipsList.add(tipsArray.getString(i))
            }
        }

        val listeningList = mutableListOf<RecommendedWork>()
        val listeningArray = json.optJSONArray("recommendedListening")
        if (listeningArray != null) {
            for (i in 0 until listeningArray.length()) {
                val itemObj = listeningArray.getJSONObject(i)
                listeningList.add(
                    RecommendedWork(
                        title = itemObj.optString("title", "Related Work"),
                        composer = itemObj.optString("composer", composer),
                        whyListen = itemObj.optString("whyListen", "")
                    )
                )
            }
        }

        val recordingsList = mutableListOf<FamousRecording>()
        val recArray = json.optJSONArray("famousRecordings")
        if (recArray != null) {
            for (i in 0 until recArray.length()) {
                val recObj = recArray.getJSONObject(i)
                recordingsList.add(
                    FamousRecording(
                        performer = recObj.optString("performer", "Master Soloist"),
                        orchestraOrAccompanist = recObj.optString("orchestraOrAccompanist", "Philharmonic Orchestra"),
                        highlight = recObj.optString("highlight", "")
                    )
                )
            }
        }

        return RepertoireAnalysis(
            pieceTitle = title,
            composer = composer,
            instrument = instrument,
            eraAndStyle = eraAndStyle,
            technicalTips = if (tipsList.isNotEmpty()) tipsList else when (language) {
                AppLanguage.RU -> listOf("Контролируйте непрерывность дыхания.", "Отрабатывайте пассажи под метроном.")
                AppLanguage.EN -> listOf("Maintain continuous airflow support.", "Practice challenging passages with metronome subdivision.")
                AppLanguage.ZH -> listOf("保持气息连贯稳定。", "配合节拍器慢速细分打磨技术难点。")
            },
            recommendedListening = listeningList,
            famousRecordings = recordingsList
        )
    }
}
