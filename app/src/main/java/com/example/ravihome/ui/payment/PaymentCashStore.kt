package com.example.ravihome.ui.payment

import android.content.Context

object PaymentCashStore {
    private const val PREF = "payment_cash_pref"
    val denominations = listOf(2000, 500, 200, 100, 50, 20, 10, 5, 2, 1)

    fun getCounts(context: Context): Map<Int, Int> {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return denominations.associateWith { pref.getInt("denom_$it", 0) }
    }

    fun setCount(context: Context, denomination: Int, count: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt("denom_$denomination", count.coerceAtLeast(0))
            .apply()
    }

    fun setAllCounts(context: Context, counts: Map<Int, Int>) {
        val edit = context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
        denominations.forEach { d ->
            edit.putInt("denom_$d", counts[d]?.coerceAtLeast(0) ?: 0)
        }
        edit.apply()
    }

    fun addReceivedAmount(context: Context, amount: Int) {
        var pending = amount.coerceAtLeast(0)
        if (pending == 0) return
        val counts = getCounts(context).toMutableMap()
        for (denomination in denominations) {
            if (pending <= 0) break
            val pieces = pending / denomination
            if (pieces > 0) {
                counts[denomination] = (counts[denomination] ?: 0) + pieces
                pending %= denomination
            }
        }
        if (pending > 0) {
            counts[1] = (counts[1] ?: 0) + pending
        }
        setAllCounts(context, counts)
    }

    fun totalInHand(context: Context): Int {
        return getCounts(context).entries.sumOf { (d, c) -> d * c }
    }
}