package com.kseb.smart_car.presentation.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kseb.smart_car.BuildConfig
import com.kseb.smart_car.R
import com.kseb.smart_car.domain.repository.KakaoRepository
import com.kseb.smart_car.extension.AddressState
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
class MainViewModel @Inject constructor(
    private val kakaoRepository: KakaoRepository
) : ViewModel() {
    private val _accessToken = MutableLiveData<String>()
    val accessToken:MutableLiveData<String> get() = _accessToken

    private val _addressState = MutableStateFlow<AddressState>(AddressState.Loading)
    val addressState:StateFlow<AddressState> = _addressState.asStateFlow()

    fun setAccessToken(token: String) {
        _accessToken.value= token
    }

    fun getAddress(address:String){
        val api = "KakaoAK ${BuildConfig.KAKAO_REST_API}"
        Log.d("mainviewmodel","api key: ${api}")

        viewModelScope.launch {
            kakaoRepository.getAddress(api, address).onSuccess { response->
                _addressState.value=AddressState.Success(response)
                Log.d("mainViewModel","addressState 성공!")
            }.onFailure {
                _addressState.value=AddressState.Error("Error response failure: ${it.message}")
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
}