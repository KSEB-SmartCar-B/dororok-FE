package com.kseb.smart_car.presentation

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class KakaoAuthViewModelFactory(
    private val application: Application,
    private val allViewModel: AllViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KakaoAuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KakaoAuthViewModel(application, allViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
