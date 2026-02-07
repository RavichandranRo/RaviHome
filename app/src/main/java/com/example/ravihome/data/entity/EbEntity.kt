package com.example.ravihome.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eb_readings")
data class EbEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val units: Int,
    val amount: Double,
    val readingDate: Long,
    val paid: Boolean
)