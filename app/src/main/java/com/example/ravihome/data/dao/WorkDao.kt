package com.example.ravihome.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.ravihome.data.entity.WorkEntity
import com.example.ravihome.data.status.WorkStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WorkDao {

    @Insert
    suspend fun insert(work: WorkEntity)

    @Update
    suspend fun updateWork(work: WorkEntity)

    @Query("DELETE FROM works WHERE id=:id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM works WHERE status = 'PLANNED' ORDER BY createdAt DESC")
    fun plannedWorks(): Flow<List<WorkEntity>>

    @Query("SELECT * FROM works WHERE status = 'COMPLETED' ORDER BY createdAt DESC")
    fun completedWorks(): Flow<List<WorkEntity>>

    @Query("SELECT * FROM works WHERE status = 'PLANNED' ORDER BY createdAt DESC LIMIT 3")
    fun getRecentPlannedWorks(): Flow<List<WorkEntity>>

    @Query("UPDATE works SET status = 'COMPLETED' WHERE id = :workId")
    suspend fun markAsCompleted(workId: Long)

    @Query(
        """
    SELECT * FROM works
    WHERE status = :status
    AND (:keyword IS NULL OR title LIKE '%' || :keyword || '%')
    AND (:date IS NULL OR date = :date)
    ORDER BY createdAt DESC
"""
    )
    fun searchWorks(
        status: WorkStatus,
        keyword: String?,
        date: LocalDate?
    ): Flow<List<WorkEntity>>

    @Query("SELECT COUNT(*) FROM works WHERE status = :status")
    fun countByStatus(status: WorkStatus): Flow<Int>
}