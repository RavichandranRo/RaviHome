package com.example.ravihome.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.ravihome.data.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Insert
    suspend fun insert(payment: PaymentEntity)

    @Delete
    suspend fun delete(payment: PaymentEntity)

    @Query("SELECT * FROM payments ORDER BY dateMillis DESC")
    fun allPayments(): Flow<List<PaymentEntity>>

    @Query("""
        SELECT SUM(amount) FROM payments
        WHERE dateMillis BETWEEN :start AND :end
    """)
    fun monthlyTotal(start: Long, end: Long): Flow<Double?>
}
