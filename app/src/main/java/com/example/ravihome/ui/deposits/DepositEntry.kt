package com.example.ravihome.ui.deposits

data class DepositEntry(
    val id: Long,
    val depositNumber: String,
    val amount: Double,
    val startDateMillis: Long,
    val maturityDateMillis: Long,
    val rate: Double,
    val bank: String,
    val isPremature: Boolean,
    val createdAt: Long
)