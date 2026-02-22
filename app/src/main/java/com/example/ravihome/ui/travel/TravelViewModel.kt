package com.example.ravihome.ui.travel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ravihome.data.repository.TravelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TravelViewModel @Inject constructor(
    private val repository: TravelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelUiState())
    val uiState: StateFlow<TravelUiState> = _uiState

    fun fetchPnrStatus(pnr: String) {
        if (pnr.length != 10 || pnr.any { !it.isDigit() }) {
            _uiState.update { it.copy(message = "Enter a valid 10-digit PNR", isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, message = null) }
        viewModelScope.launch {
            val result = repository.fetchStatus(pnr)
            _uiState.update {
                result.fold(
                    onSuccess = { status ->
                        val updatedHistory = (it.history + status).takeLast(5)
                        it.copy(
                            isLoading = false,
                            status = status,
                            message = null,
                            history = updatedHistory,
                            stats = buildStats(updatedHistory)
                        )
                    },
                    onFailure = { error ->
                        it.copy(
                            isLoading = false,
                            message = error.message ?: "Unable to fetch PNR status"
                        )
                    }
                )
            }
        }
    }

    private fun buildStats(history: List<TravelStatus>): TravelStats {
        val total = history.size
        val confirmed = history.count { it.status == "CONFIRMED" }
        val waitlist = history.count { it.status == "WAITLIST" }
        return TravelStats(total, confirmed, waitlist)
    }
}


data class TravelUiState(
    val isLoading: Boolean = false,
    val status: TravelStatus? = null,
    val message: String? = null,
    val history: List<TravelStatus> = emptyList(),
    val stats: TravelStats = TravelStats()
)

data class TravelStatus(
    val pnr: String,
    val trainName: String,
    val from: String,
    val to: String,
    val date: String,
    val status: String,
    val coach: String,
    val seat: String,
    val chartPrepared: Boolean,
    val departureTime: String = "-",
    val arrivalTime: String = "-",
    val fare: String = "-",
    val berthType: String = "-"
)

data class TravelStats(
    val totalChecks: Int = 0,
    val confirmed: Int = 0,
    val waitlist: Int = 0
)