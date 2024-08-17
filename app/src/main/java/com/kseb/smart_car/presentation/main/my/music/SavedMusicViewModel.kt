package com.kseb.smart_car.presentation.main.my.music

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.data.repositoryImpl.AuthRepositoryImpl
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.GetFavoriteMusicState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedMusicViewModel @Inject constructor(
    private val authRepository:AuthRepository
):ViewModel() {
    private val _savedMusicState = MutableStateFlow<GetFavoriteMusicState>(GetFavoriteMusicState.Loading)
    val savedMusicState:StateFlow<GetFavoriteMusicState> = _savedMusicState.asStateFlow()

    private val deleteMusicList = mutableListOf<String>()

    private val _accessToken=MutableLiveData<String>()
    val accessToken:MutableLiveData<String> get()=_accessToken

    fun setAccessToken(token:String){
        _accessToken.value=token
    }

    fun deleteMusicAdd(trackId:String){
        if(trackId in deleteMusicList){
            deleteMusicList.remove(trackId)
        }else{
            deleteMusicList.add(trackId)
        }
    }

    fun deleteMusicList(){
        /*viewModelScope.launch {
            authRepository.deleteFavoriteMusic(_accessToken!!.value, deleteMusicList){

            }
        }*/
    }

}