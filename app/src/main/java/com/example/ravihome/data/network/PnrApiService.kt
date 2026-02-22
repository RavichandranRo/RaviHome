package com.example.ravihome.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface PnrApiService {
    @GET("getPNRStatus/{pnr}")
    suspend fun getPnrStatus(
        @Path("pnr") pnr: String,
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") apiHost: String
    ): Response<PnrResponse>
}

data class PnrResponse(
    val success: Boolean? = null,
    val data: PnrData? = null
)

data class PnrData(
    val pnrNumber: String? = null,
    val dateOfJourney: String? = null,
    val trainNumber: String? = null,
    val trainName: String? = null,
    val sourceStation: String? = null,
    val destinationStation: String? = null,
    val reservationUpto: String? = null,
    val boardingPoint: String? = null,
    val journeyClass: String? = null,
    val chartStatus: String? = null,
    val passengerList: List<PnrPassenger>? = null,
    val departureTime: String? = null,
    val arrivalDate: String? = null,
    val bookingFare: String? = null,
    val ticketFare: String? = null,
    val quota: String? = null,
    val isWL: String? = null
)

data class PnrPassenger(
    val bookingStatus: String? = null,
    val currentStatus: String? = null,
    val currentCoachId: String? = null,
    val currentBerthNo: Int? = null,
    val currentBerthCode: String? = null,
    val currentStatusDetails: String? = null,
    val bookingCoachId: String? = null,
    val bookingBerthNo: Int? = null,
    val bookingBerthCode: String? = null
)
