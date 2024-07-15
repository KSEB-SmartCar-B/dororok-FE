package com.kseb.smart_car.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.domain.repository.AuthRepository
import com.kseb.smart_car.extension.AccessState
import com.kseb.smart_car.extension.SignInState
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
class AllViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Loading)
    val signInState:StateFlow<SignInState> = _signInState.asStateFlow()
    private val _accessState = MutableStateFlow<AccessState>(AccessState.Loading)
    val accessState: StateFlow<AccessState> = _accessState.asStateFlow()

    private val _accessToken = MutableLiveData<String?>()
    val accessToken: MutableLiveData<String?> get() = _accessToken

    fun getAccessToken(token: String) {
        viewModelScope.launch {
            authRepository.isSigned(token).onSuccess { response ->
                if(response.isSigned){
                    _signInState.value=SignInState.Success(true)
                    viewModelScope.launch {
                        authRepository.getSignIn(token).onSuccess { response ->
                            Log.d("allviewmodel","signed!")
                            _accessState.value = AccessState.Success(response.jwtToken.accessToken)
                            _accessToken.value = response.jwtToken.accessToken
                        }
                    }
                } else{
                    _signInState.value=SignInState.Success(false)
                   Log.d("allviewmodel","isn't signed")
                }
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
}