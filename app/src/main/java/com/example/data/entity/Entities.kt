package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "practice_routine_items")
data class PracticeRoutineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val stageType: String, // WARM_UP, TECHNICAL, REPERTOIRE, SIGHT_READING, CUSTOM
    val title: String,
    val subtitle: String = "",
    val durationMinutes: Int = 15,
    val targetBpm: Int = 80,
    val timeSignature: String = "4/4",
    val sessionNotes: String = "",
    val isCompleted: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "saved_repertoire_items")
data class SavedRepertoireEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val composer: String,
    val instrument: String,
    val eraAndStyle: String,
    val status: String, // "Currently Learning", "Polishing", "Mastered"
    val technicalTipsJson: String,
    val recommendedListeningJson: String,
    val famousRecordingsJson: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val userNotes: String = ""
)

@Entity(tableName = "practice_logs")
data class PracticeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val stagesCompletedCount: Int,
    val routineTitle: String,
    val sessionNotes: String = ""
)
