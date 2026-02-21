package com.example.ravihome.data.repository

import com.example.ravihome.BuildConfig
import com.example.ravihome.ui.travel.TravelStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

class TravelRepository @Inject constructor() {

    suspend fun fetchStatus(pnr: String): Result<TravelStatus> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.PNR_API_KEY
        val apiHost = BuildConfig.PNR_API_HOST
        val baseUrl = BuildConfig.PNR_API_BASE_URL

        if (apiKey.isBlank() || apiHost.isBlank() || baseUrl.isBlank()) {
            return@withContext Result.failure(IllegalStateException("PNR API key/host missing"))
        }

        val encodedPnr = URLEncoder.encode(pnr, Charsets.UTF_8.name())
        val url = URL("$baseUrl/getPNRStatus/$encodedPnr")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("X-RapidAPI-Key", apiKey)
            setRequestProperty("X-RapidAPI-Host", apiHost)
            setRequestProperty("Accept", "application/json")
            connectTimeout = 10000
            readTimeout = 10000
        }

        return@withContext try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (responseCode !in 200..299) {
                val trimmed = body.trim().take(200)
                val suffix = if (trimmed.isNotBlank()) ": $trimmed" else ""
                return@withContext Result.failure(IllegalStateException("API error: $responseCode$suffix"))
            }
            val json = JSONObject(body)
            val data = json.optJSONObject("data") ?: JSONObject()
            Result.success(parseStatus(pnr, data))
        } catch (ex: Exception) {
            Result.failure(ex)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseStatus(pnr: String, data: JSONObject): TravelStatus {
        val trainName = data.optString("trainName", "Train")
        val trainNumber = data.optString("trainNumber", "")
        val from = data.optString("from", data.optString("boardingPoint", ""))
        val to = data.optString("to", data.optString("reservationUpto", ""))
        val date = data.optString("journeyDate", "")
        val chartStatus = data.optString("chartStatus", "NOT PREPARED")
        val passengers = data.optJSONArray("passenger")
        val firstPassenger = passengers?.optJSONObject(0)
        val currentStatus = firstPassenger?.optString("currentStatus", "") ?: ""
        val bookingStatus = firstPassenger?.optString("bookingStatus", "") ?: ""
        val statusLabel = normalizeStatus(currentStatus.ifBlank { bookingStatus })
        val coach = data.optString("journeyClass", "")
        val seat = firstPassenger?.optString("seatNo", "") ?: ""

        return TravelStatus(
            pnr = pnr,
            trainName = listOfNotNull(
                trainName.takeIf { it.isNotBlank() },
                trainNumber.takeIf { it.isNotBlank() })
                .joinToString(" ")
                .trim(),
            from = from.ifBlank { "-" },
            to = to.ifBlank { "-" },
            date = date.ifBlank { "-" },
            status = statusLabel,
            coach = coach.ifBlank { "-" },
            seat = seat.ifBlank { "-" },
            chartPrepared = chartStatus.equals("PREPARED", ignoreCase = true)
        )
    }

    private fun normalizeStatus(value: String): String {
        val trimmed = value.uppercase()
        return when {
            trimmed.startsWith("CNF") -> "CONFIRMED"
            trimmed.startsWith("RAC") -> "RAC"
            trimmed.startsWith("WL") -> "WAITLIST"
            trimmed.isBlank() -> "UNKNOWN"
            else -> trimmed
        }
    }
}