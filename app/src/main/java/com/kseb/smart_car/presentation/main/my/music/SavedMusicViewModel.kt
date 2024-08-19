package com.kseb.smart_car.presentation.main.my.music

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.data.repositoryImpl.AuthRepositoryImpl
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.AddressState
import com.kseb.smart_car.extension.DeleteMusicListState
import com.kseb.smart_car.extension.GetFavoriteMusicState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class SavedMusicViewModel @Inject constructor(
    private val authRepository:AuthRepository
):ViewModel() {
    private val _savedMusicState = MutableStateFlow<GetFavoriteMusicState>(GetFavoriteMusicState.Loading)
    val savedMusicState:StateFlow<GetFavoriteMusicState> = _savedMusicState.asStateFlow()

    private val _deleteMusicListState =  MutableStateFlow<DeleteMusicListState>(DeleteMusicListState.Loading)
    val deleteMusicListState:StateFlow<DeleteMusicListState> = _deleteMusicListState.asStateFlow()


    private val _accessToken=MutableLiveData<String>()
    val accessToken:MutableLiveData<String> get()=_accessToken

    fun setAccessToken(token:String){
        _accessToken.value=token
    }

    fun deleteMusicList(musicList:List<String>){
        viewModelScope.launch {
            authRepository.deleteFavoriteMusicList(_accessToken.value!!, musicList).onSuccess {response->
                _deleteMusicListState.value=DeleteMusicListState.Success(response.response)
                Log.d("savedMusicViewModel","delete music list success!")
            }.onFailure {
                _deleteMusicListState.value= DeleteMusicListState.Error("delete music list Error response failure: ${it.message}")
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // 전체 에러 바디를 로깅하여 디버깅
                        Log.e("mainViewModel", "Full error body: $errorBodyString")

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("message", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("mainViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("mainViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    fun setDeleteMusicListStateLoading(){
        _deleteMusicListState.value=DeleteMusicListState.Loading
    }
}