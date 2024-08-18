package com.kseb.smart_car.presentation.main.my.music

import android.util.Log
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.data.repositoryImpl.AuthRepositoryImpl
import com.kseb.smart_car.domain.repository.AuthRepository
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

    private val _deleteMusicListState = MutableStateFlow<DeleteMusicListState>(DeleteMusicListState.Loading)
    val deleteMusicListState:StateFlow<DeleteMusicListState> = _deleteMusicListState.asStateFlow()

    private val _accessToken=MutableLiveData<String>()
    val accessToken:MutableLiveData<String> get()=_accessToken

    fun setAccessToken(token:String){
        _accessToken.value=token
    }

    fun deleteMusicList(deleteMusicList:List<String>){
        viewModelScope.launch {
            authRepository.deleteFavoriteMusicList(_accessToken.value!!, deleteMusicList).onSuccess {response->
                Log.d("savedMusicViewModel","music delete success: ${response.response}")
                _deleteMusicListState.value=DeleteMusicListState.Success(response.response)
            }.onFailure {
                _deleteMusicListState.value=DeleteMusicListState.Error("Error response failure: ${it.message}")
                Log.e("savedMusicViewModel", "music delete Error:${it.message}")
                Log.e("savedMusicViewModel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("savedMusicViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("savedMusicViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    fun setDeleteMusicStateLoading(){
        _deleteMusicListState.value=DeleteMusicListState.Loading
    }

}