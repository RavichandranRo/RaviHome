package com.example.ravihome.ui.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ravihome.data.repository.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompletedWorksViewModel @Inject constructor(
    private val repository: WorkRepository
) : ViewModel() {

    val completedWorks = repository.completedWorks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}