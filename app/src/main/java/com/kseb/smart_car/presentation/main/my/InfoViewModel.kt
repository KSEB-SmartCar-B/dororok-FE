package com.kseb.smart_car.presentation.main.my

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.data.requestDto.RequestUpdateInfoDto
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.UpdateInfoState
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
class InfoViewModel @Inject constructor(
    val authRepository: AuthRepository
):ViewModel() {
    private val _updateInfoState = MutableStateFlow<UpdateInfoState>(UpdateInfoState.Loading)
    val updateInfoState:StateFlow<UpdateInfoState> = _updateInfoState.asStateFlow()

    fun updateInfo(token:String, info:RequestUpdateInfoDto){
        viewModelScope.launch {
            authRepository.updateInfo(token, info).onSuccess {response ->
                _updateInfoState.value=UpdateInfoState.Success(response)
            }.onFailure {
                _updateInfoState.value=UpdateInfoState.Error("Error response failure: ${it.message}")

                Log.e("infoViewModel", "Error:${it.message}")
                Log.e("infoViewModel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("infoViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("infoViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }
}