package com.example.ravihome.data.converter

import androidx.room.TypeConverter
import java.time.LocalDate

class LocalDateConverter {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long): LocalDate = LocalDate.ofEpochDay(value)
}