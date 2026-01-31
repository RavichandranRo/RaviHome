package com.example.ravihome.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.ravihome.data.dao.WorkDao
import com.example.ravihome.data.entity.WorkEntity
import com.example.ravihome.data.status.WorkStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class WorkRepository @Inject constructor(
    private val dao: WorkDao
) {
    fun plannedWorks() = dao.plannedWorks()
    fun getRecentPlannedWorks() = dao.getRecentPlannedWorks()
    fun completedWorks() = dao.completedWorks()
    suspend fun insert(work: WorkEntity) = dao.insert(work)
    suspend fun delete(id: Long) = dao.deleteById(id)

    suspend fun update(work: WorkEntity) = dao.updateWork(work)

    suspend fun markComplete(work: WorkEntity) = dao.markAsCompleted(work.id)

    fun recentPlanned(): Flow<List<WorkEntity>> = dao.getRecentPlannedWorks()

    fun searchPlannedWorks(keyword: String?, date: String?) =
        dao.searchWorks(WorkStatus.PLANNED, keyword, date)

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addEbPayment(amount: Double) {
        insert(
            WorkEntity(
                title = "EB Bill Paid",
                description = "Auto generated",
                date = LocalDate.now().toString(),
                status = WorkStatus.COMPLETED,
                amount = amount,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
