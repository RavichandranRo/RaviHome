package com.example.ravihome.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ravihome.data.dao.WorkDao
import com.example.ravihome.data.repository.EbRepository
import com.example.ravihome.data.repository.WorkRepository
import com.example.ravihome.data.status.WorkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardSummary(
    val planned: Int,
    val completed: Int,
    val ebThisMonth: Double,
    val travelThisMonth: Double
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    workRepo: WorkRepository,
    ebRepo: EbRepository,
    travelRepo: TravelRepository
) : ViewModel() {

    val state = combine(
        workRepo.countByStatus(WorkStatus.PLANNED),
        workRepo.countByStatus(COMPLETED),
        ebRepo.monthlyTotal(start, end),
        travelRepo.monthlyTravel(start, end)
    ) { p, c, eb, t ->
        DashboardState(p, c, eb ?: 0.0, t ?: 0.0)
    }.stateIn(viewModelScope, SharingStarted.Lazily, DashboardState(0,0,0.0,0.0))
}