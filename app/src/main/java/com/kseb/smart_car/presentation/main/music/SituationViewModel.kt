package com.kseb.smart_car.presentation.main.music

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.SituationState
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
class SituationViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {
    private val _situationState= MutableStateFlow<SituationState>(SituationState.Loading)
    val situationState:StateFlow<SituationState> = _situationState.asStateFlow()
    fun makeList(){
        viewModelScope.launch{
            authRepository.getSituations().onSuccess { response->
                _situationState.value=SituationState.Success(response)
                Log.d("situationViewModel","musicState 성공")
            }.onFailure {
                _situationState.value=SituationState.Error("Error response failure: ${it.message}")
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""
                        // 전체 에러 바디를 로깅하여 디버깅
                        Log.e("searchViewModel", "Full error body: $errorBodyString")

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("message", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("searchViewModel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("searchViewModel", "Error parsing error body", e)
                    }
                }
            }
        }
    }
}