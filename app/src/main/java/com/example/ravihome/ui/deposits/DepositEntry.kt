package com.example.ravihome.ui.deposits

data class DepositEntry(
    val id: Long,
    val amount: Double,
    val durationMonths: Int,
    val rate: Double,
    val bank: String,
    val createdAt: Long
)