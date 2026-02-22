package com.example.ravihome.data.repository

import com.example.ravihome.BuildConfig
import com.example.ravihome.data.network.PnrApiService
import com.example.ravihome.data.network.PnrData
import com.example.ravihome.data.network.PnrPassenger
import com.example.ravihome.ui.travel.TravelStatus
import javax.inject.Inject

class TravelRepository @Inject constructor(
    private val pnrApiService: PnrApiService
) {

    suspend fun fetchStatus(pnr: String): Result<TravelStatus> {
        val apiKey = BuildConfig.PNR_API_KEY
        val apiHost = BuildConfig.PNR_API_HOST

        if (apiKey.isBlank() || apiHost.isBlank()) {
            return Result.failure(IllegalStateException("PNR API key/host missing"))
        }

        return runCatching {
            val response = pnrApiService.getPnrStatus(pnr, apiKey, apiHost)
            if (!response.isSuccessful) {
                throw IllegalStateException("API error: ${response.code()}")
            }
            val payload = response.body()?.data
                ?: throw IllegalStateException("Empty PNR response")
            parseStatus(pnr, payload)
        }
    }

    private fun parseStatus(pnr: String, data: PnrData): TravelStatus {
        val passenger = data.passengerList?.firstOrNull() ?: PnrPassenger()
        val currentStatusRaw = passenger.currentStatus.orEmpty().ifBlank {
            passenger.currentStatusDetails.orEmpty().ifBlank { passenger.bookingStatus.orEmpty() }
        }
        val statusLabel = normalizeStatus(currentStatusRaw)

        val coach = passenger.currentCoachId
            ?: passenger.bookingCoachId
            ?: data.journeyClass
            ?: "-"

        val seatNo = passenger.currentBerthNo ?: passenger.bookingBerthNo
        val berthCode = passenger.currentBerthCode ?: passenger.bookingBerthCode
        val seat = listOfNotNull(seatNo?.toString(), berthCode).joinToString("/").ifBlank { "-" }

        val berthType = when (berthCode?.uppercase()) {
            "SL" -> "Side Lower"
            "SU" -> "Side Upper"
            "LB" -> "Lower"
            "MB" -> "Middle"
            "UB" -> "Upper"
            else -> berthCode ?: "-"
        }

        val from = data.sourceStation ?: data.boardingPoint ?: "-"
        val to = data.destinationStation ?: data.reservationUpto ?: "-"

        return TravelStatus(
            pnr = data.pnrNumber ?: pnr,
            trainName = listOfNotNull(data.trainName, data.trainNumber)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { "Train" },
            from = from,
            to = to,
            date = data.dateOfJourney ?: "-",
            status = statusLabel,
            coach = coach,
            seat = seat,
            chartPrepared = data.chartStatus.orEmpty().contains("prepared", ignoreCase = true),
            departureTime = data.dateOfJourney ?: "-",
            arrivalTime = data.arrivalDate ?: "-",
            fare = data.ticketFare ?: data.bookingFare ?: "-",
            berthType = berthType
        )
    }

    private fun normalizeStatus(value: String): String {
        val trimmed = value.uppercase()
        return when {
            trimmed.startsWith("CNF") -> "CONFIRMED"
            trimmed.startsWith("RAC") -> "RAC"
            trimmed.startsWith("WL") || trimmed.contains("WAIT") -> "WAITLIST"
            trimmed.isBlank() -> "UNKNOWN"
            else -> trimmed
        }
    }
}