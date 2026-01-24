package com.example.ravihome.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ravihome.data.dao.WorkDao
import com.example.ravihome.data.status.WorkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardSummary(
    val planned: Int,
    val completed: Int
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    dao: WorkDao
) : ViewModel() {

    val summary = combine(
        dao.countByStatus(WorkStatus.PLANNED),
        dao.countByStatus(WorkStatus.COMPLETED)
    ) { planned, completed ->
        DashboardSummary(planned, completed)
    }.stateIn(viewModelScope, SharingStarted.Lazily, DashboardSummary(0, 0))
}
