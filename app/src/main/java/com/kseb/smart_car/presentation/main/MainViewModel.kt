package com.kseb.smart_car.presentation.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _accessToken = MutableLiveData<String>()
    val accessToken:MutableLiveData<String> get() = _accessToken

    fun setAccessToken(token: String) {
        _accessToken.value= token
    }
}