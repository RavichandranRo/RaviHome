package com.example.ravihome.data.converter
import androidx.room.TypeConverter
import com.example.ravihome.data.status.WorkStatus

class WorkStatusConverter {
    @TypeConverter
    fun fromStatus(status: WorkStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): WorkStatus = WorkStatus.valueOf(value)
}