package com.example.ravihome.ui.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ravihome.data.repository.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CompletedWorksViewModel @Inject constructor(
    repository: WorkRepository
) : ViewModel() {

    val completedWorks = repository.completedWorks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}