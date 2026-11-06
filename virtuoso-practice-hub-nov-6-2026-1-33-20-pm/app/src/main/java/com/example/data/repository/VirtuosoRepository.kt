package com.example.data.repository

import com.example.data.VirtuosoDatabase
import com.example.data.entity.PracticeLogEntity
import com.example.data.entity.PracticeRoutineEntity
import com.example.data.entity.SavedRepertoireEntity
import com.example.data.model.FamousRecording
import com.example.data.model.RecommendedWork
import com.example.data.model.RepertoireAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

data class PracticeStats(
    val totalMinutesThisWeek: Int,
    val streakDays: Int,
    val totalSessions: Int,
    val learningCount: Int,
    val polishingCount: Int,
    val masteredCount: Int
)

class VirtuosoRepository(private val database: VirtuosoDatabase) {
    private val routineDao = database.routineDao()
    private val repertoireDao = database.repertoireDao()
    private val logDao = database.practiceLogDao()

    val routineItems: Flow<List<PracticeRoutineEntity>> = routineDao.getAllRoutineItems()
    val savedRepertoire: Flow<List<SavedRepertoireEntity>> = repertoireDao.getAllRepertoire()
    val practiceLogs: Flow<List<PracticeLogEntity>> = logDao.getAllLogs()

    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        routineDao.updateCompletionStatus(id, isCompleted)
    }

    suspend fun updateRoutineNotes(id: Long, notes: String) = withContext(Dispatchers.IO) {
        routineDao.updateNotes(id, notes)
    }

    suspend fun addRoutineItem(item: PracticeRoutineEntity) = withContext(Dispatchers.IO) {
        routineDao.insertRoutineItem(item)
    }

    suspend fun updateRoutineItem(item: PracticeRoutineEntity) = withContext(Dispatchers.IO) {
        routineDao.updateRoutineItem(item)
    }

    suspend fun deleteRoutineItem(item: PracticeRoutineEntity) = withContext(Dispatchers.IO) {
        routineDao.deleteRoutineItem(item)
    }

    suspend fun resetRoutineCompletions() = withContext(Dispatchers.IO) {
        routineDao.resetAllCompletions()
    }

    suspend fun restoreDefaultRoutine() = withContext(Dispatchers.IO) {
        routineDao.clearRoutine()
        VirtuosoDatabase.seedDefaultData(database)
    }

    suspend fun savePieceToRepertoire(
        analysis: RepertoireAnalysis,
        status: String = "Currently Learning",
        userNotes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val tipsArray = JSONArray().apply {
            analysis.technicalTips.forEach { put(it) }
        }
        val listeningArray = JSONArray().apply {
            analysis.recommendedListening.forEach { work ->
                put(JSONObject().apply {
                    put("title", work.title)
                    put("composer", work.composer)
                    put("whyListen", work.whyListen)
                })
            }
        }
        val recordingsArray = JSONArray().apply {
            analysis.famousRecordings.forEach { rec ->
                put(JSONObject().apply {
                    put("performer", rec.performer)
                    put("orchestraOrAccompanist", rec.orchestraOrAccompanist)
                    put("highlight", rec.highlight)
                })
            }
        }

        val entity = SavedRepertoireEntity(
            title = analysis.pieceTitle,
            composer = analysis.composer,
            instrument = analysis.instrument,
            eraAndStyle = analysis.eraAndStyle,
            status = status,
            technicalTipsJson = tipsArray.toString(),
            recommendedListeningJson = listeningArray.toString(),
            famousRecordingsJson = recordingsArray.toString(),
            userNotes = userNotes
        )
        repertoireDao.insertPiece(entity)
    }

    suspend fun updateRepertoireStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        repertoireDao.updateStatus(id, status)
    }

    suspend fun deleteRepertoire(item: SavedRepertoireEntity) = withContext(Dispatchers.IO) {
        repertoireDao.deletePiece(item)
    }

    suspend fun logCompletedSession(
        durationMinutes: Int,
        stagesCompletedCount: Int,
        routineTitle: String,
        sessionNotes: String
    ): Long = withContext(Dispatchers.IO) {
        val entity = PracticeLogEntity(
            durationMinutes = durationMinutes,
            stagesCompletedCount = stagesCompletedCount,
            routineTitle = routineTitle,
            sessionNotes = sessionNotes
        )
        logDao.insertLog(entity)
    }

    val practiceStats: Flow<PracticeStats> = combine(practiceLogs, savedRepertoire) { logs, repertoire ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val weekStart = cal.timeInMillis

        val totalMinutesThisWeek = logs
            .filter { it.timestamp >= weekStart }
            .sumOf { it.durationMinutes }

        val streak = calculateDayStreak(logs)

        val learning = repertoire.count { it.status == "Currently Learning" || it.status == "В работе (Учу)" || it.status == "正在练习" }
        val polishing = repertoire.count { it.status == "Polishing" || it.status == "Шлифовка" || it.status == "精细打磨" }
        val mastered = repertoire.count { it.status == "Mastered" || it.status == "Выучено (Мастер)" || it.status == "已经掌握" }

        PracticeStats(
            totalMinutesThisWeek = totalMinutesThisWeek,
            streakDays = streak,
            totalSessions = logs.size,
            learningCount = learning,
            polishingCount = polishing,
            masteredCount = mastered
        )
    }

    private fun calculateDayStreak(logs: List<PracticeLogEntity>): Int {
        if (logs.isEmpty()) return 0

        val cal = Calendar.getInstance()
        val todayYear = cal.get(Calendar.YEAR)
        val todayDayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val sessionDays = logs.map { log ->
            val c = Calendar.getInstance().apply { timeInMillis = log.timestamp }
            "${c.get(Calendar.YEAR)}-${c.get(Calendar.DAY_OF_YEAR)}"
        }.toSet()

        var streak = 0
        var checkCal = Calendar.getInstance()

        // If no session today, check if yesterday had one to maintain the streak
        val todayKey = "$todayYear-$todayDayOfYear"
        if (!sessionDays.contains(todayKey)) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }

        while (true) {
            val key = "${checkCal.get(Calendar.YEAR)}-${checkCal.get(Calendar.DAY_OF_YEAR)}"
            if (sessionDays.contains(key)) {
                streak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }

    companion object {
        fun parseRepertoireAnalysis(entity: SavedRepertoireEntity): RepertoireAnalysis {
            val tips = mutableListOf<String>()
            try {
                val array = JSONArray(entity.technicalTipsJson)
                for (i in 0 until array.length()) {
                    tips.add(array.getString(i))
                }
            } catch (_: Exception) {}

            val listening = mutableListOf<RecommendedWork>()
            try {
                val array = JSONArray(entity.recommendedListeningJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    listening.add(
                        RecommendedWork(
                            title = obj.getString("title"),
                            composer = obj.getString("composer"),
                            whyListen = obj.optString("whyListen", "")
                        )
                    )
                }
            } catch (_: Exception) {}

            val recordings = mutableListOf<FamousRecording>()
            try {
                val array = JSONArray(entity.famousRecordingsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    recordings.add(
                        FamousRecording(
                            performer = obj.getString("performer"),
                            orchestraOrAccompanist = obj.optString("orchestraOrAccompanist", ""),
                            highlight = obj.optString("highlight", "")
                        )
                    )
                }
            } catch (_: Exception) {}

            return RepertoireAnalysis(
                pieceTitle = entity.title,
                composer = entity.composer,
                instrument = entity.instrument,
                eraAndStyle = entity.eraAndStyle,
                technicalTips = tips,
                recommendedListening = listening,
                famousRecordings = recordings
            )
        }
    }
}
