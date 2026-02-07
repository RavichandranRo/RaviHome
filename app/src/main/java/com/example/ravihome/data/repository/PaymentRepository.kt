package com.example.ravihome.data.repository

import com.example.ravihome.data.dao.PaymentDao
import com.example.ravihome.data.entity.PaymentCategory
import com.example.ravihome.data.entity.PaymentEntity
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    private val dao: PaymentDao
) {
    fun payments() = dao.allPayments()

    fun monthlyTotal(start: Long, end: Long) =
        dao.monthlyTotal(start, end)

    suspend fun add(
        category: PaymentCategory,
        amount: Double,
        notes: String?
    ) {
        dao.insert(
            PaymentEntity(
                category = category,
                amount = amount,
                dateMillis = System.currentTimeMillis(),
                notes = notes
            )
        )
    }
}