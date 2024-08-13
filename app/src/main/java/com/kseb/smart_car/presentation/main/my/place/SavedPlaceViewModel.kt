package com.kseb.smart_car.presentation.main.my.place

import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.GetFavoritePlaceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedPlaceViewModel @Inject constructor(
    private val authRepository: AuthRepository
) :ViewModel() {
    private val _accessToken = MutableLiveData<String>()
    val accessToken:MutableLiveData<String> get()=_accessToken

    fun setToken(token:String){
        accessToken.value=token
    }
}