package com.kseb.smart_car.presentation.main.my.place

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.DeleteMusicListState
import com.kseb.smart_car.extension.DeletePlaceListState
import com.kseb.smart_car.extension.GetFavoritePlaceState
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
class SavedPlaceViewModel @Inject constructor(
    private val authRepository: AuthRepository
) :ViewModel() {
    private val _accessToken = MutableLiveData<String>()
    val accessToken:MutableLiveData<String> get()=_accessToken

    private val _deletePlaceListState =  MutableStateFlow<DeletePlaceListState>(DeletePlaceListState.Loading)
    val deletePlaceListState:StateFlow<DeletePlaceListState> = _deletePlaceListState.asStateFlow()


    fun setToken(token:String){
        accessToken.value=token
    }

    fun deletePlaceList(deleteList:List<String>){
        viewModelScope.launch {
            authRepository.deleteFavoritePlaceList(_accessToken.value!!, deleteList).onSuccess { response->
               _deletePlaceListState.value=DeletePlaceListState.Success(response.response)
                Log.d("savedPlaceViewModel","delete place list success!")
            } .onFailure {
                _deletePlaceListState.value= DeletePlaceListState.Error("delete place list Error response failure: ${it.message}")
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

    fun setDeletePlaceListStateLoading(){
        _deletePlaceListState.value=DeletePlaceListState.Loading
    }

}