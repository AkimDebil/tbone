package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.PracticeLogEntity
import com.example.data.entity.PracticeRoutineEntity
import com.example.data.entity.SavedRepertoireEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM practice_routine_items ORDER BY orderIndex ASC")
    fun getAllRoutineItems(): Flow<List<PracticeRoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineItem(item: PracticeRoutineEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PracticeRoutineEntity>)

    @Update
    suspend fun updateRoutineItem(item: PracticeRoutineEntity)

    @Delete
    suspend fun deleteRoutineItem(item: PracticeRoutineEntity)

    @Query("UPDATE practice_routine_items SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    @Query("UPDATE practice_routine_items SET sessionNotes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)

    @Query("UPDATE practice_routine_items SET isCompleted = 0")
    suspend fun resetAllCompletions()

    @Query("DELETE FROM practice_routine_items")
    suspend fun clearRoutine()
}

@Dao
interface RepertoireDao {
    @Query("SELECT * FROM saved_repertoire_items ORDER BY dateAdded DESC")
    fun getAllRepertoire(): Flow<List<SavedRepertoireEntity>>

    @Query("SELECT * FROM saved_repertoire_items WHERE status = :status ORDER BY dateAdded DESC")
    fun getRepertoireByStatus(status: String): Flow<List<SavedRepertoireEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPiece(piece: SavedRepertoireEntity): Long

    @Update
    suspend fun updatePiece(piece: SavedRepertoireEntity)

    @Delete
    suspend fun deletePiece(piece: SavedRepertoireEntity)

    @Query("UPDATE saved_repertoire_items SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String)

    @Query("SELECT COUNT(*) FROM saved_repertoire_items")
    suspend fun getRepertoireCount(): Int
}

@Dao
interface PracticeLogDao {
    @Query("SELECT * FROM practice_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<PracticeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: PracticeLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<PracticeLogEntity>)

    @Query("SELECT SUM(durationMinutes) FROM practice_logs WHERE timestamp >= :sinceTimestamp")
    fun getTotalMinutesSince(sinceTimestamp: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM practice_logs")
    suspend fun getLogCount(): Int
}
