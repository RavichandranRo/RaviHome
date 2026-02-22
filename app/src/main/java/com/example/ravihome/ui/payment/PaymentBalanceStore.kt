package com.example.ravihome.ui.payment

import android.content.Context
import java.time.LocalDate
import java.time.YearMonth

object PaymentBalanceStore {
    private const val PREF = "payment_balance_pref"
    private const val KEY_BALANCE = "current_balance"
    private const val KEY_MONTH = "month_tag"

    fun shouldPromptOpeningBalance(context: Context): Boolean {
        val now = LocalDate.now()
        if (now.dayOfMonth != 1) return false
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val monthTag = YearMonth.now().toString()
        return pref.getString(KEY_MONTH, "") != monthTag
    }

    fun setOpeningBalance(context: Context, value: Double) {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        pref.edit()
            .putFloat(KEY_BALANCE, value.toFloat())
            .putString(KEY_MONTH, YearMonth.now().toString())
            .apply()
    }

    fun getBalance(context: Context): Double {
        val pref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return pref.getFloat(KEY_BALANCE, 0f).toDouble()
    }

    fun applyCredit(context: Context, amount: Double) {
        setBalance(context, getBalance(context) + amount)
    }

    fun applyDebit(context: Context, amount: Double) {
        setBalance(context, getBalance(context) - amount)
    }

    private fun setBalance(context: Context, amount: Double) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_BALANCE, amount.toFloat())
            .apply()
    }
}
