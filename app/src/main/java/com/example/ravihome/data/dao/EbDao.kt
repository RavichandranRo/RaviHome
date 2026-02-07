package com.example.ravihome.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.ravihome.data.entity.EbEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EbDao {

    @Insert
    suspend fun insert(eb: EbEntity)

    @Query("SELECT * FROM eb_readings ORDER BY readingDate DESC")
    fun allReadings(): Flow<List<EbEntity>>

    @Query("""
        SELECT SUM(amount) FROM eb_readings
        WHERE readingDate BETWEEN :start AND :end
    """)
    fun monthlyTotal(start: Long, end: Long): Flow<Double?>
}
