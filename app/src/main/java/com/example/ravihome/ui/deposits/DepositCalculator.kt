package com.example.ravihome.ui.deposits

object DepositCalculator {
    fun fixedMaturity(amount: Double, durationMonths: Int, rate: Double): Double {
        val years = durationMonths / 12.0
        return amount * (1 + (rate / 100) * years)
    }

    fun recurringMaturity(amount: Double, durationMonths: Int, rate: Double): Double {
        val n = durationMonths
        val monthlyRate = rate / 1200
        val interestFactor = n * (n + 1) / 2.0
        return (amount * n) + (amount * interestFactor * monthlyRate)
    }
}