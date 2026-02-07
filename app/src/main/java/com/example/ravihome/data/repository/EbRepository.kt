package com.example.ravihome.data.repository

import com.example.ravihome.data.dao.EbDao
import com.example.ravihome.data.entity.EbEntity
import javax.inject.Inject

class EbRepository @Inject constructor(
    private val dao: EbDao
) {
    fun readings() = dao.allReadings()
    fun monthlyTotal(start: Long, end: Long) = dao.monthlyTotal(start, end)
    suspend fun add(units: Int, amount: Double) {
        dao.insert(
            EbEntity(
                units = units,
                amount = amount,
                readingDate = System.currentTimeMillis(),
                paid = true
            )
        )
    }
}