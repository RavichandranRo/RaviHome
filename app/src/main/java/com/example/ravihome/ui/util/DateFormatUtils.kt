package com.example.ravihome.ui.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatUtils {
    private val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    private val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    fun parseInput(value: String): LocalDate? =
        runCatching { LocalDate.parse(value, inputFormatter) }.getOrNull()

    fun formatInput(date: LocalDate): String = date.format(inputFormatter)

    fun formatDisplay(date: LocalDate): String = date.format(displayFormatter)
}