package com.example.ravihome.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.ravihome.data.entity.TravelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelDao {
    @Query("SELECT * FROM travel ORDER BY travelDate DESC")
    fun allTravel(): Flow<List<TravelEntity>>

    @Query(
        """
    SELECT SUM(amount) FROM travel
    WHERE travelDate BETWEEN :start AND :end
"""
    )
    fun monthlyTravel(start: Long, end: Long): Flow<Double?>
}