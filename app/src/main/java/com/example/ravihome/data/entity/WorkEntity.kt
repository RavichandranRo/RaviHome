package com.example.ravihome.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ravihome.data.status.WorkStatus
import java.time.LocalDate

@Entity(tableName = "works")
data class WorkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val date: LocalDate,
    val status: WorkStatus,
    val amount: Double? = null,
    val createdAt: Long
)