package com.example.ravihome.data.repository

import com.example.ravihome.data.dao.TravelDao
import com.example.ravihome.data.entity.TravelEntity
import com.example.ravihome.data.entity.TravelMode
import javax.inject.Inject

class TravelRepository @Inject constructor(
    private val dao: TravelDao
) {
    fun travels() = dao.allTravel()

    fun monthlyTotal(start: Long, end: Long) =
        dao.monthlyTravel(start, end)

    suspend fun add(
        mode: TravelMode,
        from: String,
        to: String,
        amount: Double,
        dateMillis: Long
    ) {
        dao.insert(
            TravelEntity(
                mode = mode,
                fromPlace = from,
                toPlace = to,
                amount = amount,
                travelDate = dateMillis
            )
        )
    }
}