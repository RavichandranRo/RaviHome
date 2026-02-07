package com.example.ravihome.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel")
data class TravelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: TravelMode,
    val fromPlace: String,
    val toPlace: String,
    val travelDate: Long,
    val amount: Double
)

enum class TravelMode { TRAIN, BUS, AUTO, BIKE }