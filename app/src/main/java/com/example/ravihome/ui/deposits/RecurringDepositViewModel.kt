package com.example.ravihome.ui.deposits

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecurringDepositViewModel : ViewModel() {
    private val _entries = MutableStateFlow<List<DepositEntry>>(emptyList())
    val entries: StateFlow<List<DepositEntry>> = _entries

    fun add(entry: DepositEntry) {
        _entries.value = listOf(entry) + _entries.value
    }

    fun update(entry: DepositEntry) {
        _entries.value = _entries.value.map { if (it.id == entry.id) entry else it }
    }
}