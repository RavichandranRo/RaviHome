package com.example.ravihome.ui.eb

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EbViewModel @Inject constructor() : ViewModel() {

    fun calculate(units: Float): Float {
        return when {
            units <= 100 -> 0f
            units <= 200 -> (units - 100) * 2.25f
            units <= 400 -> 225 + (units - 200) * 4.5f
            units <= 500 -> 1125 + (units - 400) * 6f
            else -> 1725 + (units - 500) * 8f
        }
    }
}
