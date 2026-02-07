package com.example.ravihome.ui.planned

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ravihome.data.entity.WorkEntity
import com.example.ravihome.data.repository.WorkRepository
import com.example.ravihome.data.status.WorkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class PlannedWorksViewModel @Inject constructor(
    private val repository: WorkRepository
) : ViewModel() {

    val plannedWorks = repository.plannedWorks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentPlannedWorks = repository.getRecentPlannedWorks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val searchKeyword = MutableStateFlow("")
    private val searchDate = MutableStateFlow<LocalDate?>(null)

    val filteredWorks = combine(searchKeyword, searchDate) { k, d ->
        k to d
    }.flatMapLatest { (k, d) ->
        repository.searchPlannedWorks(k, d)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setKeyword(keyword: String?) {
        searchKeyword.value = keyword ?.trim().orEmpty()
    }

    fun setDate(date: LocalDate?) {
        searchDate.value = date
    }

    fun addPlannedWork(title: String, date: LocalDate, desc: String) {
        viewModelScope.launch {
            repository.insert(
                WorkEntity(
                    title = title,
                    description = desc,
                    date = date,
                    status = WorkStatus.PLANNED,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun markCompleted(work: WorkEntity) {
        viewModelScope.launch {
            repository.markComplete(work)
        }
    }

    fun deleteWork(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }

    fun restoreWork(work: WorkEntity) {
        viewModelScope.launch {
            repository.insert(work.copy(id = 0))
        }
    }

}
