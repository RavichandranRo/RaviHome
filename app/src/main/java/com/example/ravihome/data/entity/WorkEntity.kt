package com.example.ravihome.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ravihome.data.status.WorkStatus

@Entity(tableName = "works")
data class WorkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val date: String,
    val status: WorkStatus,
    val amount: Double? = null,
    val createdAt: Long
)