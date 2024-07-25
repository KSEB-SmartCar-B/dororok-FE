package com.kseb.smart_car.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.AccessState
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
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
):ViewModel() {
    private val _accessState = MutableStateFlow<AccessState>(AccessState.Loading)
    val accessState: StateFlow<AccessState> = _accessState.asStateFlow()

    fun getAccessToken(token:String){
        viewModelScope.launch {
            authRepository.getSignIn(token).onSuccess { response ->
                Log.d("allviewmodel","signed!")
                _accessState.value = AccessState.Success(response.accessToken)
            }.onFailure {
                _accessState.value = AccessState.Error("Error response failure: ${it.message}")

                Log.e("allviewmodel", "Error:${it.message}")
                Log.e("allviewmodel", Log.getStackTraceString(it))
                if (it is HttpException) {
                    try {
                        val errorBody: ResponseBody? = it.response()?.errorBody()
                        val errorBodyString = errorBody?.string() ?: ""

                        // JSONObject를 사용하여 메시지 추출
                        val jsonObject = JSONObject(errorBodyString)
                        val errorMessage = jsonObject.optString("errMsg", "Unknown error")

                        // 추출된 에러 메시지 로깅
                        Log.e("allviewmodel", "Error message: $errorMessage")
                    } catch (e: Exception) {
                        // JSON 파싱 실패 시 로깅
                        Log.e("allviewmodel", "Error parsing error body", e)
                    }
                }
            }
        }
    }

    fun setSignInLoading(){
        _accessState.value=AccessState.Loading
    }
}