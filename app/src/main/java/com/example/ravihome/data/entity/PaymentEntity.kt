package com.example.ravihome.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: PaymentCategory,
    val amount: Double,
    val dateMillis: Long,
    val notes: String?
)

enum class PaymentCategory {
    EB, RENT, INTERNET, MOBILE, GROCERIES, OTHER
}